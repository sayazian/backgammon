package com.coderscampus.backgammon.config;

import com.coderscampus.backgammon.web.LoginSuccessHandler;
import com.coderscampus.backgammon.web.LogoutSuccessHandler;
import com.coderscampus.backgammon.web.filter.ActivityTrackingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            ClientRegistrationRepository clientRegistrationRepository,
                                            LoginSuccessHandler loginSuccessHandler,
                                            LogoutSuccessHandler logoutSuccessHandler,
                                            ActivityTrackingFilter activityTrackingFilter) throws Exception {
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        authorizationRequestResolver.setAuthorizationRequestCustomizer(builder ->
                builder.additionalParameters(params -> params.put("prompt", "select_account")));

        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/css/**", "/js/**", "/images/**", "/preview/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(authorizationRequestResolver))
                        .successHandler(loginSuccessHandler))
                .formLogin(form -> form
                        .loginPage("/")
                        .successHandler(loginSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .addFilterAfter(activityTrackingFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
