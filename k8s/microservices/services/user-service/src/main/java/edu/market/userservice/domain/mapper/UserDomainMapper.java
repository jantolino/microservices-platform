package edu.market.userservice.domain.mapper;

import edu.market.userservice.domain.enums.AuthProviderType;
import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.SocialLoginDataVO;

import java.time.LocalDateTime;

/**
 * Mapper para entidades de dominio relacionadas con usuarios
 */
public class UserDomainMapper {
    
    /**
     * Crea un nuevo usuario a partir de datos de login social
     * 
     * @param socialLoginData Datos de autenticación social
     * @param firstName Nombre extraído
     * @param lastName Apellido extraído
     * @return Usuario creado
     */
    public static User toNewUserFromSocialLogin(SocialLoginDataVO socialLoginData, String firstName, String lastName) {
        User user = new User();
        user.setEmail(socialLoginData.getEmail());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setName(socialLoginData.getName());        
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        return user;
    }
    
    /**
     * Crea metadatos de login para un usuario
     * 
     * @param user Usuario
     * @param provider Proveedor de autenticación
     * @param ipAddress Dirección IP
     * @param userAgent Agente de usuario
     * @return Metadatos de login
     */
    public static MetadataLogin toLoginMetadata(User user, AuthProviderType provider, String ipAddress, String userAgent) {
        MetadataLogin metadata = new MetadataLogin();
        metadata.setUserId(user.getId());
        metadata.setProvider(provider);
        metadata.setIpAddress(ipAddress);
        metadata.setUserAgent(userAgent);       
        
        LocalDateTime now = LocalDateTime.now();
        metadata.setCreatedAt(now);
        metadata.setUpdatedAt(now);
        
        return metadata;
    }
    
    /**
     * Crea metadatos de registro para un usuario
     * 
     * @param user Usuario
     * @param provider Proveedor de autenticación
     * @param ipAddress Dirección IP
     * @param userAgent Agente de usuario
     * @return Metadatos de login
     */
    public static MetadataLogin toRegistrationMetadata(User user, AuthProviderType provider, String ipAddress, String userAgent) {
        MetadataLogin metadata = new MetadataLogin();
        metadata.setUserId(user.getId());
        metadata.setProvider(provider);
        metadata.setIpAddress(ipAddress);
        metadata.setUserAgent(userAgent);
        
        LocalDateTime now = LocalDateTime.now();        
        metadata.setCreatedAt(now);
        metadata.setUpdatedAt(now);
        
        return metadata;
    }
}
