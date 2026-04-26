package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.User;

/**
 * Value Object para los metadatos de login
 */
public final class LoginMetadataVO {
    
    private final User user;
    private final String ipAddress;
    private final String userAgent;
    private final String provider;
    private final String providerId;
    private final String pictureUrl;
    
    /**
     * Constructor para los metadatos de login
     * 
     * @param user Usuario que inicia sesión
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del navegador
     * @param provider Proveedor de autenticación (opcional)
     * @param providerId ID del proveedor (opcional)
     * @param pictureUrl URL de la imagen de perfil (opcional)
     */
    public LoginMetadataVO(User user, String ipAddress, String userAgent, 
            String provider, String providerId, String pictureUrl) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.provider = provider;
        this.providerId = providerId;
        this.pictureUrl = pictureUrl;
    }
    
    /**
     * Constructor para login local (sin proveedor social)
     * 
     * @param user Usuario que inicia sesión
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del navegador
     */
    public LoginMetadataVO(User user, String ipAddress, String userAgent) {
        this(user, ipAddress, userAgent, null, null, null);
    }
    
    /**
     * Constructor a partir de un SocialLoginRequestVO
     * 
     * @param user Usuario que inicia sesión
     * @param socialLoginRequest Datos de login social
     */
    public LoginMetadataVO(User user, SocialLoginRequestVO socialLoginRequest) {
        this(
            user,
            socialLoginRequest.getIpAddress(),
            socialLoginRequest.getUserAgent(),
            socialLoginRequest.getProvider(),
            socialLoginRequest.getProviderId(),
            socialLoginRequest.getPictureUrl()
        );
    }

    public User getUser() {
        return user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
    
    /**
     * Verifica si este login es de un proveedor social
     * 
     * @return true si el login es de un proveedor social
     */
    public boolean isSocialLogin() {
        return provider != null && !provider.isEmpty();
    }
}
