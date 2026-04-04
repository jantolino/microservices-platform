package edu.market.notification.application.port.input.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.SubscribeToEventsCommand;

/**
 * Puerto de caso de uso para suscribirse a eventos de otros microservicios.
 * 
 * Este caso de uso permite que el servicio de notificaciones se suscriba dinámicamente a
 * eventos de otras partes del sistema para generar notificaciones basadas en estos eventos.
 */
public interface SubscribeToEventsUseCasePort {
    
    /**
     * Suscribe el servicio a un tipo de evento específico de otro servicio.
     * 
     * @param command El comando con los datos para la suscripción
     * @return true si la suscripción fue exitosa, false en caso contrario
     */
    boolean subscribe(SubscribeToEventsCommand command, ClientContextCommand clientContext);
}
