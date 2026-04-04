package edu.market.notification.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.infrastructure.config.properties.TransactionalOutboxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de RabbitMQ para la publicación y consumo de eventos.
 * Define los exchanges, colas y bindings necesarios para la comunicación asíncrona.
 * Las colas y bindings se crean dinámicamente basados en los tipos de eventos definidos en el dominio.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final TransactionalOutboxProperties properties;
    
    /**
     * Define el exchange para los eventos de notificación.
     * Se utiliza un TopicExchange para enrutar mensajes basados en patrones de routing key.
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }
    
    /**
     * Crea un RabbitTemplate configurado para enviar mensajes a RabbitMQ.
     * 
     * @param connectionFactory Factory para conexiones RabbitMQ
     * @return RabbitTemplate configurado
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }
    
    // Colas para cada tipo de evento
    
    @Bean
    public Queue notificationCanceledQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_CANCELED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue notificationCreatedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_CREATED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue notificationFailedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_FAILED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue notificationScheduledQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_SCHEDULED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue notificationDisabledQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_DISABLED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue notificationSentQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.NOTIFICATION_SENT.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue subscriptionChangedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.SUSBCRIPTION_CHANGED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue userPreferenceChangedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.USER_PREFERENCE_CHANGED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue templateChangedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.TEMPLATE_CHANGED.name().toLowerCase()).build();
    }
    
    @Bean
    public Queue templateCreatedQueue() {
        return QueueBuilder.durable(properties.getQueuePrefix() + "." + EventType.TEMPLATE_CREATED.name().toLowerCase()).build();
    }
    
    // Bindings para cada tipo de evento
    
    @Bean
    public Binding notificationCanceledBinding(Queue notificationCanceledQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationCanceledQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_CANCELED.name().toLowerCase());
    }
    
    @Bean
    public Binding notificationCreatedBinding(Queue notificationCreatedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationCreatedQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_CREATED.name().toLowerCase());
    }
    
    @Bean
    public Binding notificationFailedBinding(Queue notificationFailedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationFailedQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_FAILED.name().toLowerCase());
    }
    
    @Bean
    public Binding notificationScheduledBinding(Queue notificationScheduledQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationScheduledQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_SCHEDULED.name().toLowerCase());
    }
    
    @Bean
    public Binding notificationDisabledBinding(Queue notificationDisabledQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationDisabledQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_DISABLED.name().toLowerCase());
    }
    
    @Bean
    public Binding notificationSentBinding(Queue notificationSentQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationSentQueue)
                .to(notificationExchange)
                .with(EventType.NOTIFICATION_SENT.name().toLowerCase());
    }
    
    @Bean
    public Binding subscriptionChangedBinding(Queue subscriptionChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(subscriptionChangedQueue)
                .to(notificationExchange)
                .with(EventType.SUSBCRIPTION_CHANGED.name().toLowerCase());
    }
    
    @Bean
    public Binding userPreferenceChangedBinding(Queue userPreferenceChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(userPreferenceChangedQueue)
                .to(notificationExchange)
                .with(EventType.USER_PREFERENCE_CHANGED.name().toLowerCase());
    }
    
    @Bean
    public Binding templateChangedBinding(Queue templateChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(templateChangedQueue)
                .to(notificationExchange)
                .with(EventType.TEMPLATE_CHANGED.name().toLowerCase());
    }
    
    @Bean
    public Binding templateCreatedBinding(Queue templateCreatedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(templateCreatedQueue)
                .to(notificationExchange)
                .with(EventType.TEMPLATE_CREATED.name().toLowerCase());
    }
}
