package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.service.AuthUserHelper;
import com.coderscampus.backgammon.service.PresenceService;
import com.coderscampus.backgammon.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceController {
    private final AuthUserHelper authUserHelper;
    private final UserService userService;
    private final PresenceService presenceService;

    public PresenceController(AuthUserHelper authUserHelper,
                              UserService userService,
                              PresenceService presenceService) {
        this.authUserHelper = authUserHelper;
        this.userService = userService;
        this.presenceService = presenceService;
    }

    @MessageMapping("/presence/dashboard")
    public void dashboardHeartbeat(Authentication authentication) {
        User user = authUserHelper.resolveUser(authentication, userService);
        if (user == null) {
            return;
        }
        presenceService.touchDashboard(user.getUserId());
    }
}
