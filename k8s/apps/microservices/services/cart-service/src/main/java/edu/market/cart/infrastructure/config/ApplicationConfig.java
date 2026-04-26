package edu.market.notification.infrastructure.config;

import edu.market.notification.application.port.input.event.ProcessDomainEventUseCasePort;
import edu.market.notification.application.port.input.event.SubscribeToEventsUseCasePort;
import edu.market.notification.application.port.input.event.UnsubscribeFromEventsUseCasePort;
import edu.market.notification.application.port.input.notification.CancelNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.CheckNotificationStatusUseCasePort;
import edu.market.notification.application.port.input.notification.GenerateNotificationReportUseCasePort;
import edu.market.notification.application.port.input.notification.GetNotificationHistoryUseCasePort;
import edu.market.notification.application.port.input.notification.RetryNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.ScheduleNotificationUseCasePort;
import edu.market.notification.application.port.input.notification.SendNotificationUseCasePort;
import edu.market.notification.application.port.input.template.CreateTemplateUseCasePort;
import edu.market.notification.application.port.input.template.DeleteTemplateUseCasePort;
import edu.market.notification.application.port.input.template.GetTemplateUseCasePort;
import edu.market.notification.application.port.input.template.UpdateTemplateUseCasePort;
import edu.market.notification.application.port.input.user.DisableNotificationsUseCasePort;
import edu.market.notification.application.port.input.user.GetUserPreferencesUseCasePort;
import edu.market.notification.application.port.input.user.UpdateUserPreferencesUseCasePort;
import edu.market.notification.application.usecase.event.ProcessDomainEventUseCase;
import edu.market.notification.application.usecase.event.SubscribeToEventsUseCase;
import edu.market.notification.application.usecase.event.UnsubscribeFromEventsUseCase;
import edu.market.notification.application.usecase.notification.CancelNotificationUseCase;
import edu.market.notification.application.usecase.notification.CheckNotificationStatusUseCase;
import edu.market.notification.application.usecase.notification.GenerateNotificationReportUseCase;
import edu.market.notification.application.usecase.notification.GetNotificationHistoryUseCase;
import edu.market.notification.application.usecase.notification.RetryNotificationUseCase;
import edu.market.notification.application.usecase.notification.ScheduleNotificationUseCase;
import edu.market.notification.application.usecase.notification.SendNotificationUseCase;
import edu.market.notification.application.usecase.template.CreateTemplateUseCase;
import edu.market.notification.application.usecase.template.DeleteTemplateUseCase;
import edu.market.notification.application.usecase.template.GetTemplateUseCase;
import edu.market.notification.application.usecase.template.UpdateTemplateUseCase;
import edu.market.notification.application.usecase.user.DisableNotificationsUseCase;
import edu.market.notification.application.usecase.user.GetUserPreferencesUseCase;
import edu.market.notification.application.usecase.user.UpdateUserPreferencesUseCase;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.EventSubscriptionRepositoryPort;
import edu.market.notification.domain.port.output.persistence.NotificationRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;
import edu.market.notification.domain.port.output.persistence.TransactionalOutboxRepositoryPort;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;
import edu.market.notification.domain.port.output.service.NotificationChannelServicePort;
import edu.market.notification.domain.port.output.service.ReportGeneratorServicePort;
import edu.market.notification.domain.port.output.service.SerializationServicePort;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de los casos de uso de la aplicación.
 * Esta clase sigue el principio de inversión de dependencias de la arquitectura hexagonal,
 * proporcionando implementaciones para los puertos de entrada que la capa de aplicación expone.
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * Casos de uso para notificaciones
     */
    @Bean
    public SendNotificationUseCasePort sendNotificationUseCase(
            NotificationChannelServicePort notificationChannel,
            NotificationRepositoryPort notificationRepository,
            TransactionalOutboxRepositoryPort outboxRepository,            
            LoggingPort log,
            SerializationServicePort serialization) {
        return new SendNotificationUseCase(
                notificationChannel,
                notificationRepository,
                outboxRepository,                
                log,
                serialization);
    }
    
    /**
     * Casos de uso para plantillas
     */
    @Bean
    public CreateTemplateUseCasePort createTemplateUseCase(
            TemplateRepositoryPort templateRepository,
            LoggingPort log) {
        return new CreateTemplateUseCase(templateRepository, log);
    }
    
    /**
     * Casos de uso para eventos
     */
    @Bean
    public ProcessDomainEventUseCasePort processDomainEventUseCase(
            EventSubscriptionRepositoryPort eventSubscriptionRepository,
            UserPreferenceRepositoryPort userPreferenceRepository,
            SendNotificationUseCasePort sendNotificationUseCase,
            LoggingPort log) {
        return new ProcessDomainEventUseCase(
                eventSubscriptionRepository,
                userPreferenceRepository,
                sendNotificationUseCase,
                log);
    }
    
    @Bean
    public SubscribeToEventsUseCasePort subscribeToEventsUseCase(
            EventSubscriptionRepositoryPort eventSubscriptionRepository,
            LoggingPort log) {
        return new SubscribeToEventsUseCase(eventSubscriptionRepository, log);
    }
    
    @Bean
    public UnsubscribeFromEventsUseCasePort unsubscribeFromEventsUseCase(
            EventSubscriptionRepositoryPort eventSubscriptionRepository,
            LoggingPort log) {
        return new UnsubscribeFromEventsUseCase(eventSubscriptionRepository, log);
    }
    
    /**
     * Casos de uso para notificaciones
     */
    @Bean
    public CancelNotificationUseCasePort cancelNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            TransactionalOutboxRepositoryPort outboxRepository,
            LoggingPort log,
            SerializationServicePort serialization) {
        return new CancelNotificationUseCase(
                notificationRepository,
                outboxRepository,
                log,
                serialization);
    }
    
    @Bean
    public CheckNotificationStatusUseCasePort checkNotificationStatusUseCase(
            NotificationRepositoryPort notificationRepository,
            LoggingPort log) {
        return new CheckNotificationStatusUseCase(notificationRepository, log);
    }
    
    @Bean
    public GenerateNotificationReportUseCasePort generateNotificationReportUseCase(
            NotificationRepositoryPort notificationRepository,
            ReportGeneratorServicePort reportGeneratorService,
            LoggingPort log) {
        return new GenerateNotificationReportUseCase(
                notificationRepository,
                reportGeneratorService,
                log);
    }
    
    @Bean
    public GetNotificationHistoryUseCasePort getNotificationHistoryUseCase(
            NotificationRepositoryPort notificationRepository,
            LoggingPort log) {
        return new GetNotificationHistoryUseCase(notificationRepository, log);
    }
    
    @Bean
    public RetryNotificationUseCasePort retryNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            NotificationChannelServicePort notificationChannel,
            TransactionalOutboxRepositoryPort outboxRepository,
            LoggingPort log,
            SerializationServicePort serialization) {
        return new RetryNotificationUseCase(
                notificationRepository,
                notificationChannel,
                outboxRepository,
                log,
                serialization,
                3); // Valor predeterminado para maxRetries
    }
    
    @Bean
    public ScheduleNotificationUseCasePort scheduleNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            TransactionalOutboxRepositoryPort outboxRepository,
            LoggingPort log,
            SerializationServicePort serialization) {
        return new ScheduleNotificationUseCase(
                notificationRepository,
                outboxRepository,
                log,
                serialization);
    }
    
    /**
     * Casos de uso para plantillas
     */
    @Bean
    public DeleteTemplateUseCasePort deleteTemplateUseCase(
            TemplateRepositoryPort templateRepository,
            LoggingPort log) {
        return new DeleteTemplateUseCase(templateRepository, log);
    }
    
    @Bean
    public GetTemplateUseCasePort getTemplateUseCase(
            TemplateRepositoryPort templateRepository,
            LoggingPort log) {
        return new GetTemplateUseCase(templateRepository, log);
    }
    
    @Bean
    public UpdateTemplateUseCasePort updateTemplateUseCase(
            TemplateRepositoryPort templateRepository,
            LoggingPort log) {
        return new UpdateTemplateUseCase(templateRepository, log);
    }
    
    /**
     * Casos de uso para preferencias de usuario
     */
    @Bean
    public DisableNotificationsUseCasePort disableNotificationsUseCase(
            UserPreferenceRepositoryPort userPreferenceRepository,
            LoggingPort log) {
        return new DisableNotificationsUseCase(userPreferenceRepository, log);
    }
    
    @Bean
    public GetUserPreferencesUseCasePort getUserPreferencesUseCase(
            UserPreferenceRepositoryPort userPreferenceRepository,
            LoggingPort log) {
        return new GetUserPreferencesUseCase(userPreferenceRepository, log);
    }
    
    @Bean
    public UpdateUserPreferencesUseCasePort updateUserPreferencesUseCase(
            UserPreferenceRepositoryPort userPreferenceRepository,
            LoggingPort log) {
        return new UpdateUserPreferencesUseCase(userPreferenceRepository, log);
    }
}
