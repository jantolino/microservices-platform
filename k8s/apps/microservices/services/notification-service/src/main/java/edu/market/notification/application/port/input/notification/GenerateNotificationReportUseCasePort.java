package edu.market.notification.application.port.input.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.GenerateReportCommand;

/**
 * Puerto de caso de uso para generar informes de notificaciones.
 * 
 * Este caso de uso permite generar informes analíticos sobre las notificaciones
 * enviadas a través del sistema, apoyando la inteligencia de negocio y el monitoreo.
 */
public interface GenerateNotificationReportUseCasePort {
    
    /**
     * Genera un informe de notificaciones según los criterios especificados en el comando.
     * 
     * @param command Comando con los parámetros para generar el informe (usuario, fechas, formato)
     * @return El contenido del informe como un array de bytes
     */
    byte[] generateReport(GenerateReportCommand command, ClientContextCommand clientContext);
}
