package edu.market.notification.application.usecase.user;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.DisableNotificationsCommand;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.port.input.user.DisableNotificationsUseCasePort;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

/**
 * Implementación del caso de uso para deshabilitar notificaciones para un usuario.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class DisableNotificationsUseCase implements DisableNotificationsUseCasePort {

    private final UserPreferenceRepositoryPort userPreferenceRepository;
    private final LoggingPort log;

    public DisableNotificationsUseCase(UserPreferenceRepositoryPort userPreferenceRepository, LoggingPort log) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.log = log;
    }

    /**
     * Deshabilita notificaciones para un usuario.
     * 
     * @param command El comando para deshabilitar notificaciones
     * @return true si las notificaciones se deshabilitaron correctamente, false en caso contrario
     * @throws ApplicationException Si ocurre algún error durante el proceso
     */
    @Override
    public boolean disable(DisableNotificationsCommand command, ClientContextCommand clientContext) {
        
        log.info("disable - Start (userId={}, eventTypes={}, reason={})", command.userId(), command.eventTypes(), command.reason());
        
        try {
            log.debug("disable - Call userPreferenceRepository.findByUserId");
            Optional<UserPreference> userPreferenceOpt = userPreferenceRepository.findByUserId(command.userId());
            
            UserPreference userPreference;
            
            // Si no existen preferencias, crear nuevas con opt-out global
            if (userPreferenceOpt.isEmpty()) {
                log.debug("disable - No existing preferences found, creating new with global opt-out");
                userPreference = UserPreference.builder()
                    .withUserId(command.userId())
                    .withGlobalOptOut(true) // Deshabilitar todas las notificaciones
                    .withEnabledChannels(Collections.emptySet())                    
                    .withCreatedAt(LocalDateTime.now())
                    .withUpdatedAt(LocalDateTime.now())
                    .build();
            } else {
                userPreference = userPreferenceOpt.get();
                log.debug("disable - Found existing user preferences");
                
                // Si eventTypes es null o vacío, deshabilitar todas las notificaciones
                if (command.eventTypes().isEmpty() || command.eventTypes() == null) {
                    log.debug("disable - Disabling all notifications (global opt-out)");
                    userPreference.setGlobalOptOut(true);
                    
                    // Registrar el motivo si se proporciona
                    if (command.reason() != null && !command.reason().isBlank()) {
                        log.debug("disable - Recording opt-out reason: {}", command.reason());
                        // Aquí podríamos agregar el motivo a los metadatos o a un campo específico
                        // si el modelo de dominio lo soporta
                    }
                } else {
                    // Deshabilitar solo los tipos de eventos específicos
                    log.debug("disable - Disabling specific event types: {}", command.eventTypes());
                    
                    // Eliminar las preferencias para los tipos de eventos especificados
                    for (String eventType : command.eventTypes()) {
                        log.debug("disable - Call unsubscribeFromEvent");
                        userPreference.unsubscribeFromEvent(eventType);
                        
                    }
                }
            }
            // Guardar las preferencias actualizadas
            log.debug("disable - Call userPreferenceRepository.save");
            userPreferenceRepository.save(userPreference);
            
            log.info("disable - End (notifications disabled successfully)");
            return true;
            
        } catch (Exception e) {
            String errorMessage = "Error disabling notifications: " + e.getMessage();
            log.error("disable - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
