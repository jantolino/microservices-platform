package edu.market.notification.infrastructure.crosscutting;

import edu.market.notification.domain.port.output.monitoring.ExceptionTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto para el manejo de excepciones en toda la aplicación.
 * Este aspecto captura las excepciones lanzadas por los métodos en la aplicación
 * y las reporta al sistema de seguimiento de excepciones.
 * 
 * Este aspecto ejecuta de forma síncrona pero delega el registro de excepciones
 * a un servicio asíncrono que implementa ExceptionTrackerPort, evitando así la recursividad
 * en los aspectos y siguiendo las recomendaciones de "Microservices Patterns".
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionTrackerAspect {

    private final ExceptionTrackerPort exceptionTrackerPort;
    
    /**
     * Pointcut que define los métodos en la capa de aplicación.
     */
    @Pointcut("execution(* edu.market.notification.application..*.*(..))")    
    public void applicationLayer() {}
    
    /**
     * Pointcut que define los métodos en la capa de dominio.
     */
    @Pointcut("execution(* edu.market.notification.domain..*.*(..))")    
    public void domainLayer() {}
    
    /**
     * Pointcut que define los métodos en la capa de infraestructura,
     * excluyendo los aspectos crosscutting y los adaptadores de monitoreo
     * para evitar recursividad.
     */
    @Pointcut("execution(* edu.market.notification.infrastructure.adapter.input..*.*(..))" +
              " && (* edu.market.notification.infrastructure.adapter.output..*.*(..))" +
              " && !within(edu.market.notification.infrastructure.crosscutting..*)" +
              " && !within(edu.market.notification.infrastructure.adapter.output.monitoring..*)"
              )
    public void infrastructureLayer() {}
    
    /**
     * Captura excepciones en la capa de aplicación.
     *
     * @param joinPoint El punto de unión donde ocurrió la excepción
     * @param exception La excepción capturada
     */
    @AfterThrowing(pointcut = "applicationLayer()", throwing = "exception")
    public void handleApplicationException(JoinPoint joinPoint, Exception exception) {
        trackException(joinPoint, exception, "application");
    }
    
    /**
     * Captura excepciones en la capa de dominio.
     *
     * @param joinPoint El punto de unión donde ocurrió la excepción
     * @param exception La excepción capturada
     */
    @AfterThrowing(pointcut = "domainLayer()", throwing = "exception")
    public void handleDomainException(JoinPoint joinPoint, Exception exception) {
        trackException(joinPoint, exception, "domain");
    }
    
    /**
     * Captura excepciones en la capa de infraestructura.
     *
     * @param joinPoint El punto de unión donde ocurrió la excepción
     * @param exception La excepción capturada
     */
    @AfterThrowing(pointcut = "infrastructureLayer()", throwing = "exception")
    public void handleInfrastructureException(JoinPoint joinPoint, Exception exception) {
        trackException(joinPoint, exception, "infrastructure");
    }
    
    /**
     * Método común para registrar excepciones en el sistema de seguimiento.
     *
     * @param joinPoint El punto de unión donde ocurrió la excepción
     * @param exception La excepción capturada
     * @param layer La capa arquitectónica donde ocurrió la excepción
     */
    private void trackException(JoinPoint joinPoint, Exception exception, String layer) {
        
        log.debug("trackException - Init");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;
        String errorType = exception.getClass().getSimpleName();
        
        // Preparar información de contexto para el seguimiento de excepciones
        Map<String, String> context = new HashMap<>();
        context.put("layer", layer);
        context.put("class", className);
        context.put("method", methodName);
        context.put("errorType", errorType);
        
        // Agregar argumentos si son tipos simples (para evitar problemas de serialización)
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && (args[i] instanceof String || args[i] instanceof Number || args[i] instanceof Boolean)) {
                    context.put("arg" + i, String.valueOf(args[i]));
                }
            }
        }
        
        log.debug("trackException - Call exceptionTrackerPort.trackException");
        // Delegamos al puerto que ya implementa asincronía con CompletableFuture
        try {
            exceptionTrackerPort.trackException(exception, fullMethodName, context)
                .exceptionally(ex -> {
                    log.error("Error while tracking exception asynchronously: {}", ex.getMessage(), ex);
                    return null;
                });
            log.debug("trackException - Exception tracking delegated to async port");
        } catch (Exception ex) {
            // En caso de error al delegar, aseguramos que el flujo principal no se vea afectado
            log.error("trackException - Failed to delegate exception tracking: {}", ex.getMessage(), ex);
        }
    }
}
