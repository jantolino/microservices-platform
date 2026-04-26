package edu.market.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para el servicio de notificaciones.
 * 
 * Esta configuración permite el acceso público a todos los endpoints del servicio,
 * ya que la autenticación y autorización se manejan a nivel de API Gateway.
 * La dependencia spring-boot-starter-oauth2-client solo se utiliza para consumir
 * servicios protegidos, no para proteger este servicio.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para permitir todas las solicitudes.
     * 
     * @param http Configuración de seguridad HTTP
     * @return La cadena de filtros de seguridad configurada
     * @throws Exception Si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
