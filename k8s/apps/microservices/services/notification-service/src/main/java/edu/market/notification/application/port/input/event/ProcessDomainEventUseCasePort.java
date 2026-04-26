package edu.market.notification.application.port.input.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.ProcessDomainEventCommand;
import edu.market.notification.application.dto.response.NotificationResponse;

/**
 * Puerto de caso de uso para procesar eventos de dominio y generar notificaciones.
 * 
 * Este caso de uso permite procesar eventos de dominio recibidos de otros servicios
 * y generar notificaciones basadas en estos eventos cuando sea apropiado.
 */
public interface ProcessDomainEventUseCasePort {
    
    /**
     * Procesa un evento de dominio y genera una notificación si es necesario.
     * 
     * @param command Comando con los datos del evento de dominio a procesar
     * @return Respuesta con la notificación generada, o null si no se generó ninguna notificación
     */
    NotificationResponse process(ProcessDomainEventCommand command, ClientContextCommand clientContext);
}
