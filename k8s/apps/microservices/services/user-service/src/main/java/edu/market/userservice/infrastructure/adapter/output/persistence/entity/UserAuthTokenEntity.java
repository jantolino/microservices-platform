package edu.market.userservice.infrastructure.adapter.output.persistence.entity;

import edu.market.userservice.domain.enums.AuthProviderType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_auth_tokens")
public class UserAuthTokenEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(columnDefinition = "TEXT")
    private String token;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "token_type")
    private String tokenType;
    
    @Column(nullable = false)
    private String status; // ACTIVE, REVOKED, EXPIRED
    
    // Campos para autenticación social
    @Column
    @Enumerated(EnumType.STRING)
    private AuthProviderType provider;
    
    @Column(name = "provider_user_id")
    private String providerUserId;
    
    @Column(name = "provider_access_token", columnDefinition = "TEXT")
    private String providerAccessToken;
    
    @Column(name = "provider_refresh_token", columnDefinition = "TEXT")
    private String providerRefreshToken;
    
    @Column
    private String scopes;
    
    @Column(name = "profile_data", columnDefinition = "JSON")
    private String profileData;
    
    // Información de seguridad y auditoría
    @Column(name = "device_info")
    private String deviceInfo;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
