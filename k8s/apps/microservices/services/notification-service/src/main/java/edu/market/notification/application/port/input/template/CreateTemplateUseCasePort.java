package edu.market.notification.application.port.input.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.CreateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;

/**
 * Puerto de caso de uso para crear plantillas de notificaciones.
 * 
 * Este caso de uso permite la creación de plantillas para diferentes tipos de notificaciones,
 * facilitando la generación de contenido consistente para las notificaciones.
 */
public interface CreateTemplateUseCasePort {
    
    /**
     * Crea una nueva plantilla de notificación.
     * 
     * @param command Comando con los datos de la plantilla a crear
     * @return Respuesta con la plantilla creada y su identificador asignado
     */
    TemplateResponse create(CreateTemplateCommand command, ClientContextCommand clientContext);
}
