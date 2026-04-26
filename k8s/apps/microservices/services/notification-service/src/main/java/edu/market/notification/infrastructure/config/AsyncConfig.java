package edu.market.notification.infrastructure.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración para el procesamiento asíncrono en la aplicación.
 * 
 * Esta clase configura los ejecutores de tareas asíncronas utilizados por los aspectos
 * de seguimiento de excepciones y recolección de métricas. La anotación @EnableAsync
 * habilita el soporte para procesamiento asíncrono en toda la aplicación, permitiendo
 * que los métodos anotados con @Async se ejecuten en hilos separados.
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {
        
    /**
     * Crea un ejecutor de tareas para el seguimiento de excepciones mediante AOP.
     * 
     * Este ejecutor se utiliza específicamente para procesar de forma asíncrona
     * el registro y seguimiento de excepciones capturadas por los aspectos AOP,
     * evitando que el manejo de errores afecte al rendimiento del flujo principal.
     * 
     * @return Un ejecutor configurado con un pool de 3 hilos y una capacidad de cola de 300 tareas
     */
    @Bean(name = "exceptionTrackerExecutor")
    public Executor exceptionTrackerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("exceptionTrackerExecutor-");
        executor.initialize();
        return executor;
    }

    /**
     * Crea un ejecutor de tareas basado en hilos virtuales para la recolección de métricas mediante AOP.
     * 
     * Este ejecutor utiliza los hilos virtuales introducidos en Java 21 para procesar de forma asíncrona
     * la recolección y registro de métricas de rendimiento capturadas por los aspectos AOP.
     * Los hilos virtuales ofrecen mejor rendimiento y escalabilidad que los hilos tradicionales
     * al ser más ligeros y eficientes en términos de recursos.
     * 
     * @return Un ejecutor que crea un nuevo hilo virtual para cada tarea, con nombrado personalizado
     */
    @Bean(name = "metricsCollectorExecutor")
    public Executor metricsCollectorAOPExecutor() {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                .name("metricsCollectorExecutor-", 0)
                .uncaughtExceptionHandler((thread, exception) ->
                        log.error("Uncaught exception in metrics virtual thread {}: {}",
                          thread.getName(), exception.getMessage(), exception))
                .factory()
        );
    }
}
