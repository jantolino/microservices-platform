package edu.market.userservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.market.userservice.application.port.input.ChangePasswordUseCasePort;
import edu.market.userservice.application.port.input.DeleteUserUseCasePort;
import edu.market.userservice.application.port.input.GetUserUseCasePort;
import edu.market.userservice.application.port.input.LoginUserUseCasePort;
import edu.market.userservice.application.port.input.LogoutUserUseCasePort;
import edu.market.userservice.application.port.input.RefreshTokenUseCasePort;
import edu.market.userservice.application.port.input.RegisterUserUseCasePort;
import edu.market.userservice.application.port.input.ResetPasswordUseCasePort;
import edu.market.userservice.application.port.input.UpdateUserProfileUseCasePort;
import edu.market.userservice.application.usecase.auth.LoginUserUseCase;
import edu.market.userservice.application.usecase.auth.LogoutUserUseCase;
import edu.market.userservice.application.usecase.auth.RefreshTokenUseCase;
import edu.market.userservice.application.usecase.auth.RegisterUserUseCase;
import edu.market.userservice.application.usecase.user.ChangePasswordUseCase;
import edu.market.userservice.application.usecase.user.DeleteUserUseCase;
import edu.market.userservice.application.usecase.user.GetUserUseCase;
import edu.market.userservice.application.usecase.user.ResetPasswordUseCase;
import edu.market.userservice.application.usecase.user.UpdateUserProfileUseCase;
import edu.market.userservice.domain.port.output.AuthDomainServicePort;
import edu.market.userservice.domain.port.output.EventPublisherPort;
import edu.market.userservice.domain.port.output.MetadataLoginRepositoryPort;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.domain.port.output.UserDomainServicePort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.service.AuthDomainService;
import edu.market.userservice.domain.service.TokenDomainService;
import edu.market.userservice.domain.service.UserDomainService;

/**
 * Configuración para registrar los casos de uso como beans
 */
@Configuration
public class BeansConfig {

    /**
     * Registra el servicio de dominio de autenticación
     */
    @Bean
    public AuthDomainServicePort authDomainService(TokenDomainServicePort tokenDomainService) {
        return new AuthDomainService(tokenDomainService);
    }

    /**
     * Registra el servicio de dominio de tokens
     */
    @Bean
    public TokenDomainServicePort tokenDomainService(TokenConfig tokenConfig,
            UserAuthTokenRepositoryPort userAuthTokenRepository) {
        return new TokenDomainService(tokenConfig, userAuthTokenRepository);
    }

    /**
     * Registra el servicio de dominio de usuarios
     */
    @Bean
    public UserDomainServicePort userDomainService() {
        // Ya no necesita el EventPublisherPort, se trasladó a los casos de uso
        return new UserDomainService();
    }

    /**
     * Registra el caso de uso de login de usuario
     */
    @Bean
    public LoginUserUseCasePort loginUserUseCase(AuthDomainServicePort authDomainService,
            TokenDomainServicePort tokenDomainService, UserRepositoryPort userRepository,
            UserAuthTokenRepositoryPort authTokenRepository,
            MetadataLoginRepositoryPort metadataLoginRepository, PasswordEncoder passwordEncoder) {
        return new LoginUserUseCase(authDomainService, tokenDomainService, userRepository,
                authTokenRepository, metadataLoginRepository, passwordEncoder, authTokenRepository);
    }

    /**
     * Registra el caso de uso de cambio de contraseña
     */
    @Bean
    public ChangePasswordUseCasePort changePasswordUseCase(UserDomainServicePort userDomainService,
                                                          UserRepositoryPort userRepository,
                                                          EventPublisherPort eventPublisher,
                                                          PasswordEncoder passwordEncoder) {
        return new ChangePasswordUseCase(
                userDomainService, userRepository, eventPublisher, passwordEncoder);
    }

    /**
     * Registra el caso de uso de restablecimiento de contraseña
     */
    @Bean
    public ResetPasswordUseCasePort resetPasswordUseCase(UserDomainServicePort userDomainService,
                                                        UserRepositoryPort userRepository,
                                                        EventPublisherPort eventPublisher,
                                                        PasswordEncoder passwordEncoder) {
        return new ResetPasswordUseCase(
                userDomainService, userRepository, eventPublisher, passwordEncoder);
    }

    /**
     * Registra el caso de uso de registro de usuario
     */
    @Bean
    public RegisterUserUseCasePort registerUserUseCase(UserDomainServicePort userDomainService,
            AuthDomainServicePort authDomainService, TokenDomainServicePort tokenDomainService,
            UserRepositoryPort userRepository, UserAuthTokenRepositoryPort authTokenRepository,
            MetadataLoginRepositoryPort metadataLoginRepository, EventPublisherPort eventPublisher,
            PasswordEncoder passwordEncoder) {
        return new RegisterUserUseCase(userDomainService, authDomainService, tokenDomainService,
                userRepository, authTokenRepository, metadataLoginRepository, eventPublisher, passwordEncoder);
    }

    /**
     * Registra el caso de uso de cierre de sesión
     */
    @Bean
    public LogoutUserUseCasePort logoutUserUseCase(TokenDomainServicePort tokenDomainService,
            UserRepositoryPort userRepository) {
        return new LogoutUserUseCase(tokenDomainService, userRepository);
    }

    /**
     * Registra el caso de uso de refresco de token
     */
    @Bean
    public RefreshTokenUseCasePort refreshTokenUseCase(
            UserAuthTokenRepositoryPort userAuthTokenRepository,
            TokenDomainServicePort tokenDomainService) {
        return new RefreshTokenUseCase(userAuthTokenRepository, tokenDomainService);
    }

    /**
     * Registra el caso de uso de obtención de usuario
     */
    @Bean
    public GetUserUseCasePort getUserUseCase(UserRepositoryPort userRepository) {
        return new GetUserUseCase(userRepository);
    }

    /**
     * Registra el caso de uso de actualización de perfil de usuario
     */
    @Bean
    public UpdateUserProfileUseCasePort updateUserProfileUseCase(
            UserDomainServicePort userDomainService, UserRepositoryPort userRepository) {
        return new UpdateUserProfileUseCase(userDomainService, userRepository);
    }

    /**
     * Registra el caso de uso de eliminación de usuario
     */
    @Bean
    public DeleteUserUseCasePort deleteUserUseCase(UserRepositoryPort userRepository) {
        return new DeleteUserUseCase(userRepository);
    }
}
