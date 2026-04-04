package edu.market.notification.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "notification-service.app.config.infrastructure.metrics")
public class MetricsProperties {

    @NotEmpty
    private String prefix;
}
