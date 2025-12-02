package com.coderscampus.backgammon_vanilla.config;

import com.coderscampus.backgammon_vanilla.service.UserService;
import com.coderscampus.backgammon_vanilla.web.GameController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                .anyRequest().authenticated()
        );
        http.formLogin(form -> form
                .loginPage("/login")
                .successHandler(markOnlineSuccessHandler(userService))
        );
        http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(markOnlineSuccessHandler(userService))
        );
        http.logout(logout -> logout.logoutSuccessHandler(markOfflineLogoutHandler(userService)));
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler markOnlineSuccessHandler(UserService userService) {
        return (request, response, authentication) -> {
            String name = GameController.extractName(authentication);
            String email = GameController.extractEmail(authentication);
            userService.markOnline(name, email, true, true);
            SavedRequestAwareAuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();
                    delegate.setDefaultTargetUrl("/dashboard");
                    delegate.onAuthenticationSuccess(request, response, authentication);

        };
    }

    @Bean
    public LogoutSuccessHandler markOfflineLogoutHandler(UserService userService){
        return (request, response, authentication) -> {
            if(authentication != null) {
                String name = GameController.extractName(authentication);
                String email = GameController.extractEmail(authentication);
                userService.markOnline(name, email, false, false);
            }
            response.sendRedirect("/login?logout");
        };
    }
}
