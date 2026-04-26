package edu.market.userservice.domain.mapper;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.LoginCredentialsVO;
import edu.market.userservice.domain.vo.UserRegistrationVO;

/**
 * Mapper para convertir entre Value Objects y modelos de dominio
 */
public class ValueObjectMapper {
    
    /**
     * Extrae un usuario de un Value Object de registro
     * 
     * @param registrationVO Value Object de registro
     * @return Usuario extraído
     */
    public static User extractUser(UserRegistrationVO registrationVO) {
        if (registrationVO == null) {
            return null;
        }
        
        return registrationVO.getUser();
    }
    
    /**
     * Crea un Value Object de credenciales a partir de datos individuales
     * 
     * @param email Email del usuario
     * @param password Contraseña
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de credenciales
     */
    public static LoginCredentialsVO createLoginCredentials(String email, String password, String ipAddress, String userAgent) {
        return new LoginCredentialsVO(email, password, ipAddress, userAgent);
    }
    
    /**
     * Crea un Value Object de registro a partir de datos individuales
     * 
     * @param user Usuario a registrar
     * @param rawPassword Contraseña en texto plano
     * @param confirmPassword Confirmación de la contraseña
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de registro
     */
    public static UserRegistrationVO createUserRegistration(User user, String rawPassword, String confirmPassword, String ipAddress, String userAgent) {
        return new UserRegistrationVO(user, rawPassword, confirmPassword, ipAddress, userAgent);
    }
}
