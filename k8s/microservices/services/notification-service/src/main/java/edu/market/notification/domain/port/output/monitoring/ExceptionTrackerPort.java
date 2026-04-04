package edu.market.notification.domain.port.output.monitoring;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Puerto para el seguimiento centralizado de excepciones que no deben interrumpir el flujo.
 * Permite registrar excepciones en un sistema externo de monitoreo sin afectar
 * la ejecución normal del programa.
 * 
 * Los métodos son asíncronos para evitar bloquear el flujo principal de la aplicación,
 * devolviendo CompletableFuture que pueden ser combinados o ignorados según sea necesario.
 * 
 * Implementa los patrones:
 * - Hexagonal Architecture: Puerto de salida para adaptadores de infraestructura
 * - Fault Tolerance: Facilita el registro de errores sin afectar el flujo principal
 * - Asynchronous Processing: Permite el procesamiento asíncrono de información no crítica
 */
public interface ExceptionTrackerPort {
    
    /**
     * Registra una excepción con contexto adicional sin interrumpir el flujo.
     * 
     * @param throwable La excepción a registrar
     * @param source Origen o componente donde ocurrió la excepción
     * @param context Información contextual adicional sobre la excepción
     * @return CompletableFuture<Void> que se completa cuando la excepción ha sido registrada
     */
    CompletableFuture<Void> trackException(Throwable throwable, String source, Map<String, String> context);

    /**
     * Verifica de forma asíncrona si el servicio de seguimiento de excepciones está disponible.
     *
     * @return CompletableFuture<Boolean> que se completa con true si el servicio está disponible
     */
    CompletableFuture<Boolean> isAvailable();
}
