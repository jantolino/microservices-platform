package edu.market.notification.infrastructure.adapter.input.web.mapper;

import edu.market.notification.application.dto.request.DisableNotificationsCommand;
import edu.market.notification.application.dto.request.UpdateUserPreferencesCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.DisableNotificationsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateUserPreferencesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.UserPreferenceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir DTOs de preferencias de usuario de la capa web a comandos de la capa de aplicación
 * y viceversa. Este mapper se encarga de la conversión entre objetos de la API REST y objetos de la capa 
 * de aplicación para las operaciones relacionadas con preferencias de usuario.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInputMapper {
    
    /**
     * Convierte un DTO de solicitud de actualización de preferencias a un comando de la capa de aplicación.
     *
     * @param userId ID del usuario
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public UpdateUserPreferencesCommand toUpdateUserPreferencesCommand(String userId, UpdateUserPreferencesRequestDTO requestDTO) {
        
        log.info("toUpdateUserPreferencesCommand - init");
        
        UpdateUserPreferencesCommand command = new UpdateUserPreferencesCommand(
            UUID.fromString(userId),
            requestDTO.isGlobalOptOut(),
            requestDTO.getDisabledChannels(),
            requestDTO.getEventTypePreferences(),
            requestDTO.getTimezone(),
            requestDTO.getPreferredLanguage(),
            requestDTO.getAdditionalSettings()
        );
        
        log.info("toUpdateUserPreferencesCommand - end");
        return command;
    }
    
    /**
     * Convierte un ID de usuario a un comando para deshabilitar notificaciones.
     *
     * @param userId ID del usuario
     * @param requestDTO DTO de solicitud de la API
     * @return Comando para la capa de aplicación
     */
    public DisableNotificationsCommand toDisableNotificationsCommand(String userId, DisableNotificationsRequestDTO requestDTO) {
        
        log.info("toDisableNotificationsCommand - init");
        
        DisableNotificationsCommand command = new DisableNotificationsCommand(
            UUID.fromString(userId),
            requestDTO.getEventTypes(),
            requestDTO.getReason()
        );
        
        log.info("toDisableNotificationsCommand - end");
        return command;
    }

    /**
     * Convierte un objeto UserPreferenceResponse de la capa de aplicación a un DTO de respuesta.
     *
     * @param response Objeto de respuesta de la capa de aplicación
     * @return DTO de respuesta para la API
     */
    public UserPreferenceResponseDTO toUserPreferenceResponseDTO(UserPreferenceResponse response) {
        
        log.info("toUserPreferenceResponseDTO - init");
        
        if (response == null) {
            return null;
        }

        UserPreferenceResponseDTO dto = new UserPreferenceResponseDTO(
                response.userId(),
                response.globalOptOut(),
                response.enabledChannels(),
                response.eventTypePreferences(),
                response.createdAt(),
                response.updatedAt()
        );
        
        log.info("toUserPreferenceResponseDTO - end");
        return dto;
    }    
}
