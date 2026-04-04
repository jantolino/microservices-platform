package edu.market.notification.infrastructure.config;

import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.service.EventManagementServicePort;
import edu.market.notification.domain.port.output.service.NotificationCoordinationServicePort;
import edu.market.notification.domain.port.output.service.NotificationTemplateServicePort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;
import edu.market.notification.domain.service.EventManagementServiceAdapter;
import edu.market.notification.domain.service.NotificationCoordinationServiceAdapter;
import edu.market.notification.domain.service.NotificationTemplateServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de los servicios de dominio.
 * Esta clase sigue el principio de inversión de dependencias de la arquitectura hexagonal,
 * proporcionando implementaciones para los puertos de salida que el dominio necesita.
 */
@Configuration
public class DomainConfig {

    /**
     * Servicios de dominio para gestión de eventos
     */
    @Bean
    public EventManagementServicePort eventManagmentService(
            SerializationServicePort serializationServicePort,
            LoggingPort loggingPort) {
        return new EventManagementServiceAdapter(serializationServicePort, loggingPort);
    }
    
    /**
     * Servicios de dominio para gestión de plantillas
     */
    @Bean
    public NotificationTemplateServicePort notificationTemplateService(
            LoggingPort loggingPort) {
        return new NotificationTemplateServiceAdapter(loggingPort);
    }
    
    /**
     * Servicios de dominio para coordinación de notificaciones
     */
    @Bean
    public NotificationCoordinationServicePort notificationCoordinationService(
            LoggingPort loggingPort) {
        return new NotificationCoordinationServiceAdapter(loggingPort);
    }
}
