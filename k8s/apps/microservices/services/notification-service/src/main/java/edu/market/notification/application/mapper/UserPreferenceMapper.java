package edu.market.notification.application.mapper;

import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.application.dto.request.UpdateUserPreferencesCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;
import edu.market.notification.domain.enums.NotificationChannelType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio UserPreference y objetos de valor relacionados.
 * Siguiendo el patrón de arquitectura hexagonal, este mapper pertenece a la capa de aplicación
 * y se encarga de transformar objetos del dominio a objetos de la capa de aplicación.
 */
public class UserPreferenceMapper {

    /**
     * Convierte una entidad UserPreference a un objeto UserPreferenceResponse
     * 
     * @param userPreference La entidad de dominio a convertir
     * @return El objeto UserPreferenceResponse correspondiente
     */
    public static UserPreferenceResponse toResponse(UserPreference userPreference) {
        if (userPreference == null) {
            return null;
        }
        
        // Convertir las preferencias de eventos a un formato adecuado para la respuesta
        Map<String, Set<String>> eventTypePreferences = new HashMap<>();
        userPreference.getEventPreferences().forEach((eventType, channels) -> {
            Set<String> channelNames = channels.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
            eventTypePreferences.put(eventType, channelNames);
        });
        
        // Obtener los canales habilitados
        Set<NotificationChannelType> enabledChannels = userPreference.getEnabledChannels();
        
        return new UserPreferenceResponse(
            userPreference.getUserId(),
            userPreference.isGlobalOptOut(),
            enabledChannels,
            eventTypePreferences,
            userPreference.getCreatedAt(),
            userPreference.getUpdatedAt()
        );
    }
    
    /**
     * Actualiza una entidad UserPreference existente con los datos de un comando
     * 
     * @param existingPreference La entidad de dominio a actualizar
     * @param command El comando con los datos de actualización
     * @return La entidad UserPreference actualizada
     */
    public static UserPreference updateFromCommand(UserPreference existingPreference, UpdateUserPreferencesCommand command) {
        // Actualizar la preferencia global de opt-out
        existingPreference.setGlobalOptOut(command.globalOptOut());
        
        // Procesar los canales deshabilitados
        if (command.disabledChannels() != null) {
            // Obtener todos los canales posibles
            Set<NotificationChannelType> allChannels = new HashSet<>();
            for (NotificationChannelType channel : NotificationChannelType.values()) {
                allChannels.add(channel);
            }
            
            // Calcular los canales habilitados como la diferencia entre todos los canales y los deshabilitados
            Set<NotificationChannelType> enabledChannels = new HashSet<>(allChannels);
            enabledChannels.removeAll(command.disabledChannels());
            
            // Actualizar las preferencias de eventos según los canales habilitados
            if (command.eventTypePreferences() != null) {
                command.eventTypePreferences().forEach((eventType, eventChannels) -> {
                    // Limpiar las preferencias existentes para este tipo de evento
                    existingPreference.unsubscribeFromEvent(eventType);
                    
                    // Añadir las nuevas preferencias, pero solo para canales habilitados
                    if (eventChannels != null) {
                        for (String channelName : eventChannels) {
                            try {
                                NotificationChannelType channel = NotificationChannelType.valueOf(channelName);
                                if (enabledChannels.contains(channel)) {
                                    existingPreference.subscribeToEventViaChannel(eventType, channel);
                                }
                            } catch (IllegalArgumentException e) {
                                // Ignorar canales inválidos
                            }
                        }
                    }
                });
            }
        }
        
        return existingPreference;
    }
}
