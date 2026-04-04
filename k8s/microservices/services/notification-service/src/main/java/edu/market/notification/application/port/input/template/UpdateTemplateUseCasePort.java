package edu.market.notification.application.port.input.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UpdateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;

/**
 * Puerto de caso de uso para actualizar plantillas de notificaciones existentes.
 * 
 * Este caso de uso permite modificar plantillas de notificaciones ya creadas,
 * actualizando su contenido, formato o cualquier otro atributo.
 */
public interface UpdateTemplateUseCasePort {
    
    /**
     * Actualiza una plantilla de notificación existente.
     * 
     * @param command Comando con los datos actualizados de la plantilla
     * @return Respuesta con la plantilla actualizada
     */
    TemplateResponse update(UpdateTemplateCommand command, ClientContextCommand clientContext);
}
