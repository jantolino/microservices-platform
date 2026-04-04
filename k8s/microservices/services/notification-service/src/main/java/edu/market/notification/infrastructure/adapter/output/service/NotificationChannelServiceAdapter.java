package edu.market.notification.infrastructure.adapter.output.service;

import edu.market.notification.domain.model.DeliveryAttempt;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.port.output.monitoring.MetricsPort;
import edu.market.notification.domain.port.output.service.NotificationChannelServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adaptador de infraestructura para el envío de notificaciones a través de canales.
 * Implementa el puerto de salida NotificationChannelServicePort.
 * 
 * Esta implementación base puede ser extendida por adaptadores específicos para
 * diferentes canales como email, SMS, push, etc.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationChannelServiceAdapter implements NotificationChannelServicePort {

    private final MetricsPort metricsPort;
    
    /**
     * Envía una notificación a través del canal implementado.
     * 
     * @param notification Notificación a enviar
     * @return Resultado del intento de entrega
     */
    @Override
    public DeliveryAttempt send(Notification notification) {
        try {
            log.info("NotificationChannelServiceAdapter.send - Enviando notificación {} por canal {}", 
                    notification.getId(), getChannelName());
            
            // No hay un método específico para intentos, usamos el contador general
            //metricsPort.incrementNotificationCounter(EventStatusType.PROCESSING);
            
            // En una implementación real, aquí se enviaría la notificación
            // a través del canal específico (email, SMS, etc.)
            
            // Simular éxito en el envío
            log.info("NotificationChannelServiceAdapter.send - Notificación {} enviada exitosamente", notification.getId());
            
            // Registrar métrica de éxito
            //metricsPort.recordNotificationSent(notification.getChannels().iterator().next());
            
            return new DeliveryAttempt.Builder()
                    .withId(UUID.randomUUID())
                    .withNotificationId(notification.getId())
                    .withChannel(notification.getChannels().iterator().next())
                    .withAttemptedAt(LocalDateTime.now())
                    .withSuccessful(true)
                    .withExternalId(UUID.randomUUID().toString())
                    .withAttemptNumber(1)
                    .build();
            
        } catch (Exception e) {
            log.error("NotificationChannelServiceAdapter.send - Error al enviar notificación {}: {}", 
                    notification.getId(), e.getMessage());
            
            // Registrar métrica de error
            metricsPort.recordNotificationFailed(notification.getChannels().iterator().next(), 
            e.getMessage());
            
            return new DeliveryAttempt.Builder()
                    .withId(UUID.randomUUID())
                    .withNotificationId(notification.getId())
                    .withChannel(notification.getChannels().iterator().next())
                    .withAttemptedAt(LocalDateTime.now())
                    .withSuccessful(false)
                    .withErrorMessage(e.getMessage())
                    .withAttemptNumber(1)
                    .build();
        }
    }
    
    /**
     * Verifica si el canal está disponible para enviar notificaciones.
     * 
     * @return true si el canal está disponible
     */
    @Override
    public boolean isAvailable() {
        log.debug("NotificationChannelServiceAdapter.isAvailable - Verificando disponibilidad del canal {}", getChannelName());
        // En una implementación real, aquí se verificaría la disponibilidad del servicio
        return true;
    }
    
    /**
     * Obtiene el nombre del canal.
     * 
     * @return Nombre del canal
     */
    @Override
    public String getChannelName() {
        return "DEFAULT";
    }
}
