package edu.market.notification.infrastructure.adapter.output.monitoring;

import edu.market.notification.domain.port.output.monitoring.ExceptionTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Implementación asíncrona del puerto de seguimiento de excepciones.
 * Esta clase proporciona una implementación que registra excepciones
 * utilizando el sistema de logging y potencialmente podría integrarse
 * con servicios externos de seguimiento de errores como Sentry, Rollbar, etc.
 * 
 * Utiliza CompletableFuture y ejecutores con hilos virtuales para operaciones asíncronas
 * que no bloquean el hilo principal de la aplicación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionTrackerAdapter implements ExceptionTrackerPort {
    
    @Qualifier("exceptionTrackerExecutor")
    private final Executor exceptionTrackerExecutor;
    
    @Override
    public CompletableFuture<Void> trackException(Throwable throwable, String source, Map<String, String> context) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("trackException - Init");

                String contextStr = context.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", "));
                
                log.debug("trackException - Call isAvailable - Checking ExceptionTracker Service is available");
                isAvailable().thenAccept(available -> {
                    if (available) {
                        // Registrar la excepción con contexto en el sistema de logging
                        log.error("Exception tracked from {} with context {{{}}}: {}", 
                                source, contextStr, throwable.getMessage(), throwable);
                        // Aquí se podría integrar con un servicio externo de seguimiento de errores
                        // Por ejemplo: 
                        // sentryClient.withScope(scope -> {
                        //     context.forEach(scope::setTag);
                        //     scope.setTag("source", source);
                        //     sentryClient.captureException(throwable);
                        // });   
                    }
                }).exceptionally(ex -> {
                    log.error("trackException - Error checking availability: {}", ex.getMessage(), ex);
                    return null;
                });
            } catch (Exception e) {
                log.error("trackException - Error tracking exception: {}", e.getMessage(), e);
            }
        }, exceptionTrackerExecutor);
        
    }

    @Override
    public CompletableFuture<Boolean> isAvailable() {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("isAvailable - Init");
            log.debug("isAvailable - Return true");
            return true;
        }, exceptionTrackerExecutor);
    }
}
