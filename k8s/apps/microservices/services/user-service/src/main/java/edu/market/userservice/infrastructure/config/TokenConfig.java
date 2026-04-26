package edu.market.userservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para los tokens de autenticación
 */
@Configuration
@ConfigurationProperties(prefix = "custom.auth.token")
public class TokenConfig {
    
    private int accessTokenDurationMinutes = 30; // Valor por defecto
    private int refreshTokenDurationDays = 7;    // Valor por defecto
    
    public int getAccessTokenDurationMinutes() {
        return accessTokenDurationMinutes;
    }
    
    public void setAccessTokenDurationMinutes(int accessTokenDurationMinutes) {
        this.accessTokenDurationMinutes = accessTokenDurationMinutes;
    }
    
    public int getRefreshTokenDurationDays() {
        return refreshTokenDurationDays;
    }
    
    public void setRefreshTokenDurationDays(int refreshTokenDurationDays) {
        this.refreshTokenDurationDays = refreshTokenDurationDays;
    }
}
