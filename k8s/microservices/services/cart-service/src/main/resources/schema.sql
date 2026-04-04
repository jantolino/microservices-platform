-- Schema for Notification Service Database
-- Created: 2025-07-01

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS user_preference_event_channels;
DROP TABLE IF EXISTS user_preference_events;
DROP TABLE IF EXISTS user_preference_channels;
DROP TABLE IF EXISTS user_preferences;
DROP TABLE IF EXISTS template_supported_channels;
DROP TABLE IF EXISTS templates;
DROP TABLE IF EXISTS notification_channels;
DROP TABLE IF EXISTS delivery_attempts;
DROP TABLE IF EXISTS delivery_results;
DROP TABLE IF EXISTS cancellation_results;
DROP TABLE IF EXISTS notification_audits;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS event_subscriptions;
DROP TABLE IF EXISTS transactional_outbox;

-- Create tables

-- Notifications table
CREATE TABLE notifications (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    content_body VARCHAR(4000) NOT NULL,
    content_attributes VARCHAR(4000),
    recipient_user_id VARCHAR(100),
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),
    recipient_device_token VARCHAR(255),
    recipient_attributes VARCHAR(4000),
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    retry_count INT,
    error_message TEXT,
    template_id VARCHAR(100),
    requester_id VARCHAR(100) NOT NULL,
    source_service VARCHAR(50) NOT NULL,
    CONSTRAINT uk_notifications_uuid UNIQUE (uuid)
);

-- Notification channels table
CREATE TABLE notification_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,    
    notification_uuid VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    CONSTRAINT fk_notification_channels_notification FOREIGN KEY (notification_uuid) REFERENCES notifications (uuid)
);

-- Índice para optimizar búsquedas por notification_uuid
CREATE INDEX idx_notification_channels_notification_uuid ON notification_channels (notification_uuid);

-- Templates table
CREATE TABLE templates (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    subject VARCHAR(255),
    language VARCHAR(10),
    version INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    active BOOLEAN NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    source_service VARCHAR(50) NOT NULL,
    CONSTRAINT uk_templates_uuid UNIQUE (uuid),
    CONSTRAINT uk_templates_code UNIQUE (code)
);

-- Template supported channels table
CREATE TABLE template_supported_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,    
    template_uuid VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    CONSTRAINT fk_template_supported_channels_template FOREIGN KEY (template_uuid) REFERENCES templates (uuid)
);

-- Índice para optimizar búsquedas por template_uuid
CREATE INDEX idx_template_supported_channels_template_uuid ON template_supported_channels (template_uuid);

-- User preferences table
CREATE TABLE user_preferences (
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    global_opt_out BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_user_preferences_uuid UNIQUE (uuid),
    CONSTRAINT uk_user_preferences_user_id UNIQUE (user_id)
);

-- User preference channels table
CREATE TABLE user_preference_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_preference_uuid VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_preference_channels_user_preference FOREIGN KEY (user_preference_uuid) REFERENCES user_preferences (uuid)
);

-- Índice para optimizar búsquedas por user_preference_uuid
CREATE INDEX idx_user_preference_channels_user_preference_uuid ON user_preference_channels (user_preference_uuid);

-- User preference events table
CREATE TABLE user_preference_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_preference_uuid VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    CONSTRAINT fk_user_preference_events_user_preference FOREIGN KEY (user_preference_uuid) REFERENCES user_preferences (uuid)
);

-- Índice para optimizar búsquedas por user_preference_uuid
CREATE INDEX idx_user_preference_events_user_preference_uuid ON user_preference_events (user_preference_uuid);

-- User preference event channels table
CREATE TABLE user_preference_event_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_preference_event_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_preference_event_channels_event FOREIGN KEY (user_preference_event_id) REFERENCES user_preference_events (id)
);

-- Event subscriptions table
CREATE TABLE event_subscriptions (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source_service VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_event_subscriptions_uuid UNIQUE (uuid)
);

-- Notification audits table
CREATE TABLE notification_audits (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    notification_uuid VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(100) NOT NULL,
    requester_id VARCHAR(100) NOT NULL,
    source_service VARCHAR(50),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    device_info VARCHAR(255),
    CONSTRAINT uk_notification_audits_uuid UNIQUE (uuid)
);

-- Delivery attempts table
CREATE TABLE delivery_attempts (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    notification_uuid VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    successful BOOLEAN NOT NULL,
    error_message TEXT,
    provider_response TEXT,
    external_id VARCHAR(255),
    attempt_number INT NOT NULL,
    CONSTRAINT uk_delivery_attempts_uuid UNIQUE (uuid),
    CONSTRAINT fk_delivery_attempts_notification FOREIGN KEY (notification_uuid) REFERENCES notifications (uuid)
);

-- Índice para optimizar búsquedas por notification_uuid
CREATE INDEX idx_delivery_attempts_notification_uuid ON delivery_attempts (notification_uuid);
-- Índice para búsquedas por fecha de intento
CREATE INDEX idx_delivery_attempts_attempted_at ON delivery_attempts (attempted_at);

-- Delivery results table
CREATE TABLE delivery_results (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    successful BOOLEAN NOT NULL,
    notification_uuid VARCHAR(100) NOT NULL,
    audit_uuid VARCHAR(100),
    delivery_time_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_count INT,
    CONSTRAINT uk_delivery_results_uuid UNIQUE (uuid),
    CONSTRAINT fk_delivery_results_notification FOREIGN KEY (notification_uuid) REFERENCES notifications (uuid),
    CONSTRAINT fk_delivery_results_audit FOREIGN KEY (audit_uuid) REFERENCES notification_audits (uuid)
);

-- Índice para optimizar búsquedas por notification_uuid
CREATE INDEX idx_delivery_results_notification_uuid ON delivery_results (notification_uuid);
-- Índice para búsquedas por fecha de creación
CREATE INDEX idx_delivery_results_created_at ON delivery_results (created_at);

-- Cancellation results table
CREATE TABLE cancellation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    successful BOOLEAN NOT NULL,
    notification_uuid VARCHAR(100) NOT NULL,
    audit_uuid VARCHAR(100),
    cancelled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cancellation_results_notification FOREIGN KEY (notification_uuid) REFERENCES notifications (uuid),
    CONSTRAINT fk_cancellation_results_audit FOREIGN KEY (audit_uuid) REFERENCES notification_audits (uuid)
);

-- Índice para optimizar búsquedas por notification_uuid
CREATE INDEX idx_cancellation_results_notification_uuid ON cancellation_results (notification_uuid);
-- Índice para búsquedas por fecha de cancelación
CREATE INDEX idx_cancellation_results_cancelled_at ON cancellation_results (cancelled_at);

-- Transactional outbox table
CREATE TABLE transactional_outbox (    
    uuid VARCHAR(100) NOT NULL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,
    CONSTRAINT uk_transactional_outbox_uuid UNIQUE (uuid)
);

-- Create indexes for better performance
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_recipient_user_id ON notifications (recipient_user_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notifications_scheduled_for ON notifications (scheduled_for);
CREATE INDEX idx_event_subscriptions_event_type ON event_subscriptions (event_type, source_service);

-- Índices adicionales para campos UUID para mejor rendimiento en búsquedas frecuentes
CREATE INDEX idx_notifications_uuid ON notifications (uuid);
CREATE INDEX idx_templates_uuid ON templates (uuid);
CREATE INDEX idx_user_preferences_uuid ON user_preferences (uuid);
CREATE INDEX idx_notification_audits_uuid ON notification_audits (uuid);
CREATE INDEX idx_transactional_outbox_uuid ON transactional_outbox (uuid);
CREATE INDEX idx_transactional_outbox_aggregate_id ON transactional_outbox (aggregate_id);
CREATE INDEX idx_transactional_outbox_event_type ON transactional_outbox (event_type);
CREATE INDEX idx_user_preferences_user_id ON user_preferences (user_id);
CREATE INDEX idx_user_preference_events_event_type ON user_preference_events (event_type);
CREATE INDEX idx_notification_audits_notification_uuid ON notification_audits (notification_uuid);
CREATE INDEX idx_delivery_attempts_notification_id ON delivery_attempts (notification_uuid);
CREATE INDEX idx_delivery_attempts_successful ON delivery_attempts (successful);
CREATE INDEX idx_delivery_results_notification_id ON delivery_results (notification_uuid);
CREATE INDEX idx_delivery_results_successful ON delivery_results (successful);
CREATE INDEX idx_transactional_outbox_processed ON transactional_outbox (processed);
CREATE INDEX idx_event_subscriptions_active ON event_subscriptions (active);

-- Comments on tables
COMMENT ON TABLE notifications IS 'Stores notification data';
COMMENT ON TABLE notification_channels IS 'Stores channels associated with notifications';
COMMENT ON TABLE templates IS 'Stores notification templates';
COMMENT ON TABLE template_supported_channels IS 'Stores channels supported by templates';
COMMENT ON TABLE user_preferences IS 'Stores user notification preferences';
COMMENT ON TABLE user_preference_channels IS 'Stores enabled channels for user preferences';
COMMENT ON TABLE user_preference_events IS 'Stores event preferences for users';
COMMENT ON TABLE user_preference_event_channels IS 'Stores channels enabled for specific events in user preferences';
COMMENT ON TABLE event_subscriptions IS 'Stores subscriptions to events from other microservices';
COMMENT ON TABLE notification_audits IS 'Stores audit logs for notification actions';
COMMENT ON TABLE delivery_attempts IS 'Stores notification delivery attempts';
COMMENT ON TABLE delivery_results IS 'Stores results of notification deliveries';
COMMENT ON TABLE cancellation_results IS 'Stores results of notification cancellations';
COMMENT ON TABLE transactional_outbox IS 'Implements outbox pattern for transactional message publishing';
