package edu.market.notification.application.port.input.user;

import java.util.Optional;
import java.util.UUID;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.UserPreferenceResponse;

/**
 * Puerto de caso de uso para consultar preferencias de notificación del usuario.
 * 
 * Este caso de uso permite consultar las preferencias actuales de notificación
 * para un usuario específico.
 */
public interface GetUserPreferencesUseCasePort {
    
    /**
     * Recupera las preferencias de notificación para un usuario específico.
     * 
     * @param userId El identificador del usuario
     * @return Un Optional que contiene el Value Object con las preferencias del usuario si se encuentran, o vacío si no se encuentran
     */
    Optional<UserPreferenceResponse> get(UUID userId, ClientContextCommand clientContext);
}
