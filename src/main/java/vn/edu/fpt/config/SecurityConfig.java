package vn.edu.fpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.authority.AuthorityUtils;
import java.util.Set;
import vn.edu.fpt.service.CustomOAuth2UserService;
import vn.edu.fpt.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider) {

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (roles.contains("ROLE_MANAGER")) {
                response.sendRedirect("/manager/dashboard");
            } else if (roles.contains("ROLE_INSTRUCTOR")) {
                response.sendRedirect("/instructor/dashboard");
            } else {
                response.sendRedirect("/home");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CustomOAuth2UserService oAuth2UserService,
            CustomUserDetailsService userDetailsService) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                .authenticationProvider(authenticationProvider)

//                    .authorizeHttpRequests(auth -> auth
//                            .anyRequest().permitAll()
//                    )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/register",
                                "/css/**", "/js/**",
                                "/verify-otp",
                                "/resend-otp",
                                "/images/**", "/oauth2/**",
                                "/forgot-password",
                                "/reset-password",
                                "/password-reset-success"
                        ).permitAll()
                        .requestMatchers(
                                "/home",
                                "/",
                                "/courses",
                                "/course/detail",
                                "/instructor/*/profile"
                        ).access((authentication, context) -> {
                            if (authentication.get() == null || !authentication.get().isAuthenticated()) {
                                return new org.springframework.security.authorization.AuthorizationDecision(true);
                            }
                            boolean hasRestrictedRole = authentication.get().getAuthorities().stream()
                                    .anyMatch(a -> {
                                        String role = a.getAuthority();
                                        return role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER") || role.equals("ROLE_INSTRUCTOR");
                                    });
                            return new org.springframework.security.authorization.AuthorizationDecision(!hasRestrictedRole);
                        })
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/instructorcourse/**").hasRole("INSTRUCTOR")
                        .requestMatchers("/student/**").hasRole("LEARNER")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")   // thêm dòng này
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .rememberMe(remember -> remember
                .rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService)
                .key("learninghub-remember-me-secret-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .useSecureCookie(false)
        )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserService)
                        )
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }
}
