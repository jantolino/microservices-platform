package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.enums.AuthProviderType;

/**
 * Value Object para los datos de autenticación social
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class SocialLoginDataVO {
    
    private final AuthProviderType provider;
    private final String email;
    private final String name;
    private final String providerId;
    private final String pictureUrl;
    
    public SocialLoginDataVO(AuthProviderType provider, String email, String name, String providerId, String pictureUrl) {
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.providerId = providerId;
        this.pictureUrl = pictureUrl;
    }
    
    public AuthProviderType getProvider() {
        return provider;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getName() {
        return name;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public String getPictureUrl() {
        return pictureUrl;
    }
}
