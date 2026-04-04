package edu.market.userservice.infrastructure.adapter.input.web.mapper;

import org.springframework.stereotype.Component;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.LoginCredentialsVO;
import edu.market.userservice.domain.vo.LogoutAllRequestVO;
import edu.market.userservice.domain.vo.LogoutRequestVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;
import edu.market.userservice.domain.vo.TokenRefreshRequestVO;
import edu.market.userservice.domain.vo.UserRegistrationVO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.LoginRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SignupRequestDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.SocialLoginRequestDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper para convertir DTOs de solicitud a Value Objects del dominio
 */
@Slf4j
@Component
public class AuthRequestMapper {
    
    /**
     * Convierte un DTO de solicitud de login a un Value Object de credenciales
     * 
     * @param requestDTO DTO de solicitud de login
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de credenciales
     */
    public LoginCredentialsVO toLoginCredentialsVO(LoginRequestDTO requestDTO, String ipAddress, String userAgent) {
        return new LoginCredentialsVO(
                requestDTO.getEmail(),
                requestDTO.getPassword(),
                ipAddress,
                userAgent
        );
    }
    
    /**
     * Convierte un DTO de solicitud de registro a un Value Object de registro
     * 
     * @param requestDTO DTO de solicitud de registro
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de registro
     */
    public UserRegistrationVO toUserRegistrationVO(SignupRequestDTO requestDTO, String ipAddress, String userAgent) {
        log.debug("init - toUserRegistrationVO para email: {}", requestDTO.getEmail());
        
        User user = toDomainUser(requestDTO);
        
        // En este caso, consideramos que la contraseña ya ha sido validada en el controlador
        // o que el VO realizará su propia validación interna
        UserRegistrationVO vo = new UserRegistrationVO(
                user,
                requestDTO.getPassword(),
                requestDTO.getPassword(), // Usamos el mismo valor para confirmPassword
                ipAddress,
                userAgent
        );
        
        log.debug("end - toUserRegistrationVO para email: {}", requestDTO.getEmail());
        return vo;
    }
    
    /**
     * Convierte un DTO de solicitud de registro a un objeto de dominio User
     * 
     * @param requestDTO DTO de solicitud de registro
     * @return Objeto de dominio User
     */
    public User toDomainUser(SignupRequestDTO requestDTO) {
        log.debug("init - toDomainUser para email: {}", requestDTO.getEmail());
        
        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        if (requestDTO.getPhone() != null) {
            user.setPhone(requestDTO.getPhone());
        }
        
        log.debug("end - toDomainUser para email: {}", requestDTO.getEmail());
        return user;
    }
    
    /**
     * Convierte un DTO de solicitud de login social a un Value Object de login social
     * 
     * @param requestDTO DTO de solicitud de login social
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de login social
     */
    public SocialLoginRequestVO toSocialLoginRequestVO(SocialLoginRequestDTO requestDTO, String ipAddress, String userAgent) {
        log.debug("init - toSocialLoginRequestVO para email: {}, proveedor: {}", requestDTO.getEmail(), requestDTO.getProvider());
        
        SocialLoginRequestVO vo = new SocialLoginRequestVO(
                requestDTO.getProvider(),
                requestDTO.getProviderId(),
                requestDTO.getEmail(),
                requestDTO.getName(),
                requestDTO.getPictureUrl(),
                ipAddress,
                userAgent
        );
        
        log.debug("end - toSocialLoginRequestVO para email: {}, proveedor: {}", requestDTO.getEmail(), requestDTO.getProvider());
        return vo;
    }
    
    /**
     * Crea un Value Object para la solicitud de logout
     * 
     * @param accessToken Token de acceso
     * @param refreshToken Token de refresco (puede ser null)
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de logout
     */
    public LogoutRequestVO toLogoutRequestVO(String accessToken, String refreshToken, String ipAddress, String userAgent) {
        log.debug("init - toLogoutRequestVO");
        
        // Extraer el token de acceso del encabezado Authorization (Bearer token) si es necesario
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        LogoutRequestVO vo = new LogoutRequestVO(
                accessToken,
                refreshToken,
                ipAddress,
                userAgent
        );
        
        log.debug("end - toLogoutRequestVO");
        return vo;
    }
    
    /**
     * Crea un Value Object para la solicitud de logout de todas las sesiones
     * 
     * @param userId ID del usuario
     * @return Value Object de logout all
     */
    public LogoutAllRequestVO toLogoutAllRequestVO(Long userId) {
        log.debug("init - toLogoutAllRequestVO para userId: {}", userId);
        LogoutAllRequestVO vo = new LogoutAllRequestVO(userId);
        log.debug("end - toLogoutAllRequestVO para userId: {}", userId);
        return vo;
    }
    
    /**
     * Crea un Value Object para la solicitud de renovación de token
     * 
     * @param refreshToken Token de refresco
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     * @return Value Object de renovación de token
     */
    public TokenRefreshRequestVO toTokenRefreshRequestVO(String refreshToken, String ipAddress, String userAgent) {
        log.debug("init - toTokenRefreshRequestVO");
        TokenRefreshRequestVO vo = new TokenRefreshRequestVO(
                refreshToken,
                ipAddress,
                userAgent
        );
        log.debug("end - toTokenRefreshRequestVO");
        return vo;
    }
}
