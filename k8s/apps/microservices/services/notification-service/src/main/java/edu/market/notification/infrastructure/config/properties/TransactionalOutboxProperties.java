package edu.market.notification.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "notification-service.app.config.infrastructure.transactional-outbox")
public class TransactionalOutboxProperties {
    
    @NotEmpty
    private String exchange;
    
    @NonNull
    @Positive
    @Min(value = 1, message = "processingInterval must be at least 1")
    private Integer processingInterval;
    
    @NonNull
    @Positive
    @Min(value = 1, message = "batchSize must be at least 1")    
    private Integer batchSize;
    
    @NonNull
    @NotEmpty
    private String queuePrefix;
    
    @NonNull
    @Positive
    @Min(value = 1, message = "maxRetries must be at least 1")
    private Integer maxRetries;
}
