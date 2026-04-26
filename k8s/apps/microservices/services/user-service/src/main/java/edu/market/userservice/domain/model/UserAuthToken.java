package edu.market.userservice.domain.model;

import edu.market.userservice.domain.enums.AuthProviderType;
import edu.market.userservice.domain.enums.TokenStatusType;

import java.time.LocalDateTime;

/**
 * Modelo para tokens de autenticación de usuarios
 */
public class UserAuthToken {
    
    private Long id;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private TokenStatusType status;
    private String ipAddress;
    private String userAgent;
    private AuthProviderType provider;
    private Long userId;    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime revokedAt;
    
    // Constructor por defecto
    public UserAuthToken() {
    }
    
    // Constructor completo
    public UserAuthToken(Long id, String accessToken, String refreshToken, LocalDateTime expiresAt,
                        LocalDateTime refreshExpiresAt, TokenStatusType status, String ipAddress,
                        String userAgent, AuthProviderType provider, Long userId,
                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime revokedAt) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.status = status;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.provider = provider;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.revokedAt = revokedAt;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }
    
    public TokenStatusType getStatus() {
        return status;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public AuthProviderType getProvider() {
        return provider;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }
    
    public void setStatus(TokenStatusType status) {
        this.status = status;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public void setProvider(AuthProviderType provider) {
        this.provider = provider;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    

    
    /**
     * Verifica si el token de acceso ha expirado
     * 
     * @return true si el token ha expirado
     */
    public boolean isAccessTokenExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Verifica si el refresh token ha expirado
     * 
     * @return true si el refresh token ha expirado
     */
    public boolean isRefreshTokenExpired() {
        return LocalDateTime.now().isAfter(refreshExpiresAt);
    }
    
    /**
     * Verifica si el token está activo
     * 
     * @return true si el token está activo
     */
    public boolean isActive() {
        return status == TokenStatusType.ACTIVE && !isAccessTokenExpired();
    }
    
    /**
     * Verifica si el refresh token es válido para renovación
     * 
     * @return true si el refresh token es válido
     */
    public boolean isValidForRefresh() {
        return status == TokenStatusType.ACTIVE && !isRefreshTokenExpired();
    }
    
    /**
     * Revoca el token
     */
    public void revoke() {
        this.status = TokenStatusType.REVOKED;
        this.revokedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca el token como expirado
     */
    public void markAsExpired() {
        this.status = TokenStatusType.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca el token como incluido en lista negra
     */
    public void blacklist() {
        this.status = TokenStatusType.BLACKLISTED;
        this.updatedAt = LocalDateTime.now();
    }
}
