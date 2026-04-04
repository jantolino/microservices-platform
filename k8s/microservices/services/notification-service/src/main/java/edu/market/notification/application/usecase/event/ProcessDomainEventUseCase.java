package edu.market.notification.application.usecase.event;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.request.ProcessDomainEventCommand;
import edu.market.notification.application.dto.response.NotificationResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.NotificationMapper;
import edu.market.notification.application.port.input.event.ProcessDomainEventUseCasePort;
import edu.market.notification.application.port.input.notification.SendNotificationUseCasePort;
import edu.market.notification.domain.model.EventSubscription;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.EventSubscriptionRepositoryPort;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para procesar eventos de dominio y generar notificaciones.
 * 
 * Este caso de uso se encarga de:
 * 1. Recibir eventos de dominio de otros servicios
 * 2. Verificar si hay suscriptores para ese tipo de evento
 * 3. Verificar las preferencias de notificación de cada suscriptor
 * 4. Generar y enviar notificaciones según corresponda
 */
public class ProcessDomainEventUseCase implements ProcessDomainEventUseCasePort {

    private final EventSubscriptionRepositoryPort eventSubscriptionRepository;
    private final UserPreferenceRepositoryPort userPreferenceRepository;
    private final SendNotificationUseCasePort sendNotificationUseCase;
    private final LoggingPort log;

    public ProcessDomainEventUseCase(
            EventSubscriptionRepositoryPort eventSubscriptionRepository,
            UserPreferenceRepositoryPort userPreferenceRepository,
            SendNotificationUseCasePort sendNotificationUseCase,
            LoggingPort log) {
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.log = log;
    }

    /**
     * Procesa un evento de dominio y genera notificaciones si corresponde.
     */
    @Override
    public NotificationResponse process(ProcessDomainEventCommand command, ClientContextCommand clientContext) {
        
        log.info("process - Init");
        
        try {
            // Buscar suscripciones activas para este tipo de evento
            log.debug("process - Call eventSubscriptionRepository.findByEventType");
            List<EventSubscription> subscriptions = eventSubscriptionRepository.findByEventType(command.eventType());
            
            if (subscriptions.isEmpty()) {
                log.debug("process - No active subscriptions found");
                return null; // No hay suscripciones, no se puede procesar el evento
            }
            
            log.debug("process - Found {} subscriptions", subscriptions.size());
            
            // Por cada suscripción, verificar preferencias de usuario y enviar notificación si corresponde
            for (EventSubscription subscription : subscriptions) {
                // Nota: EventSubscription no tiene un campo userId, así que asumimos que el ID de la suscripción
                // está relacionado con el usuario. En un caso real, la entidad EventSubscription debería tener este campo.
                UUID userId = subscription.getId();
                
                // Verificar preferencias del usuario
                log.debug("process - Call userPreferenceRepository.findByUserId");
                Optional<UserPreference> userPreferenceOpt = userPreferenceRepository.findByUserId(userId);
                
                if (userPreferenceOpt.isEmpty()) {
                    log.debug("process - No preferences found for user");
                    // Si no hay preferencias, usar configuración por defecto (enviar notificación)
                    return sendNotificationForEvent(command, userId, clientContext);
                }
                
                UserPreference preference = userPreferenceOpt.get();
                
                // Verificar si el usuario tiene habilitadas las notificaciones para este tipo de evento
                log.debug("process - Checking user subscription to event");
                if (preference.isSubscribedToEvent(command.eventType())) {
                    log.debug("process - User has enabled notifications");
                    return sendNotificationForEvent(command, userId, clientContext);
                } else {
                    log.debug("process - User has disabled notifications");
                }
            }
            
            log.info("process - End");
            return null;
            
        } catch (Exception e) {
            String errorMessage = "Error processing domain event: " + e.getMessage();
            log.error("process - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
    
    /**
     * Envía una notificación basada en un evento de dominio.
     * 
     * @param command El comando con los datos del evento
     * @param userId El ID del usuario destinatario
     * @return La respuesta de la notificación enviada
     */
    private NotificationResponse sendNotificationForEvent(ProcessDomainEventCommand command, UUID userId, ClientContextCommand clientContext) {
        log.info("sendNotificationForEvent - Init");
        
        // Crear un comando de notificación basado en el evento
        log.debug("sendNotificationForEvent - Call NotificationInputMapper.fromDomainEvent");
        NotificationCommand notificationCommand = NotificationMapper.fromDomainEvent(command, userId);
        
        // Enviar la notificación
        log.debug("sendNotificationForEvent - Call sendNotificationUseCase.send");
        NotificationResponse response = sendNotificationUseCase.send(notificationCommand, clientContext);
        
        log.debug("sendNotificationForEvent - Notification created");
        log.info("sendNotificationForEvent - End");
        return response;
    }
}
