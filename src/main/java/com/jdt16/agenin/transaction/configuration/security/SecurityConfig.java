package com.jdt16.agenin.transaction.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF untuk testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/transaction/**").permitAll() // Izinkan endpoint transaction
                        .requestMatchers("/api/v1/user/**").permitAll() // Izinkan endpoint user jika perlu
                        .anyRequest().authenticated() // Endpoint lain tetap butuh autentikasi
                );

        return http.build();
    }
}
