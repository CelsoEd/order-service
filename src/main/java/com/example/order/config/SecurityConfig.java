package com.example.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Desativa CSRF para APIs REST (pode ser reativado se necessário)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/login").permitAll() // Permite acesso público ao login, sem autenticação
                        .requestMatchers("/api/registro").permitAll() // Permite acesso público ao registro, sem autenticação
                        .requestMatchers("/api/pedido/listar-produtos").permitAll() // Permite acesso público ao listar-produtos, sem autenticação
                        .requestMatchers("/api/pedido/**").authenticated() // Requer autenticação para outros endpoints de pedido
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}