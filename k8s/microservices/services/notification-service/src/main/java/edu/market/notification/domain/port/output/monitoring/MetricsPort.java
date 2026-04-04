package edu.market.notification.domain.port.output.monitoring;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.EventStatusType;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida para el registro de métricas específicas del dominio de notificaciones.
 * Contiene solo las métricas que no pueden ser implementadas directamente con Micrometer/Prometheus.
 * 
 * Para métricas técnicas generales (circuit breakers, health checks, etc.) se recomienda usar
 * directamente Micrometer con anotaciones AOP.
 * 
 * Los métodos son asíncronos para evitar bloquear el flujo principal de la aplicación,
 * devolviendo CompletableFuture que pueden ser combinados o ignorados según sea necesario.
 */
public interface MetricsPort {
    
    /**
     * Registra una notificación enviada exitosamente.
     * Métrica específica del dominio que registra el éxito por canal.
     * 
     * @param channel Canal utilizado para enviar la notificación
     * @return CompletableFuture<Void> que se completa cuando la métrica se ha registrado
     */
    CompletableFuture<Void> recordNotificationSent(NotificationChannelType channel);
    
    /**
     * Registra una notificación fallida con su tipo de error.
     * Métrica específica del dominio que permite análisis detallado de fallos.
     * 
     * @param channel Canal utilizado para el intento de envío
     * @param error Tipo específico de error que ocurrió
     * @return CompletableFuture<Void> que se completa cuando la métrica se ha registrado
     */
    CompletableFuture<Void> recordNotificationFailed(NotificationChannelType channel, String error);
    
    /**
     * Registra el tiempo de entrega de una notificación.
     * Permite medir la latencia específica por canal de notificación.
     * 
     * @param channel Canal utilizado para la notificación
     * @param duration Duración total del proceso de entrega
     * @return CompletableFuture<Void> que se completa cuando la métrica se ha registrado
     */
    CompletableFuture<Void> recordDeliveryTime(NotificationChannelType channel, Duration duration);
    
    /**
     * Incrementa el contador de notificaciones según su estado.
     * Permite realizar análisis de tendencias y volúmenes.
     * 
     * @param status Estado de la notificación (enviada, fallida, programada, etc.)
     * @return CompletableFuture<Void> que se completa cuando la métrica se ha registrado
     */
    CompletableFuture<Void> incrementNotificationCounter(EventStatusType status);
}
