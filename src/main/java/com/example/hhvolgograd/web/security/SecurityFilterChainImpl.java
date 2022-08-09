package com.example.hhvolgograd.web.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.POST;

@EnableWebSecurity
@AllArgsConstructor
public class SecurityFilterChainImpl {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .servletApi(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .exceptionHandling(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(auth2ResourceServerConfigurer -> auth2ResourceServerConfigurer
                        .jwt().and()
                )
                .authorizeRequests(
                        authorizeRequestsConfigurer -> authorizeRequestsConfigurer
                                .antMatchers(POST, "/auth/registration/**")
                                .permitAll()
                                .antMatchers("/swagger-ui/**", "/v3/api-docs/**")
                                .permitAll()
                                .mvcMatchers(POST, "/resource/user/{id:\\d+}/updating")
                                .access("@userAccess.checkId(authentication,#id)")
                                .anyRequest()
                                .denyAll()
                )
                .build();
    }
}
