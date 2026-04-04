package edu.market.userservice.infrastructure.adapter.output.persistence.repository;

import edu.market.userservice.infrastructure.adapter.output.persistence.entity.MetadataLoginEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetadataLoginRepository extends JpaRepository<MetadataLoginEntity, Long> {
    
    List<MetadataLoginEntity> findByUser(UserEntity user);
    
    Optional<MetadataLoginEntity> findByUserAndProvider(UserEntity user, String provider);
    
    Optional<MetadataLoginEntity> findByProviderAndProviderId(String provider, String providerId);
}
