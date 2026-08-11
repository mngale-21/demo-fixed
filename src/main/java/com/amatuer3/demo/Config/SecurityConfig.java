package com.amatuer3.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) 
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // Fungua kila kitu 100%
        )
        .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Ruhusu iFrame kama unazitumia
        .formLogin(form -> form.disable());

    return http.build();
}}