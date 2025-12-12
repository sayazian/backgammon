package com.coderscampus.backgammon_vanilla.web;

import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.service.AuthUserHelper;
import com.coderscampus.backgammon_vanilla.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class GameController {

    private final UserService userService;
    private final AuthUserHelper authUserHelper;

    public GameController(UserService userService,
                          AuthUserHelper authUserHelper) {
        this.userService = userService;
        this.authUserHelper = authUserHelper;
    }

    @GetMapping({"/", "/login"})
    public String login(Authentication authentication) {
        if (isAnonymous(authentication)) {
            return "login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, ModelMap model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        String name = authUserHelper.extractName(authentication);
        String email = authUserHelper.extractEmail(authentication);
        User user = userService.findUser(name, email);
        userService.logUserIn(user);
        List<User> onlineUsers = userService.extractOnlineUsers();
        model.put("user", user);
        model.put("onlineUsers", onlineUsers);
        return "dashboard";
    }


    @GetMapping("/profile")
    public String profile(Authentication authentication, ModelMap model) {
        String name = authUserHelper.extractName(authentication);
        String email = authUserHelper.extractEmail(authentication);
        User user = userService.findUser(name, email);
        model.put("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@ModelAttribute("user") User user) {
        userService.updateUser(user);
        return "profile";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response, @ModelAttribute("user") User user) {
//        userService.logUserOut(user);
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//        }
        return "redirect:/login?logout";
    }


    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }


    private User getUser(Authentication authentication) {
        User user = new User(authUserHelper.extractName(authentication), authUserHelper.extractEmail(authentication));
        user = userService.updateUser(user);
        return user;
    }

}
