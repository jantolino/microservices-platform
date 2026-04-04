package edu.market.userservice.infrastructure.adapter.output.event;

import edu.market.userservice.domain.event.PasswordChangedEvent;
import edu.market.userservice.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener para procesar eventos de dominio
 */
@Component
public class DomainEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);
    
    /**
     * Procesa eventos de registro de usuario
     * 
     * @param event Evento de registro de usuario
     */
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Usuario registrado: {} ({})", event.getName(), event.getEmail());
        
        // Aquí se podrían implementar acciones como:
        // 1. Enviar email de bienvenida
        // 2. Notificar a sistemas externos
        // 3. Generar estadísticas
        
        if (event.isSocialLogin()) {
            log.info("Usuario registrado mediante proveedor social");
        } else {
            log.info("Usuario registrado mediante formulario local");
        }
    }
    
    /**
     * Procesa eventos de cambio de contraseña
     * 
     * @param event Evento de cambio de contraseña
     */
    @EventListener
    public void handlePasswordChangedEvent(PasswordChangedEvent event) {
        log.info("Contraseña cambiada para el usuario: {}", event.getEmail());
        
        // Aquí se podrían implementar acciones como:
        // 1. Enviar email de notificación de cambio de contraseña
        // 2. Registrar en log de auditoría
        // 3. Revocar tokens existentes
        
        if (event.isResetPassword()) {
            log.info("Contraseña restablecida mediante proceso de recuperación");
        } else {
            log.info("Contraseña cambiada por el usuario desde: {} ({})", 
                    event.getIpAddress(), event.getUserAgent());
        }
    }
}
