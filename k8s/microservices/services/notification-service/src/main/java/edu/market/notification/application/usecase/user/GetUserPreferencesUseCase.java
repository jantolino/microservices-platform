package edu.market.notification.application.usecase.user;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.UserPreferenceMapper;
import edu.market.notification.application.port.input.user.GetUserPreferencesUseCasePort;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para consultar preferencias de notificación del usuario.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class GetUserPreferencesUseCase implements GetUserPreferencesUseCasePort {

    private final UserPreferenceRepositoryPort userPreferenceRepository;
    private final LoggingPort log;

    public GetUserPreferencesUseCase(
            UserPreferenceRepositoryPort userPreferenceRepository,
            LoggingPort log) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.log = log;
    }

    /**
     * Recupera las preferencias de notificación para un usuario específico.
     * 
     * @param userId El identificador del usuario
     * @return Un Optional que contiene el Value Object con las preferencias del usuario si se encuentran, o vacío si no se encuentran
     */
    @Override
    public Optional<UserPreferenceResponse> get(UUID userId, ClientContextCommand clientContext) {
        log.info("get - Init for user ID: {}", userId);
        
        try {
            // Buscar las preferencias del usuario
            Optional<UserPreference> userPreferenceOpt = userPreferenceRepository.findByUserId(userId);
            
            if (userPreferenceOpt.isPresent()) {
                log.debug("get - Call userPreferenceRepository.findByUserId");                
                UserPreferenceResponse response = UserPreferenceMapper.toResponse(userPreferenceOpt.get());

                log.info("get - End (preferences found)");
                return Optional.of(response);
            } else {                
                log.info("get - End (no preferences found)");
                return Optional.empty();
            }
        } catch (Exception e) {
            String errorMessage = "Error retrieving user preferences: " + e.getMessage();
            log.error("get - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
