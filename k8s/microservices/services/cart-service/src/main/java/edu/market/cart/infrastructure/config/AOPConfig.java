package edu.market.notification.infrastructure.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuración para las métricas del servicio de notificaciones.
 * Configura Micrometer con Prometheus para la recolección de métricas técnicas.
 */
@Configuration
@EnableAspectJAutoProxy
public class AOPConfig {

    /**
     * Configura el registro de métricas de Prometheus.
     * 
     * @return PrometheusMeterRegistry configurado
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Configura el aspecto TimedAspect de Micrometer para permitir
     * la anotación @Timed en métodos.
     * 
     * @param registry Registro de métricas
     * @return TimedAspect configurado
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
