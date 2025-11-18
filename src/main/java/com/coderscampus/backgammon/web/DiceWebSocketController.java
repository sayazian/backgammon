package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.service.GameDiceService;
import com.coderscampus.backgammon.util.ChannelKeyUtil;
import com.coderscampus.backgammon.web.dto.DiceEventMessage;
import com.coderscampus.backgammon.web.dto.DiceExitRequest;
import com.coderscampus.backgammon.web.dto.DiceResetRequest;
import com.coderscampus.backgammon.web.dto.DiceRollRequest;
import com.coderscampus.backgammon.web.dto.DiceStateRequest;
import com.coderscampus.backgammon.web.dto.DiceTimeoutRequest;
import com.coderscampus.backgammon.web.dto.TurnRollMessage;
import com.coderscampus.backgammon.web.dto.TurnRollRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

@Controller
public class DiceWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameDiceService gameDiceService;

    public DiceWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   GameDiceService gameDiceService) {
        this.messagingTemplate = messagingTemplate;
        this.gameDiceService = gameDiceService;
    }

    @MessageMapping("/game.dice.roll")
    public void handleRoll(@Payload DiceRollRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        List<DiceEventMessage> events = gameDiceService.registerRoll(
                new DiceRollRequest(channelKey, request.playerKey(), request.playerName()));
        if (events.isEmpty()) {
            return;
        }
        String destination = destinationFor(channelKey);
        events.forEach(event -> messagingTemplate.convertAndSend(destination, event));
    }

    @MessageMapping("/game.dice.state")
    public void handleStateRequest(@Payload DiceStateRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        Optional<DiceEventMessage> snapshot = gameDiceService.currentState(channelKey);
        snapshot.ifPresent(event -> messagingTemplate.convertAndSend(destinationFor(channelKey), event));
    }

    @MessageMapping("/game.dice.turn")
    public void handleTurnRoll(@Payload TurnRollRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        TurnRollMessage message = TurnRollMessage.from(channelKey, request);
        if (message == null) {
            return;
        }
        messagingTemplate.convertAndSend(destinationFor(channelKey), message);
    }

    @MessageMapping("/game.dice.reset")
    public void handleReset(@Payload DiceResetRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        gameDiceService.reset(channelKey);
        String name = request.playerName();
        if (!StringUtils.hasText(name)) {
            name = "A player";
        } else {
            name = name.trim();
        }
        String message = name + " restarted the dice.";
        messagingTemplate.convertAndSend(
                destinationFor(channelKey),
                DiceEventMessage.reset(channelKey, message));
    }

    @MessageMapping("/game.dice.timeout")
    public void handleTimeout(@Payload DiceTimeoutRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        gameDiceService.reset(channelKey);
        String name = request.playerName();
        if (!StringUtils.hasText(name)) {
            name = "System";
        } else {
            name = name.trim();
        }
        String message = "Game closed due to inactivity. Triggered by " + name + '.';
        messagingTemplate.convertAndSend(
                destinationFor(channelKey),
                DiceEventMessage.timeout(channelKey, message));
    }

    @MessageMapping("/game.dice.exit")
    public void handleExit(@Payload DiceExitRequest request) {
        if (request == null || !StringUtils.hasText(request.gameKey())) {
            return;
        }
        String channelKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (channelKey == null) {
            return;
        }
        gameDiceService.reset(channelKey);
        String name = request.playerName();
        if (!StringUtils.hasText(name)) {
            name = "A player";
        } else {
            name = name.trim();
        }
        String message = name + " has exited the game.";
        messagingTemplate.convertAndSend(
                destinationFor(channelKey),
                DiceEventMessage.exit(channelKey, request.playerKey(), name, message));
    }

    private String destinationFor(String gameKey) {
        return "/topic/games/" + gameKey + "/dice";
    }
}
