package edu.market.notification.infrastructure.crosscutting;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import edu.market.notification.domain.port.output.monitoring.MetricsPort;
import edu.market.notification.infrastructure.config.properties.MetricsProperties;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Aspecto para la recolección automática de métricas técnicas.
 * Registra automáticamente métricas de latencia, throughput y errores
 * para los métodos de la capa de aplicación e infraestructura.
 * 
 * Este aspecto ejecuta de forma síncrona pero delega el registro de métricas
 * a un servicio asíncrono que implementa MetricsPort, evitando así la recursividad
 * en los aspectos y siguiendo las recomendaciones de "Microservices Patterns".
 * 
 * Implementa los patrones:
 * - Aspect-Oriented Programming: Separación de la lógica de métricas
 * - Cross-Cutting Concern: Manejo transversal de métricas
 * - Decorator: Añade funcionalidad de métricas sin modificar el código original
 * - Service Layer: Delega la implementación concreta al puerto de servicios
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsCollectionAspect {

    private final MeterRegistry meterRegistry;
    private final MetricsProperties metricsProperties;
    private final MetricsPort metricsPort;
    

    /**
     * Pointcut que define los puntos donde se aplicará la recolección de métricas.
     * Incluye todos los métodos públicos en la capa de aplicación (usecases).
     */
    @Pointcut("execution(* edu.market.notification.application..*.*(..))")
    public void applicationLayer() {}

    /**
     * Pointcut que define los puntos donde se aplicará la recolección de métricas.
     * Incluye todos los métodos públicos en los adaptadores de salida.
     */
    @Pointcut("execution(* edu.market.notification.domain..*.*(..))")
    public void domainLayer() {}

    /**
     * Pointcut que define los puntos donde se aplicará la recolección de métricas.
     * Incluye todos los métodos públicos en los controladores REST.
     */
    @Pointcut("execution(* edu.market.notification.infrastructure.adapter.input..*.*(..))" +
              " && (* edu.market.notification.infrastructure.adapter.output..*.*(..))" +
              " && !within(edu.market.notification.infrastructure.crosscutting..*)" +
              " && !within(edu.market.notification.infrastructure.adapter.output.monitoring..*)"
              )
    public void infrastructureLayer() {}

    /**
     * Registra métricas de latencia, throughput y errores para los métodos de la capa de aplicación.
     * 
     * @param joinPoint Punto de ejecución
     * @return Resultado de la ejecución del método original
     * @throws Throwable Si ocurre un error durante la ejecución
     */
    @Around("applicationLayer() || domainLayer() || infrastructureLayer()")
    public Object collectMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String metricName = buildMetricName(joinPoint);
        Timer timer = meterRegistry.timer(metricName);
        
        long startTime = System.nanoTime();
        boolean success = false;
        
        try {
            // Ejecución síncrona del método original
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable ex) {
            
            // Registrar el error de forma síncrona, pero usando un puerto asíncrono
            try {
                recordError(metricName, ex);
                log.debug("MetricsCollectionAspect - Error recorded for method {}: {}", metricName, ex.getMessage());
            } catch (Exception metricException) {
                // Capturar cualquier excepción del procesamiento de métricas
                log.warn("Failed to record error metrics for {}: {}", 
                    metricName, metricException.getMessage());
            }
            
            // Re-lanzar la excepción original para el flujo normal
            throw ex;
        } finally {
            // Calcular la duración final
            final long duration = System.nanoTime() - startTime;
            
            try {
                // Registrar todas las métricas de forma síncrona pero usando un puerto asíncrono
                timer.record(duration, TimeUnit.NANOSECONDS);
                
                // Registrar métricas adicionales
                recordThroughput(metricName, success);
                recordLatency(metricName, duration);
                
                log.debug("MetricsCollectionAspect - Method {} executed in {} ms, success: {}", 
                    metricName, TimeUnit.NANOSECONDS.toMillis(duration), success);
            } catch (Exception metricException) {
                // Evitar que las excepciones de métricas afecten al flujo principal
                log.warn("Failed to record metrics for {}: {}", 
                    metricName, metricException.getMessage());
            }
        }
    }

    /**
     * Construye un nombre de métrica preciso basado en la clase y método.
     * 
     * @param joinPoint Punto de ejecución
     * @return Nombre de la métrica
     */
    private String buildMetricName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method = signature.getMethod();
        
        String className = targetClass.getSimpleName();
        String methodName = method.getName();
        String layer = targetClass.getPackage().getName();
        
        return String.format(metricsProperties.getPrefix() 
        + "notification.%s.%s.%s", layer, className, methodName);
    }

    /**
     * Registra una métrica de error.
     * 
     * @param metricName Nombre base de la métrica
     * @param ex Excepción ocurrida
     */
    private void recordError(String metricName, Throwable ex) {
        String errorMetric = metricName + ".errors";
        String errorTypeMetric = errorMetric + "." + ex.getClass().getSimpleName();
        
        meterRegistry.counter(errorMetric).increment();
        meterRegistry.counter(errorTypeMetric).increment();
        
        // Utilizar el puerto asíncrono para registro adicional de la excepción
        // El puerto ya implementa CompletableFuture internamente
        try {
            metricsPort.incrementNotificationCounter(edu.market.notification.domain.enums.EventStatusType.FAILED);
        } catch (Exception e) {
            log.warn("Could not increment notification error counter: {}", e.getMessage());
        }
    }

    /**
     * Registra una métrica de throughput.
     * 
     * @param metricName Nombre base de la métrica
     * @param success Si la ejecución fue exitosa
     */
    private void recordThroughput(String metricName, boolean success) {
        String throughputMetric = metricName + ".throughput";
        meterRegistry.counter(throughputMetric).increment();
        
        if (success) {
            meterRegistry.counter(throughputMetric + ".success").increment();
        } else {
            meterRegistry.counter(throughputMetric + ".failure").increment();
        }
    }

    /**
     * Registra una métrica de latencia.
     * 
     * @param metricName Nombre base de la métrica
     * @param durationNanos Duración en nanosegundos
     */
    private void recordLatency(String metricName, long durationNanos) {
        String latencyMetric = metricName + ".latency";
        meterRegistry.summary(latencyMetric).record(durationNanos / 1_000_000.0); // Convertir a milisegundos
    }
}
