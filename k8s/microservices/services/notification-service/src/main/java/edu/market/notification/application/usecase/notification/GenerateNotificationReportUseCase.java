package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.GenerateReportCommand;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.port.input.notification.GenerateNotificationReportUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.service.ReportGeneratorServicePort;
import edu.market.notification.domain.vo.NotificationFilterCriteriaVO;

import java.util.List;

/**
 * Implementación del caso de uso para generar informes de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class GenerateNotificationReportUseCase implements GenerateNotificationReportUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final ReportGeneratorServicePort reportGenerator;
    private final LoggingPort log;

    public GenerateNotificationReportUseCase(
            NotificationRepositoryPort notificationRepository,
            ReportGeneratorServicePort reportGenerator,
            LoggingPort log) {
        this.notificationRepository = notificationRepository;
        this.reportGenerator = reportGenerator;
        this.log = log;
    }

    /**
     * Genera un informe de notificaciones según los criterios especificados en el comando.
     * 
     * @param command Comando con los parámetros para generar el informe (usuario, fechas, formato)
     * @return El contenido del informe como un array de bytes
     */
    @Override
    public byte[] generateReport(GenerateReportCommand command, ClientContextCommand clientContext) {
        log.info("generateReport - Init");
        
        if (command.userId() != null) {
            log.debug("generateReport - Filtering by user ID");
        }
        
        try {
            // Crear criterios de filtrado basados en el comando
            NotificationFilterCriteriaVO filterCriteria = NotificationFilterCriteriaVO.builder()
                    .withUserId(command.userId())
                    .withStartDate(command.startDate())
                    .withEndDate(command.endDate())
                    .build();
            
            // Obtener las notificaciones que cumplen con los criterios
            log.debug("generateReport - Call notificationRepository.findByFilters");
            List<Notification> notifications = notificationRepository.findByFilters(filterCriteria);
                        
            // Generar el informe en el formato solicitado
            log.debug("generateReport - Call reportGenerator.generateReport");
            byte[] reportContent = reportGenerator.generateReport(
                    notifications, 
                    command.reportFormat(), 
                    command.startDate(), 
                    command.endDate());
            
            log.info("generateReport - End");
            return reportContent;
            
        } catch (Exception e) {
            String errorMessage = "Error generating notification report: " + e.getMessage();
            log.error("generateReport - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
