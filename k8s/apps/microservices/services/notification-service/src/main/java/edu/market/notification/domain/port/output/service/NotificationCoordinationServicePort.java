package edu.market.notification.domain.port.output.service;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.UserPreference;

import java.util.Set;

/**
 * Puerto de salida para el servicio de coordinación de notificaciones.
 * Permite que la capa de aplicación acceda a la lógica de dominio relacionada con
 * la interacción entre notificaciones y preferencias de usuario.
 * 
 * Patrones de diseño implementados:
 * - Ports and Adapters (Hexagonal Architecture): Define una interfaz para servicios de dominio
 * - Facade: Proporciona una interfaz simplificada para la coordinación entre entidades de dominio
 * - Mediator: Define una interfaz para un componente que coordina la interacción entre entidades
 * - Dependency Inversion: La capa de aplicación depende de abstracciones, no de implementaciones
 * - Interface Segregation: Define una interfaz específica para la coordinación de notificaciones
 */
public interface NotificationCoordinationServicePort {
    
    /**
     * Determina si una notificación debe ser enviada basándose en las preferencias del usuario.
     * 
     * @param notification La notificación a evaluar
     * @param userPreference Las preferencias del usuario
     * @return true si la notificación debe ser enviada según las preferencias del usuario
     */
    boolean shouldSendNotificationBasedOnPreferences(Notification notification, UserPreference userPreference);
    
    /**
     * Determina los canales preferidos para enviar una notificación a un usuario.
     * 
     * @param notification La notificación a enviar
     * @param userPreference Las preferencias del usuario
     * @return El conjunto de canales preferidos para esta notificación
     */
    Set<NotificationChannelType> determinePreferredChannelsForNotification(
            Notification notification, UserPreference userPreference);
}
