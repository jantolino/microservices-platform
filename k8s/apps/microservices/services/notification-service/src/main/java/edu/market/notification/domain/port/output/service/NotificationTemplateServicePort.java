package edu.market.notification.domain.port.output.service;

import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.Template;

/**
 * Puerto de salida para el servicio de coordinación entre notificaciones y plantillas.
 * Permite que la capa de aplicación acceda a la lógica de dominio relacionada con
 * la interacción entre estos dos agregados.
 * 
 * Patrones de diseño implementados:
 * - Ports and Adapters (Hexagonal Architecture): Define una interfaz para servicios de dominio
 *   que serán consumidos por la capa de aplicación, siguiendo el principio de separación de capas
 * - Facade: Proporciona una interfaz simplificada para la coordinación entre entidades de dominio
 * - Mediator: Define una interfaz para un componente que coordina la interacción entre entidades
 * - Strategy: Define interfaces para diferentes algoritmos de procesamiento de plantillas
 * - Dependency Inversion: La capa de aplicación depende de abstracciones, no de implementaciones
 * - Interface Segregation: Define una interfaz específica para la coordinación de plantillas
 */
public interface NotificationTemplateServicePort {
    
    /**
     * Aplica una plantilla a una notificación, verificando compatibilidad y renderizando el contenido.
     * 
     * @param notification La notificación a la que se aplicará la plantilla
     * @param template La plantilla a aplicar
     * @return La notificación con el contenido de la plantilla aplicado
     */
    Notification applyTemplate(Notification notification, Template template);
    
    /**
     * Verifica si una plantilla es compatible con una notificación
     * 
     * @param notification La notificación a verificar
     * @param template La plantilla a verificar
     * @return true si la plantilla es compatible con la notificación
     */
    boolean isTemplateCompatibleWithNotification(Notification notification, Template template);
}
