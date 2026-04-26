package edu.market.userservice.application.usecase.auth;


import edu.market.userservice.application.port.input.LoginUserUseCasePort;
import edu.market.userservice.domain.exception.AuthenticationException;
import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.port.output.AuthDomainServicePort;
import edu.market.userservice.domain.port.output.MetadataLoginRepositoryPort;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.LoginCredentialsVO;
import edu.market.userservice.domain.vo.LoginMetadataVO;
import edu.market.userservice.domain.vo.SocialAuthenticationResultVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Caso de uso para autenticar a un usuario con email y contraseña
 */
public class LoginUserUseCase implements LoginUserUseCasePort {
    

    private final Logger log = LoggerFactory.getLogger(LoginUserUseCase.class);
    
    private final AuthDomainServicePort authDomainService;
    private final TokenDomainServicePort tokenDomainService;
    private final UserRepositoryPort userRepository;
    private final UserAuthTokenRepositoryPort authTokenRepositoryPort;
    private final UserAuthTokenRepositoryPort userAuthTokenRepository;
    private final MetadataLoginRepositoryPort metadataLoginRepository;
    private final PasswordEncoder passwordEncoder;    
  
    
    public LoginUserUseCase(
            AuthDomainServicePort authDomainService,
            TokenDomainServicePort tokenDomainService,
            UserRepositoryPort userRepository,
            UserAuthTokenRepositoryPort authTokenRepositoryPort,
            MetadataLoginRepositoryPort metadataLoginRepository,
            PasswordEncoder passwordEncoder,
            UserAuthTokenRepositoryPort userAuthTokenRepository) {
        this.authDomainService = authDomainService;
        this.tokenDomainService = tokenDomainService;
        this.userRepository = userRepository;
        this.authTokenRepositoryPort = authTokenRepositoryPort;
        this.metadataLoginRepository = metadataLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAuthTokenRepository = userAuthTokenRepository;
    }
    
    /**
     * Autentica a un usuario con sus credenciales
     * 
     * @param credentials Credenciales de inicio de sesión
     * @return Token de autenticación generado
     */
    @Override
    public UserAuthToken execute(LoginCredentialsVO credentials) {
        try {
            log.info("init - execute login with credentials");
            
            // Buscar usuario por email
            log.debug("execute find user by email: {}", credentials.getEmail());
            Optional<User> userOpt = userRepository.findByEmail(credentials.getEmail());
            
            if (userOpt.isEmpty()) {
                log.error("execute error: Usuario no encontrado con email: {}", credentials.getEmail());
                throw AuthenticationException.invalidCredentials();
            }
            
            User user = userOpt.get();
            log.debug("execute user found with ID: {}", user.getId());
            
            // Validar credenciales usando el servicio de dominio
            log.debug("execute validating credentials for user: {}", user.getId());
            if (!authDomainService.validateCredentials(user, credentials.getPassword(), passwordEncoder)) {
                log.error("execute error: Contraseña incorrecta para el usuario: {}", credentials.getEmail());
                throw AuthenticationException.invalidCredentials();
            }
            
            // Actualizar último login
            log.debug("execute updating last login for user: {}", user.getId());
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
            
            // Crear metadatos de inicio de sesión usando el servicio de dominio
            log.debug("execute creating login metadata for user: {}", user.getId());
            MetadataLogin metadata = authDomainService.createLoginMetadata(
                user, credentials.getIpAddress(), credentials.getUserAgent());
            metadataLoginRepository.save(metadata);
            
            // Generar token de autenticación usando el servicio de dominio
            log.debug("execute generating authentication token for user: {}", user.getId());
            UserAuthToken token = tokenDomainService.generateToken(user);
            UserAuthToken savedToken = authTokenRepositoryPort.save(token);
            
            log.info("end - execute login with credentials");
            return savedToken;
        } catch (Exception e) {
            log.error("execute error: {}", e.getMessage(), e);
            if (e instanceof AuthenticationException) {
                throw e;
            } else {
                // Utilizamos un código de error genérico para errores de autenticación no específicos
                throw new AuthenticationException("Error durante el proceso de autenticación", 
                        AuthenticationException.CODE_INVALID_CREDENTIALS);
            }
        }
    }

    /**
     * Procesa una autenticación mediante proveedor social
     *
     * @param socialLoginRequest Value Object con los datos de la solicitud de autenticación social
     * @return Token de autenticación
     */
    @Override
    public UserAuthToken execute(SocialLoginRequestVO socialLoginRequest) {
        try {
            log.info("init - execute social login");
            
            // Usar el servicio de dominio para procesar la autenticación social y generar el token
            log.debug("execute delegating social login to domain service");
            SocialAuthenticationResultVO result = authDomainService.processSocialLoginAndGenerateToken(
                    socialLoginRequest);
            
            // Obtener el usuario y el token del resultado
            User user = result.getUser();
            UserAuthToken token = result.getToken();
            boolean isNewUser = result.isNewUser();
            
            // Guardar el usuario si es necesario (nuevo o actualizado)
            log.debug("execute saving user: {}", user.getId());
            user = userRepository.save(user);
            
            // Crear y guardar metadata de login
            if (isNewUser) {
                log.debug("execute creating registration metadata for new user: {}", user.getId());
                // Crear LoginMetadataVO para los metadatos de registro
                LoginMetadataVO registrationMetadataVO = new LoginMetadataVO(
                        user, 
                        socialLoginRequest.getIpAddress(), 
                        socialLoginRequest.getUserAgent(),
                        socialLoginRequest.getProvider(),
                        socialLoginRequest.getProviderId(),
                        socialLoginRequest.getPictureUrl());
                
                // Usar el nuevo método con el VO
                MetadataLogin registrationMetadata = authDomainService.createLoginMetadata(registrationMetadataVO);
                metadataLoginRepository.save(registrationMetadata);
            }
            
            // Crear y guardar metadata de login (siempre, incluso para usuarios existentes)
            log.debug("execute creating login metadata for user: {}", user.getId());
            
            // Crear LoginMetadataVO para los metadatos de login
            LoginMetadataVO loginMetadataVO = new LoginMetadataVO(
                    user, 
                    socialLoginRequest.getIpAddress(), 
                    socialLoginRequest.getUserAgent(),
                    socialLoginRequest.getProvider(),
                    socialLoginRequest.getProviderId(),
                    socialLoginRequest.getPictureUrl());
            
            // Usar el nuevo método con el VO
            MetadataLogin loginMetadata = authDomainService.createLoginMetadata(loginMetadataVO);
            metadataLoginRepository.save(loginMetadata);
            
            // Guardar el token
            log.debug("execute saving token for user: {}", user.getId());
            UserAuthToken savedToken = userAuthTokenRepository.save(token);
            
            log.info("end - execute social login with {}", isNewUser ? "new user" : "existing user");
            return savedToken;
        } catch (Exception e) {
            log.error("execute social login error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
