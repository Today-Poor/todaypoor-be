package com.todaypoor.global.config;

import com.todaypoor.global.security.CustomAccessDeniedHandler;
import com.todaypoor.global.security.CustomAuthenticationEntryPoint;
import com.todaypoor.global.security.CustomUserDetailsService;
import com.todaypoor.global.security.JwtAuthenticationFilter;
import com.todaypoor.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/**", "/login/**", "/api/auth/reissue", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // TODO: 서브 이슈 4에서 아래의 실제 핸들러 및 서비스 구현체 연동 예정
                        // .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        // .successHandler(oAuth2AuthenticationSuccessHandler)
                        // .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(tokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
