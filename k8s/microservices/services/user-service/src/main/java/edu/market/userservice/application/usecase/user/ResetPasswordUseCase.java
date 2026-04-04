package edu.market.userservice.application.usecase.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.market.userservice.application.port.input.ResetPasswordUseCasePort;
import edu.market.userservice.domain.event.PasswordChangedEvent;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.port.output.EventPublisherPort;
import edu.market.userservice.domain.port.output.UserDomainServicePort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.PasswordChangeVO;

/**
 * Caso de uso para restablecer la contraseña de un usuario
 */
public class ResetPasswordUseCase implements ResetPasswordUseCasePort {

    private final Logger log = LoggerFactory.getLogger(ResetPasswordUseCase.class);

    private final UserDomainServicePort userDomainService;
    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(
            UserDomainServicePort userDomainService,
            UserRepositoryPort userRepository,
            EventPublisherPort eventPublisher,
            PasswordEncoder passwordEncoder) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Restablece la contraseña de un usuario
     *
     * @param passwordChangeData Datos para el restablecimiento de contraseña
     * @return Usuario con la contraseña actualizada
     */
    @Override
    public User execute(PasswordChangeVO passwordChangeData) {
        try {
            log.info("init - execute");

            // Restablecer contraseña con validaciones de dominio
            log.debug("execute resetting password for user: {}", passwordChangeData.getUser().getId());
            User user = userDomainService.resetPassword(passwordChangeData, passwordEncoder);

            // Persistir usuario en la base de datos
            log.debug("execute saving user with reset password");
            User savedUser = userRepository.save(user);
            log.debug("execute user saved with ID: {}", savedUser.getId());

            // Publicar evento de cambio de contraseña después de la persistencia
            log.debug("execute publishing PasswordChangedEvent");
            eventPublisher.publish(new PasswordChangedEvent(
                    savedUser, 
                    passwordChangeData.getIpAddress(), 
                    passwordChangeData.getUserAgent(), 
                    true)); // true indica que es un restablecimiento de contraseña

            log.info("end - execute");
            return savedUser;
        } catch (Exception e) {
            log.error("execute error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
