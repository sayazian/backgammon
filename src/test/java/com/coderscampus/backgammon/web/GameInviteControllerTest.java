package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.dto.GameInvite;
import com.coderscampus.backgammon.dto.GameInviteResponse;
import com.coderscampus.backgammon.dto.GameInviteStatus;
import com.coderscampus.backgammon.service.AuthUserHelper;
import com.coderscampus.backgammon.service.GameService;
import com.coderscampus.backgammon.service.GameRuntimeService;
import com.coderscampus.backgammon.service.PendingGameInviteService;
import com.coderscampus.backgammon.service.PresenceService;
import com.coderscampus.backgammon.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameInviteControllerTest {
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;
    private AuthUserHelper authUserHelper;
    private GameService gameService;
    private PendingGameInviteService pendingGameInviteService;
    private PresenceService presenceService;
    private GameRuntimeService gameRuntimeService;
    private GameInviteController controller;
    private Authentication authentication;
    private User inviter;
    private User invitee;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        userService = mock(UserService.class);
        authUserHelper = mock(AuthUserHelper.class);
        gameService = mock(GameService.class);
        pendingGameInviteService = new PendingGameInviteService();
        presenceService = mock(PresenceService.class);
        gameRuntimeService = mock(GameRuntimeService.class);
        controller = new GameInviteController(
                messagingTemplate,
                userService,
                authUserHelper,
                gameService,
                pendingGameInviteService,
                presenceService,
                gameRuntimeService
        );
        authentication = mock(Authentication.class);

        inviter = new User("Inviter", "inviter@example.com");
        inviter.setUserId(11L);

        invitee = new User("Invitee", "invitee@example.com");
        invitee.setUserId(22L);
    }

    @Test
    void sendInviteNotDeliveredReportsStatusInsteadOfCreatingGame() {
        GameInvite invite = new GameInvite();
        invite.setToUserId(invitee.getUserId());
        invite.setToUserName(invitee.getName());
        invite.setMessage("Play?");

        when(authUserHelper.resolveUser(authentication, userService)).thenReturn(inviter);
        when(presenceService.isUserAvailableForInvite(invitee.getUserId())).thenReturn(false);

        controller.sendInvite(invite, authentication);

        verify(gameService, never()).createGame(any(), any(), any(), any());
        verify(messagingTemplate).convertAndSend(
                eq("/topic/invitations/status/" + inviter.getUserId()),
                any(GameInviteStatus.class)
        );
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/invitations/" + invitee.getUserId()), any(GameInvite.class));
    }

    @Test
    void acceptedInviteCreatesGameAndNotifiesInviter() {
        GameInvite invite = new GameInvite();
        invite.setFromUserId(inviter.getUserId());
        invite.setFromUserName(inviter.getName());
        invite.setToUserId(invitee.getUserId());
        invite.setToUserName(invitee.getName());
        pendingGameInviteService.createPendingInvite(invite);

        Game game = new Game();
        game.setGameId(77L);

        GameInviteResponse response = new GameInviteResponse();
        response.setInviteId(invite.getInviteId());
        response.setAccepted(true);

        when(authUserHelper.resolveUser(authentication, userService)).thenReturn(invitee);
        when(gameService.createGame(inviter.getUserId(), invitee.getUserId(), inviter.getName(), invitee.getName())).thenReturn(game);

        controller.responseToInvite(response, authentication);

        verify(gameService).createGame(inviter.getUserId(), invitee.getUserId(), inviter.getName(), invitee.getName());
        verify(gameRuntimeService).registerGame(game);
        verify(presenceService).registerGame(game);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/invitations/responses/" + inviter.getUserId()),
                any(GameInviteResponse.class)
        );
    }

    @Test
    void sendInviteDeliveredPublishesInvitation() {
        GameInvite invite = new GameInvite();
        invite.setToUserId(invitee.getUserId());
        invite.setToUserName(invitee.getName());

        when(authUserHelper.resolveUser(authentication, userService)).thenReturn(inviter);
        when(presenceService.isUserAvailableForInvite(invitee.getUserId())).thenReturn(true);

        controller.sendInvite(invite, authentication);

        verify(messagingTemplate).convertAndSend(eq("/topic/invitations/" + invitee.getUserId()), any(GameInvite.class));
    }
}
