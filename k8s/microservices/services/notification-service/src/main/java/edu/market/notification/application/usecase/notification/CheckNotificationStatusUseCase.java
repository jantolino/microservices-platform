package edu.market.notification.application.usecase.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationStatusResponse;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.notification.CheckNotificationStatusUseCasePort;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para verificar el estado de una notificación.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class CheckNotificationStatusUseCase implements CheckNotificationStatusUseCasePort {

    private final NotificationRepositoryPort notificationRepository;
    private final LoggingPort log;

    public CheckNotificationStatusUseCase(
            NotificationRepositoryPort notificationRepository,
            LoggingPort log) {
        this.notificationRepository = notificationRepository;
        this.log = log;
    }

    /**
     * Verifica el estado actual de una notificación específica.
     * 
     * @param notificationId El identificador de la notificación a verificar
     * @return Value Object con la información del estado de la notificación
     * @throws IllegalArgumentException si la notificación no existe
     */
    @Override
    public NotificationStatusResponse check(UUID notificationId, ClientContextCommand clientContext) {
        
        log.info("check - Init for notification ID: {}", notificationId);
        
        // Buscar la notificación por ID
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isEmpty()) {
            log.warn("check - Notification not found with ID: {}", notificationId);
            throw new IllegalArgumentException("Notification not found with ID: " + notificationId);
        }
        
        Notification notification = notificationOpt.get();        
        
        // Usar el mapper para crear la respuesta con un valor máximo de reintentos predefinido
        // Este valor podría venir de una configuración
        int maxRetries = 3;
        log.debug("check - Call NotificationInputMapper.toStatusResponse");
        NotificationStatusResponse response = NotificationMapper.toStatusResponse(notification, maxRetries);
        
        log.info("check - End. Status for notification ID {}: {}", notificationId, notification.getStatus());
        return response;
    }
}
