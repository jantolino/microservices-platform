package edu.market.userservice.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.market.userservice.domain.enums.AuthProviderType;
import edu.market.userservice.domain.exception.AuthenticationException;
import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.port.output.AuthDomainServicePort;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.vo.LoginMetadataVO;
import edu.market.userservice.domain.vo.SocialAuthenticationResultVO;
import edu.market.userservice.domain.vo.SocialLoginDataVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;
import edu.market.userservice.domain.vo.TokenRefreshVO;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementación del servicio de dominio para la lógica de autenticación
 */
public class AuthDomainService implements AuthDomainServicePort {

    private final Logger log = LoggerFactory.getLogger(AuthDomainService.class);    
    private final TokenDomainServicePort tokenDomainService;

    public AuthDomainService(TokenDomainServicePort tokenDomainService) {
        this.tokenDomainService = tokenDomainService;   
    }

    /**
     * Valida las credenciales de un usuario
     *
     * @param user            Usuario a validar
     * @param rawPassword     Contraseña sin codificar
     * @param passwordEncoder Codificador de contraseñas de Spring Security
     * @return true si las credenciales son válidas
     */
    @Override
    public boolean validateCredentials(User user, String rawPassword,
            PasswordEncoder passwordEncoder) {

        if (user == null || rawPassword == null || passwordEncoder == null) {
            return false;
        }

        log.debug("validateCredentials validating password for user: {}", user.getId());
        boolean isValid = passwordEncoder.matches(rawPassword, user.getPassword());

        log.info("end - validateCredentials");
        return isValid;
    }

    /**
     * Autentica a un usuario verificando sus credenciales y actualizando su último login
     *
     * @param user            Usuario a autenticar
     * @param rawPassword     Contraseña sin codificar
     * @param passwordEncoder Codificador de contraseñas
     * @return Usuario autenticado con último login actualizado
     * @throws AuthenticationException si las credenciales son inválidas
     */
    @Override
    public User authenticateUser(User user, String rawPassword, PasswordEncoder passwordEncoder) {
        log.info("Autenticando usuario: {}", user.getEmail());

        // Validar credenciales
        if (!validateCredentials(user, rawPassword, passwordEncoder)) {
            log.warn("Credenciales inválidas para el usuario: {}", user.getEmail());
            throw AuthenticationException.invalidCredentials();
        }

        // Registrar login usando el método de dominio
        user.registerLogin();
        log.info("Usuario autenticado correctamente: {}", user.getEmail());

        return user;
    }

    /**
     * Actualiza o crea un usuario a partir de datos de autenticación social
     *
     * @param existingUser    Usuario existente (puede ser null)
     * @param socialLoginData Value Object con los datos de autenticación social
     * @return Usuario actualizado o creado
     */
    @Override
    public User processUserFromSocialLogin(User existingUser, SocialLoginDataVO socialLoginData) {
        log.info("Procesando usuario desde login social");

        if (existingUser != null) {
            // Actualizar usuario existente usando el método de dominio
            log.debug("Actualizando usuario existente: {}", existingUser.getId());
            existingUser.updateFromSocialLogin(socialLoginData);
            return existingUser;
        } else {
            // Crear nuevo usuario
            log.debug("Creando nuevo usuario desde login social");
            User newUser = new User();
            
            // Usar el método de dominio para actualizar con datos sociales
            newUser.updateFromSocialLogin(socialLoginData);
            
            // Preparar para registro
            newUser.prepareForRegistration(null); // No necesitamos codificar contraseña para login social
            
            return newUser;
        }
    }

    /**
     * Crea un registro de metadatos de inicio de sesión usando un Value Object
     *
     * @param metadataVO Value Object con los datos para los metadatos de login
     * @return Metadatos de inicio de sesión
     */
    @Override
    public MetadataLogin createLoginMetadata(User user, String ipAddress, String userAgent) {
        log.debug("createLoginMetadata from VO for user: {}", user.getId());
        
        MetadataLogin metadata = new MetadataLogin();
        metadata.setUserId(user.getId());
        metadata.setIpAddress(ipAddress);
        metadata.setUserAgent(userAgent);
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setUpdatedAt(LocalDateTime.now());
        return metadata;
    }

    /**
     * Crea un registro de metadatos de inicio de sesión usando un Value Object
     *
     * @param metadataVO Value Object con los datos para los metadatos de login
     * @return Metadatos de inicio de sesión
     */
    @Override
    public MetadataLogin createLoginMetadata(LoginMetadataVO metadataVO) {
        log.debug("createLoginMetadata from VO for user: {}", metadataVO.getUser().getId());
        
        MetadataLogin metadata = new MetadataLogin();
        metadata.setUserId(metadataVO.getUser().getId());
        metadata.setIpAddress(metadataVO.getIpAddress());
        metadata.setUserAgent(metadataVO.getUserAgent());
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setUpdatedAt(LocalDateTime.now());
        
        // Establecer datos del proveedor social si están disponibles
        if (metadataVO.isSocialLogin()) {
            try {
                AuthProviderType providerType = AuthProviderType.valueOf(metadataVO.getProvider().toUpperCase());
                metadata.setProvider(providerType);
                log.debug("Establecido proveedor: {} para metadatos de login", providerType);
            } catch (IllegalArgumentException e) {
                log.warn("Provider inválido: {}", metadataVO.getProvider());
            }
            
            if (metadataVO.getProviderId() != null) {
                metadata.setProviderId(metadataVO.getProviderId());
            }
            
            if (metadataVO.getPictureUrl() != null) {
                metadata.setPictureUrl(metadataVO.getPictureUrl());
            }
        }
        
        return metadata;
    }

    /**
     * Crea un registro de metadatos de registro de usuario
     *
     * @param user      Usuario que se registra
     * @param ipAddress Dirección IP
     * @param userAgent User-Agent del navegador
     * @return Metadatos de registro
     */
    @Override
    public MetadataLogin createRegistrationMetadata(User user, String ipAddress, String userAgent) {
        log.debug("createRegistrationMetadata for user: {}", user.getId());
        MetadataLogin metadata = new MetadataLogin();
        metadata.setUserId(user.getId());
        metadata.setIpAddress(ipAddress);
        metadata.setUserAgent(userAgent);
        metadata.setCreatedAt(LocalDateTime.now());
        return metadata;
    }

    /**
     * Procesa una autenticación social, crea o actualiza el usuario y genera un token
     *
     * @param socialLoginRequest Value Object con los datos de la solicitud de autenticación social
     * @return Value Object con el resultado de la autenticación social
     */
    @Override
    public SocialAuthenticationResultVO processSocialLoginAndGenerateToken(
            SocialLoginRequestVO socialLoginRequest) {
        try {
            log.info("init - processSocialLoginAndGenerateToken");

            // Validar proveedor
            log.debug("Validando proveedor: {}", socialLoginRequest.getProvider());
            AuthProviderType providerType;
            try {
                providerType = AuthProviderType.valueOf(
                        socialLoginRequest.getProvider().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Error: Proveedor no soportado: {}", socialLoginRequest.getProvider());
                throw AuthenticationException.invalidProvider();
            }

            // Nota: La búsqueda real del usuario se realiza en el LoginUserUseCase
            // Aquí solo procesamos la información para crear o actualizar el usuario
            User user = null;
            boolean isNewUser = true; // Asumimos usuario nuevo por defecto

            // Crear el VO con los datos de autenticación social
            SocialLoginDataVO socialLoginData = new SocialLoginDataVO(
                    providerType,
                    socialLoginRequest.getEmail(),
                    socialLoginRequest.getName(),
                    socialLoginRequest.getProviderId(),
                    socialLoginRequest.getPictureUrl()
            );

            // Procesar usuario (crear nuevo o actualizar existente)
            user = processUserFromSocialLogin(null, socialLoginData);

            // Buscar token activo existente o crear uno nuevo
            UserAuthToken token;

            // Nota: En el flujo real, el usuario ya tendría un ID asignado por el repositorio
            // Aquí simulamos que el usuario ya tiene un ID para poder continuar con el flujo
            if (user.getId() == null) {
                // Esto es solo para la simulación, en la implementación real
                // el usuario ya tendría un ID asignado por el repositorio
                user.setId(1L); // ID temporal para simulación
            }

            log.debug("Buscando token existente para usuario: {} y proveedor: {}",
                    user.getId(), socialLoginRequest.getProvider());
            Optional<UserAuthToken> existingTokenOpt = tokenDomainService.findActiveSocialToken(
                    user.getId(), socialLoginRequest.getProvider());

            if (existingTokenOpt.isPresent()) {
                log.debug("Token existente encontrado, refrescando");
                UserAuthToken existingToken = existingTokenOpt.get();
                TokenRefreshVO refreshVO = new TokenRefreshVO(
                        existingToken,
                        socialLoginRequest.getIpAddress(),
                        socialLoginRequest.getUserAgent());
                token = tokenDomainService.refreshToken(refreshVO);
                isNewUser = false; // Si encontramos un token existente, no es un usuario nuevo
            } else {
                log.debug("No se encontró token existente, generando nuevo token");
                token = generateNewSocialToken(user, providerType, socialLoginRequest);
            }

            // Crear objeto resultado con el usuario, token y flag de nuevo usuario
            SocialAuthenticationResultVO result = new SocialAuthenticationResultVO(user, token,
                    isNewUser);

            log.info("end - processSocialLoginAndGenerateToken");
            return result;
        } catch (Exception e) {
            log.error("processSocialLoginAndGenerateToken error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Método auxiliar para generar un nuevo token para autenticación social
     */
    private UserAuthToken generateNewSocialToken(User user, AuthProviderType providerType,
            SocialLoginRequestVO socialLoginRequest) {

        UserAuthToken token = tokenDomainService.generateToken(user);
        token.setIpAddress(socialLoginRequest.getIpAddress());
        token.setUserAgent(socialLoginRequest.getUserAgent());
        token.setProvider(providerType);
        return token;
    }
}
