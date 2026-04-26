package edu.market.notification.application.usecase.user;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UpdateUserPreferencesCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.UserPreferenceMapper;
import edu.market.notification.application.port.input.user.UpdateUserPreferencesUseCasePort;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;

import java.util.Optional;

/**
 * Implementación del caso de uso para actualizar preferencias de notificación por usuario.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 * 
 * Implementa el patrón Transactional Outbox para garantizar la consistencia eventual
 * entre la base de datos y el sistema de mensajería.
 */
public class UpdateUserPreferencesUseCase implements UpdateUserPreferencesUseCasePort {

    private final UserPreferenceRepositoryPort userPreferenceRepository;
    private final LoggingPort log;

    public UpdateUserPreferencesUseCase(
            UserPreferenceRepositoryPort userPreferenceRepository,
            LoggingPort log) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.log = log;
    }

    /**
     * Actualiza las preferencias de notificación de un usuario.
     * 
     * @param command Value Object con las preferencias actualizadas del usuario
     * @return Value Object con las preferencias del usuario actualizadas
     */
    @Override
    public UserPreferenceResponse update(UpdateUserPreferencesCommand command, ClientContextCommand clientContext) {
        log.info("update - Init for user ID: {}", command.userId());
        
        try {
            // Buscar las preferencias existentes del usuario
            Optional<UserPreference> existingPreferenceOpt = userPreferenceRepository.findByUserId(command.userId());
            
            UserPreference userPreference;
            
            if (existingPreferenceOpt.isPresent()) {
                log.debug("update - Call UserPreferenceOutputMapper.updateFromCommand");
                // Actualizar las preferencias existentes
                userPreference = UserPreferenceMapper.updateFromCommand(existingPreferenceOpt.get(), command);
            } else {
                log.debug("update - No existing preferences found for user ID: {}, creating new", command.userId());
                // Crear nuevas preferencias
                userPreference = new UserPreference.Builder()
                    .withUserId(command.userId())
                    .withGlobalOptOut(command.globalOptOut())
                    .build();
                
                // Aplicar las actualizaciones del comando
                log.debug("update - Call UserPreferenceOutputMapper.updateFromCommand");
                userPreference = UserPreferenceMapper.updateFromCommand(userPreference, command);
            }
            
            // Guardar las preferencias actualizadas
            log.debug("update - Call userPreferenceRepository.save");
            UserPreference savedPreference = userPreferenceRepository.save(userPreference);
            log.debug("update - Preferences saved successfully");            
            
            log.info("update - End");
            return UserPreferenceMapper.toResponse(savedPreference);
        } catch (Exception e) {
            String errorMessage = "Error updating user preferences: " + e.getMessage();
            log.error("update - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
