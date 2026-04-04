package edu.market.notification.application.port.input.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UnsubscribeFromEventsCommand;

/**
 * Puerto de caso de uso para desuscribirse de eventos de otros microservicios.
 * 
 * Este caso de uso permite que el servicio de notificaciones cancele dinámicamente la suscripción a
 * eventos de otras partes del sistema cuando ya no se necesitan para generar notificaciones.
 */
public interface UnsubscribeFromEventsUseCasePort {
    
    /**
     * Cancela la suscripción del servicio a un tipo de evento específico de otro servicio.
     * 
     * @param command El comando con los datos para la desuscripción
     * @return true si la desuscripción fue exitosa, false en caso contrario
     */
    boolean unsubscribe(UnsubscribeFromEventsCommand command, ClientContextCommand clientContext);
}
