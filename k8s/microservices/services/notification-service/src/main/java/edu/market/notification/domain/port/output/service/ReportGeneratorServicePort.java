package edu.market.notification.domain.port.output.service;

import edu.market.notification.domain.enums.ReportFormatType;
import edu.market.notification.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de salida para la generación de informes de notificaciones.
 * 
 * Este puerto define la interfaz que debe implementar cualquier adaptador
 * responsable de generar informes basados en datos de notificaciones.
 */
public interface ReportGeneratorServicePort {
    
    /**
     * Genera un informe en el formato especificado basado en una lista de notificaciones.
     * 
     * @param notifications Lista de notificaciones a incluir en el informe
     * @param format Formato del informe definido como enum para garantizar tipos válidos
     * @param startDate Fecha de inicio del período del informe
     * @param endDate Fecha de fin del período del informe
     * @return El contenido del informe como un array de bytes
     */
    byte[] generateReport(List<Notification> notifications, ReportFormatType format, LocalDateTime startDate, LocalDateTime endDate);
}
