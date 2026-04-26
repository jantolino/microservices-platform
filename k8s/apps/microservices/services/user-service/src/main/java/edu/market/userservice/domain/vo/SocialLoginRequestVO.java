package edu.market.userservice.domain.vo;

import java.util.Objects;

/**
 * Value Object para representar una solicitud de autenticación mediante proveedor social
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class SocialLoginRequestVO {
    
    private final String provider;
    private final String providerId;
    private final String email;
    private final String name;
    private final String pictureUrl;
    private final String ipAddress;
    private final String userAgent;
    
    /**
     * Constructor para la solicitud de autenticación social
     * 
     * @param provider Tipo de proveedor (GOOGLE, FACEBOOK, etc.)
     * @param providerId ID del usuario en el proveedor
     * @param email Email del usuario
     * @param name Nombre completo del usuario
     * @param pictureUrl URL de la imagen de perfil
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     */
    public SocialLoginRequestVO(
            String provider,
            String providerId,
            String email,
            String name,
            String pictureUrl,
            String ipAddress,
            String userAgent) {
        
        // Validaciones
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("El proveedor no puede estar vacío");
        }
        if (providerId == null || providerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del proveedor no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPictureUrl() {
        return pictureUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialLoginRequestVO that = (SocialLoginRequestVO) o;
        return Objects.equals(provider, that.provider) &&
               Objects.equals(providerId, that.providerId) &&
               Objects.equals(email, that.email) &&
               Objects.equals(name, that.name) &&
               Objects.equals(pictureUrl, that.pictureUrl) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(userAgent, that.userAgent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(provider, providerId, email, name, pictureUrl, ipAddress, userAgent);
    }
    
    @Override
    public String toString() {
        return "SocialLoginRequestVO{" +
                "provider='" + provider + '\'' +
                ", providerId='" + providerId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
