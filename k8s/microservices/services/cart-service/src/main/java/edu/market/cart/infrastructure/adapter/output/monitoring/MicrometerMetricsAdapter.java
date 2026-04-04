package edu.market.notification.infrastructure.adapter.output.monitoring;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.port.output.monitoring.MetricsPort;
import edu.market.notification.infrastructure.config.properties.MetricsProperties;
import edu.market.notification.domain.enums.NotificationChannelType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Implementación asíncrona de MetricsPort usando Micrometer como backend de métricas.
 * Utiliza CompletableFuture con hilos virtuales para operaciones asíncronas sin bloquear el hilo principal.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MicrometerMetricsAdapter implements MetricsPort {

    private final MeterRegistry meterRegistry;
    private final MetricsProperties metricsProperties;
    
    @Qualifier("metricsCollectorExecutor")
    private final Executor metricsCollectorExecutor;

    @Override
    public CompletableFuture<Void> recordNotificationSent(NotificationChannelType channel) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Registrando métrica de notificación enviada para canal {}", channel);
                Counter.builder(metricsProperties.getPrefix() + "notification.sent")
                       .tag("channel", channel.name())
                       .register(meterRegistry)
                       .increment();
                log.debug("Métrica de notificación enviada registrada exitosamente");
            } catch (Exception e) {
                log.error("Error al registrar métrica de notificación enviada: {}", e.getMessage(), e);
            }
        }, metricsCollectorExecutor);
    }

    @Override
    public CompletableFuture<Void> recordNotificationFailed(NotificationChannelType channel,
            String errorType) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Registrando métrica de notificación fallida para canal {} con error {}", channel, errorType);
                Counter.builder(metricsProperties.getPrefix() + "notification.failed")
                       .tags("channel", channel.name(), "error", errorType)
                       .register(meterRegistry)
                       .increment();
                log.debug("Métrica de notificación fallida registrada exitosamente");
            } catch (Exception e) {
                log.error("Error al registrar métrica de notificación fallida: {}", e.getMessage(), e);
            }
        }, metricsCollectorExecutor);
    }

    @Override
    public CompletableFuture<Void> recordDeliveryTime(NotificationChannelType channel,
            Duration duration) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Registrando tiempo de entrega para canal {} con duración {}", channel, duration);
                Timer.builder(metricsProperties.getPrefix() + "notification.delivery.time")
                     .tag("channel", channel.name())
                     .register(meterRegistry)
                     .record(duration.toMillis(), TimeUnit.MILLISECONDS);
                log.debug("Tiempo de entrega registrado exitosamente");
            } catch (Exception e) {
                log.error("Error al registrar tiempo de entrega: {}", e.getMessage(), e);
            }
        }, metricsCollectorExecutor);
    }

    @Override
    public CompletableFuture<Void> incrementNotificationCounter(EventStatusType status) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Incrementando contador de notificaciones para estado {}", status);
                Counter.builder(metricsProperties.getPrefix() + "notification.count")
                       .tag("status", status.name())
                       .register(meterRegistry)
                       .increment();
                log.debug("Contador de notificaciones incrementado exitosamente");
            } catch (Exception e) {
                log.error("Error al incrementar contador de notificaciones: {}", e.getMessage(), e);
            }
        }, metricsCollectorExecutor);
    }
}
