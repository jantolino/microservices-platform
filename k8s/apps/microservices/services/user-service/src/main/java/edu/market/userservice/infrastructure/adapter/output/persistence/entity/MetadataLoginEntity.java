package edu.market.userservice.infrastructure.adapter.output.persistence.entity;

import edu.market.userservice.domain.enums.AuthProviderType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "metadata_login")
@Data
public class MetadataLoginEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
        
    @Column
    @Enumerated(EnumType.STRING)
    private AuthProviderType provider;
    
    @Column(name = "provider_id")
    private String providerId;
    
    @Column(name = "picture_url")
    private String pictureUrl;
    
    @Column
    private Boolean enabled;
    
    // Campos de auditoría
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "device_info")
    private String deviceInfo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
