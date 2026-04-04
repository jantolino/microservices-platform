package edu.market.userservice.infrastructure.adapter.output.persistence;

import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.domain.port.output.MetadataLoginRepositoryPort;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.MetadataLoginEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.mapper.MetadataLoginMapper;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.MetadataLoginRepository;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaMetadataLoginRepository implements MetadataLoginRepositoryPort {
    
    private final MetadataLoginRepository metadataLoginRepository;
    private final UserRepository userRepository;
    private final MetadataLoginMapper metadataLoginMapper;    

    @Override
    public MetadataLogin save(MetadataLogin metadataLogin) {
        try {
            log.info("init - save");
            
            if (metadataLogin == null) {
                log.debug("save metadataLogin is null, returning null");
                log.info("end - save");
                return null;
            }
            
            log.debug("save finding user with ID: {}", metadataLogin.getUserId());
            UserEntity userEntity = userRepository.findById(metadataLogin.getUserId())
                    .orElseThrow(() -> {
                        log.error("save user not found with ID: {}", metadataLogin.getUserId());
                        return new IllegalArgumentException("User not found with ID: " + metadataLogin.getUserId());
                    });
            
            log.debug("save user found with ID: {}, saving metadata", userEntity.getId());
            MetadataLoginEntity saved = metadataLoginRepository.save(
                    metadataLoginMapper.toEntity(metadataLogin, userEntity));
            
            MetadataLogin result = metadataLoginMapper.toDomain(saved);
            log.debug("save metadata saved successfully with ID: {}", result.getId());
            log.info("end - save");
            return result;
        } catch (Exception e) {
            log.error("save error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<MetadataLogin> findByUserId(Long userId) {
        try {
            log.info("init - findByUserId");
            
            log.debug("findByUserId searching user with ID: {}", userId);
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.debug("findByUserId user not found with ID: {}, returning empty list", userId);
                log.info("end - findByUserId");
                return List.of();
            }
            
            log.debug("findByUserId user found, searching metadata");
            List<MetadataLoginEntity> entities = metadataLoginRepository.findByUser(userOpt.get());
            
            log.debug("findByUserId found {} metadata entries for user ID: {}", entities.size(), userId);
            List<MetadataLogin> result = metadataLoginMapper.toDomainList(entities);
            
            log.info("end - findByUserId");
            return result;
        } catch (Exception e) {
            log.error("findByUserId error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<MetadataLogin> findByUserIdAndProvider(Long userId, String provider) {
        try {
            log.info("init - findByUserIdAndProvider");
            
            log.debug("findByUserIdAndProvider searching user with ID: {} and provider: {}", userId, provider);
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.debug("findByUserIdAndProvider user not found with ID: {}, returning empty", userId);
                log.info("end - findByUserIdAndProvider");
                return Optional.empty();
            }
            
            log.debug("findByUserIdAndProvider user found, searching metadata for provider: {}", provider);
            Optional<MetadataLoginEntity> metadataOpt = metadataLoginRepository.findByUserAndProvider(userOpt.get(), provider);
            
            if (metadataOpt.isPresent()) {
                log.debug("findByUserIdAndProvider metadata found with ID: {}", metadataOpt.get().getId());
            } else {
                log.debug("findByUserIdAndProvider metadata not found");
            }
            
            log.info("end - findByUserIdAndProvider");
            return metadataOpt.map(metadataLoginMapper::toDomain);
        } catch (Exception e) {
            log.error("findByUserIdAndProvider error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<MetadataLogin> findByProviderAndProviderId(String provider, String providerId) {
        try {
            log.info("init - findByProviderAndProviderId");
            
            log.debug("findByProviderAndProviderId searching metadata for provider: {} and providerId: {}", provider, providerId);
            Optional<MetadataLoginEntity> metadataOpt = metadataLoginRepository.findByProviderAndProviderId(provider, providerId);
            
            if (metadataOpt.isPresent()) {
                log.debug("findByProviderAndProviderId metadata found with ID: {}", metadataOpt.get().getId());
            } else {
                log.debug("findByProviderAndProviderId metadata not found");
            }
            
            log.info("end - findByProviderAndProviderId");
            return metadataOpt.map(metadataLoginMapper::toDomain);
        } catch (Exception e) {
            log.error("findByProviderAndProviderId error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
