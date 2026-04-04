package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.LoginMetadataVO;
import edu.market.userservice.domain.vo.SocialAuthenticationResultVO;
import edu.market.userservice.domain.vo.SocialLoginDataVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;
import edu.market.userservice.domain.model.MetadataLogin;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Puerto de entrada para el servicio de dominio de autenticación
 */
public interface AuthDomainServicePort {
    
    /**
     * Valida las credenciales de un usuario
     * 
     * @param user Usuario a validar
     * @param rawPassword Contraseña sin codificar
     * @param passwordEncoder Codificador de contraseñas de Spring Security
     * @return true si las credenciales son válidas
     */
    boolean validateCredentials(User user, String rawPassword, PasswordEncoder passwordEncoder);    

    
    /**
     * Actualiza o crea un usuario a partir de datos de autenticación social
     * 
     * @param existingUser Usuario existente (puede ser null)
     * @param socialLoginData Value Object con los datos de autenticación social
     * @return Usuario actualizado o creado
     */
    User processUserFromSocialLogin(User existingUser, SocialLoginDataVO socialLoginData);
    
    /**
     * Crea un registro de metadatos de inicio de sesión
     * 
     * @param user Usuario que inicia sesión
     * @param ipAddress Dirección IP
     * @param userAgent User-Agent del navegador
     * @return Metadatos de inicio de sesión
     */
    MetadataLogin createLoginMetadata(User user, String ipAddress, String userAgent);
    
    /**
     * Crea un registro de metadatos de inicio de sesión usando un Value Object
     * 
     * @param metadataVO Value Object con los datos para los metadatos de login
     * @return Metadatos de inicio de sesión
     */
    MetadataLogin createLoginMetadata(LoginMetadataVO metadataVO);
    
    /**
     * Crea un registro de metadatos de registro de usuario
     * 
     * @param user Usuario que se registra
     * @param ipAddress Dirección IP
     * @param userAgent User-Agent del navegador
     * @return Metadatos de registro
     */
    MetadataLogin createRegistrationMetadata(User user, String ipAddress, String userAgent);
    
    /**
     * Autentica a un usuario verificando sus credenciales y actualizando su último login
     * 
     * @param user Usuario a autenticar
     * @param rawPassword Contraseña sin codificar
     * @param passwordEncoder Codificador de contraseñas
     * @return Usuario autenticado con último login actualizado
     */
    User authenticateUser(User user, String rawPassword, PasswordEncoder passwordEncoder);
    
    /**
     * Procesa una autenticación social, crea o actualiza el usuario y genera un token
     * 
     * @param socialLoginRequest Value Object con los datos de la solicitud de autenticación social
     * @return Value Object con el resultado de la autenticación social
     */
    SocialAuthenticationResultVO processSocialLoginAndGenerateToken(SocialLoginRequestVO socialLoginRequest);
}
