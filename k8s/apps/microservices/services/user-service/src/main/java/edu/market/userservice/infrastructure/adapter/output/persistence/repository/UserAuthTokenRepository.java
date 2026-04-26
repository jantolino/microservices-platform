package edu.market.userservice.infrastructure.adapter.output.persistence.repository;

import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserAuthTokenEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthTokenRepository extends JpaRepository<UserAuthTokenEntity, Long> {
    
    Optional<UserAuthTokenEntity> findByToken(String token);
    
    Optional<UserAuthTokenEntity> findByRefreshToken(String refreshToken);
    
    List<UserAuthTokenEntity> findByUserAndStatus(UserEntity user, String status);
    
    @Modifying
    @Query("UPDATE UserAuthTokenEntity t SET t.status = 'REVOKED' WHERE t.user.id = :userId")
    int revokeAllTokensByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE UserAuthTokenEntity t SET t.status = 'REVOKED' WHERE t.token = :token")
    int revokeToken(String token);
    
    @Query("SELECT t FROM UserAuthTokenEntity t WHERE t.user.id = :userId AND t.provider = :provider AND t.status = 'ACTIVE'")
    Optional<UserAuthTokenEntity> findActiveSocialToken(Long userId, String provider);
}
