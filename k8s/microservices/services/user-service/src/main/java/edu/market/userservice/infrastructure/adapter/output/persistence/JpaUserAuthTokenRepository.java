package edu.market.userservice.infrastructure.adapter.output.persistence;

import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserAuthTokenEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.mapper.TokenMapper;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.UserAuthTokenRepository;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaUserAuthTokenRepository implements UserAuthTokenRepositoryPort {
    
    private final UserAuthTokenRepository userAuthTokenRepository;
    private final UserRepository userRepository;
    private final TokenMapper tokenMapper;    

    @Override
    @Transactional
    public UserAuthToken save(UserAuthToken token) {
        try {
            log.info("init - save");
            
            if (token == null) {
                log.debug("save token is null, returning null");
                log.info("end - save");
                return null;
            }
            
            log.debug("save finding user with ID: {}", token.getUserId());
            UserEntity userEntity = userRepository.findById(token.getUserId())
                    .orElseThrow(() -> {
                        log.error("save user not found with ID: {}", token.getUserId());
                        return new IllegalArgumentException("User not found with ID: " + token.getUserId());
                    });
            
            log.debug("save user found with ID: {}, saving token", userEntity.getId());
            UserAuthTokenEntity saved = userAuthTokenRepository.save(
                    tokenMapper.toEntity(token, userEntity));
            
            UserAuthToken result = tokenMapper.toDomainModel(saved);
            log.debug("save token saved successfully with ID: {}", result.getId());
            log.info("end - save");
            return result;
        } catch (Exception e) {
            log.error("save error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserAuthToken> findByToken(String token) {
        try {
            log.info("init - findByToken");
            
            log.debug("findByToken searching token: {}", token);
            Optional<UserAuthTokenEntity> tokenEntity = userAuthTokenRepository.findByToken(token);
            
            if (tokenEntity.isPresent()) {
                log.debug("findByToken token found with ID: {}", tokenEntity.get().getId());
            } else {
                log.debug("findByToken token not found");
            }
            
            log.info("end - findByToken");
            return tokenEntity.map(tokenMapper::toDomain);
        } catch (Exception e) {
            log.error("findByToken error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserAuthToken> findByRefreshToken(String refreshToken) {
        try {
            log.info("init - findByRefreshToken");
            
            log.debug("findByRefreshToken searching refresh token");
            Optional<UserAuthTokenEntity> tokenEntity = userAuthTokenRepository.findByRefreshToken(refreshToken);
            
            if (tokenEntity.isPresent()) {
                log.debug("findByRefreshToken refresh token found with ID: {}", tokenEntity.get().getId());
            } else {
                log.debug("findByRefreshToken refresh token not found");
            }
            
            log.info("end - findByRefreshToken");
            return tokenEntity.map(tokenMapper::toDomain);
        } catch (Exception e) {
            log.error("findByRefreshToken error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<UserAuthToken> findActiveTokensByUserId(Long userId) {
        try {
            log.info("init - findActiveTokensByUserId");
            
            log.debug("findActiveTokensByUserId searching user with ID: {}", userId);
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.debug("findActiveTokensByUserId user not found with ID: {}, returning empty list", userId);
                log.info("end - findActiveTokensByUserId");
                return List.of();
            }
            
            log.debug("findActiveTokensByUserId user found, searching active tokens");
            List<UserAuthTokenEntity> tokens = userAuthTokenRepository.findByUserAndStatus(
                    userOpt.get(), "ACTIVE");
            
            log.debug("findActiveTokensByUserId found {} active tokens for user ID: {}", tokens.size(), userId);
            List<UserAuthToken> result = tokenMapper.toDomainList(tokens);
            
            log.info("end - findActiveTokensByUserId");
            return result;
        } catch (Exception e) {
            log.error("findActiveTokensByUserId error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int revokeAllTokensByUserId(Long userId) {
        try {
            log.info("init - revokeAllTokensByUserId");
            
            log.debug("revokeAllTokensByUserId revoking all tokens for user ID: {}", userId);
            int count = userAuthTokenRepository.revokeAllTokensByUserId(userId);
            
            log.debug("revokeAllTokensByUserId revoked {} tokens for user ID: {}", count, userId);
            log.info("end - revokeAllTokensByUserId");
            return count;
        } catch (Exception e) {
            log.error("revokeAllTokensByUserId error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int revokeToken(String token) {
        try {
            log.info("init - revokeToken");
            
            log.debug("revokeToken revoking token");
            int count = userAuthTokenRepository.revokeToken(token);
            
            log.debug("revokeToken revoked {} tokens", count);
            log.info("end - revokeToken");
            return count;
        } catch (Exception e) {
            log.error("revokeToken error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<UserAuthToken> findActiveSocialToken(Long userId, String provider) {
        try {
            log.info("init - findActiveSocialToken");
            
            log.debug("findActiveSocialToken searching active social token for user ID: {} and provider: {}", userId, provider);
            Optional<UserAuthTokenEntity> tokenEntity = userAuthTokenRepository.findActiveSocialToken(userId, provider);
            
            if (tokenEntity.isPresent()) {
                log.debug("findActiveSocialToken token found with ID: {}", tokenEntity.get().getId());
            } else {
                log.debug("findActiveSocialToken token not found");
            }
            
            log.info("end - findActiveSocialToken");
            return tokenEntity.map(tokenMapper::toDomain);
        } catch (Exception e) {
            log.error("findActiveSocialToken error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
