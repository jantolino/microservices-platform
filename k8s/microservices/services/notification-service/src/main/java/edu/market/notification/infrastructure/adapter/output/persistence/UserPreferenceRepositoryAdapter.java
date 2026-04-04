package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.persistence.UserPreferenceRepositoryPort;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.UserPreferenceOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaUserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador para el puerto de repositorio de UserPreference.
 * Implementa las operaciones de persistencia para las preferencias de usuario.
 */
@Component
@RequiredArgsConstructor
public class UserPreferenceRepositoryAdapter implements UserPreferenceRepositoryPort {

    private final JpaUserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceOutputMapper userPreferenceOutputMapper;

    @Override
    @Transactional
    public UserPreference save(UserPreference userPreference) {
        UserPreferenceEntity entity = userPreferenceOutputMapper.toEntity(userPreference);
        UserPreferenceEntity savedEntity = userPreferenceRepository.save(entity);
        return userPreferenceOutputMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserPreference> findById(UUID id) {
        return userPreferenceRepository.findByUuid(id)
                .map(userPreferenceOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserPreference> findByUserId(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .map(userPreferenceOutputMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        userPreferenceRepository.findByUuid(id)
                .ifPresent(userPreferenceRepository::delete);
    }
}
