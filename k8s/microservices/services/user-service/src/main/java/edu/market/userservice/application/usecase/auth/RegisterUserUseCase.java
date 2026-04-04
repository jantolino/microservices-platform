package edu.market.userservice.application.usecase.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.application.port.input.RegisterUserUseCasePort;
import edu.market.userservice.domain.event.UserRegisteredEvent;
import edu.market.userservice.domain.port.output.AuthDomainServicePort;
import edu.market.userservice.domain.port.output.EventPublisherPort;
import edu.market.userservice.domain.port.output.MetadataLoginRepositoryPort;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.domain.port.output.UserDomainServicePort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.UserRegistrationVO;

/**
 * Caso de uso para registrar un nuevo usuario en el sistema
 */
public class RegisterUserUseCase implements RegisterUserUseCasePort {

    private final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserDomainServicePort userDomainService;
    private final AuthDomainServicePort authDomainService;
    private final TokenDomainServicePort tokenDomainService;
    private final UserRepositoryPort userRepository;
    private final UserAuthTokenRepositoryPort authTokenRepository;
    private final MetadataLoginRepositoryPort metadataLoginRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(
            UserDomainServicePort userDomainService,
            AuthDomainServicePort authDomainService,
            TokenDomainServicePort tokenDomainService,
            UserRepositoryPort userRepository,
            UserAuthTokenRepositoryPort authTokenRepository,
            MetadataLoginRepositoryPort metadataLoginRepository,
            EventPublisherPort eventPublisher,
            PasswordEncoder passwordEncoder) {
        this.userDomainService = userDomainService;
        this.authDomainService = authDomainService;
        this.tokenDomainService = tokenDomainService;
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.metadataLoginRepository = metadataLoginRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param registrationData Datos de registro del usuario
     * @return Token de autenticación generado
     */
    @Override
    public UserAuthToken execute(UserRegistrationVO registrationData) {
        try {
            log.info("init - execute");

            // Registrar usuario con validaciones de dominio
            log.debug("execute registering user with domain validations: {}",
                    registrationData.getUser().getEmail());

            // Registramos el usuario usando el VO
            User registeredUser = userDomainService.registerUser(registrationData, passwordEncoder);

            // Persistir usuario en la base de datos
            log.debug("execute saving user to database");
            User savedUser = userRepository.save(registeredUser);
            log.debug("execute user saved with ID: {}", savedUser.getId());
            
            // Publicar evento de registro después de la persistencia
            log.debug("execute publishing UserRegisteredEvent");
            // Asumimos que es un login social si viene de un proveedor externo
            // Esto debería determinarse en base a la lógica de negocio específica
            boolean isSocialLogin = false; // Por defecto asumimos registro normal
            eventPublisher.publish(new UserRegisteredEvent(savedUser, isSocialLogin));

            // Registrar metadatos de registro
            if (registrationData.getIpAddress() != null
                    && registrationData.getUserAgent() != null) {
                log.debug("execute registering metadata for user: {}", savedUser.getId());
                MetadataLogin metadata = authDomainService.createRegistrationMetadata(
                        savedUser,
                        registrationData.getIpAddress(),
                        registrationData.getUserAgent());
                metadataLoginRepository.save(metadata);
            }

            // Generar token de autenticación
            log.debug("execute generating authentication token");
            UserAuthToken token = tokenDomainService.generateToken(savedUser);
            UserAuthToken savedToken = authTokenRepository.save(token);

            log.info("end - execute");
            return savedToken;
        } catch (Exception e) {
            log.error("execute error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
