package edu.market.notification.application.port.input.template;

import edu.market.notification.application.dto.request.ClientContextCommand;

/**
 * Puerto de caso de uso para eliminar plantillas de notificaciones.
 * 
 * Este caso de uso permite eliminar plantillas de notificaciones que ya no son necesarias
 * o que han quedado obsoletas.
 */
public interface DeleteTemplateUseCasePort {
    
    /**
     * Elimina una plantilla de notificación existente.
     * 
     * @param templateId El identificador de la plantilla a eliminar
     * @return true si la plantilla fue eliminada correctamente, false en caso contrario
     */
    boolean delete(String templateId, ClientContextCommand clientContext);
}
