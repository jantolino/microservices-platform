package edu.market.userservice.infrastructure.crosscutting.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  // Endpoints públicos (swagger, auth, actuator, social login)
  String[] whiteList = {
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs",
    "/v3/api-docs/**",
    "/api-docs",
    "/api-docs/**",
    "/actuator",
    "/actuator/**",
    "/api/auth/**",
    "/api/auth/login",
    "/api/auth/signup",
    "/api/auth/social-login",
    "/login/oauth2/**"
  };


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    
    http.headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth ->
            auth.requestMatchers(whiteList).permitAll()
                .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt()); // Habilita validación JWT para integración con auth-server

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
