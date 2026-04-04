package edu.market.userservice.domain.model;

import java.time.LocalDateTime;

import edu.market.userservice.domain.enums.AuthProviderType;

/**
 * Modelo para almacenar metadatos de inicio de sesión de usuarios
 */
public class MetadataLogin {
    
    private Long id;
    private Long userId;
    private AuthProviderType provider;
    private String providerId;
    private String pictureUrl;    
    private boolean enabled;
    
    // Campos de auditoría
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor por defecto
    public MetadataLogin() {
    }
    
    // Constructor completo
    public MetadataLogin(Long id, Long userId, AuthProviderType provider, String providerId, String pictureUrl,
                         boolean enabled,
                         String ipAddress, String userAgent, String deviceInfo,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.pictureUrl = pictureUrl;
        this.enabled = enabled;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public AuthProviderType getProvider() {
        return provider;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public String getPictureUrl() {
        return pictureUrl;
    }    
    
    public boolean isEnabled() {
        return enabled;
    }    
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setProvider(AuthProviderType provider) {
        this.provider = provider;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Registra un inicio de sesión exitoso
     * 
     * @param ipAddress Dirección IP
     * @param userAgent Agente de usuario
     * @param deviceInfo Información del dispositivo
     */
    public void registerSuccessfulLogin(String ipAddress, String userAgent, String deviceInfo) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.updatedAt = LocalDateTime.now();
    }
}
