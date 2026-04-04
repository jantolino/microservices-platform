package edu.market.notification.infrastructure.adapter.output.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.market.notification.domain.port.output.monitoring.LoggingPort;

/**
 * Adaptador de logging que implementa el puerto de logging del dominio
 * utilizando SLF4J como implementación concreta.
 */
@Component
public class LoggingAdapter implements LoggingPort {
    
    private final Logger logger;
    
    public LoggingAdapter() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    
    // Métodos originales
    @Override
    public void info(String message) {
        logger.info(message);
    }
    
    @Override
    public void debug(String message) {
        logger.debug(message);
    }
    
    @Override
    public void warn(String message) {
        logger.warn(message);
    }
    
    @Override
    public void error(String message) {
        logger.error(message);
    }
    
    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
    
    // Nuevos métodos con formato
    @Override
    public void info(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(format, args));
        }
    }
    
    @Override
    public void debug(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(format, args));
        }
    }
    
    @Override
    public void warn(String format, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(String.format(format, args));
        }
    }
    
    @Override
    public void error(String format, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(String.format(format, args));
        }
    }
    
    @Override
    public void error(String format, Throwable throwable, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(String.format(format, args), throwable);
        }
    }
}
