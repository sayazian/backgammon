package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.service.ProfileService;
import com.coderscampus.backgammon.service.ProfileService.UserProfile;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String email = extractEmail(authentication);
        if (email == null) {
            return "redirect:/";
        }

        Optional<UserProfile> maybeProfile = profileService.findProfileByEmail(email);
        if (maybeProfile.isEmpty()) {
            return "redirect:/dashboard";
        }

        UserProfile profile = maybeProfile.get();
        ProfileForm form = new ProfileForm();
        form.setUsername(profile.username());
        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", form);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("profileForm") ProfileForm form,
                                BindingResult bindingResult,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String email = extractEmail(authentication);
        if (email == null) {
            return "redirect:/";
        }

        Optional<UserProfile> maybeProfile = profileService.findProfileByEmail(email);
        if (maybeProfile.isEmpty()) {
            return "redirect:/dashboard";
        }

        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "username.empty", "Username cannot be blank.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", maybeProfile.get());
            return "profile";
        }

        UserProfile profile = maybeProfile.get();
        profileService.updateUsername(profile.userId(), form.getUsername());
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        return "redirect:/profile";
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }

    public static class ProfileForm {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
