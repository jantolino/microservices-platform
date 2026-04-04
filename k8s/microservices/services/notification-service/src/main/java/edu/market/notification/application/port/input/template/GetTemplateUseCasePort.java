package edu.market.notification.application.port.input.template;

import java.util.List;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.GetTemplatesQuery;
import edu.market.notification.application.dto.response.TemplateResponse;

/**
 * Puerto de caso de uso para obtener plantillas de notificaciones.
 * 
 * Este caso de uso permite recuperar plantillas para su uso en la creación
 * y gestión de notificaciones.
 */
public interface GetTemplateUseCasePort {
    
    /**
     * Recupera plantillas de notificaciones según los criterios especificados.
     * 
     * @param query Consulta con los criterios de búsqueda para las plantillas
     * @return Una lista de respuestas con las plantillas que coinciden con los criterios, o una lista vacía si no se encuentran
     */
    List<TemplateResponse> getTemplates(GetTemplatesQuery query, ClientContextCommand clientContext);
}
