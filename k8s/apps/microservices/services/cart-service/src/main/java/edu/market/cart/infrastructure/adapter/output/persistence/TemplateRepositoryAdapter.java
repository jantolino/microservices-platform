package edu.market.notification.infrastructure.adapter.output.persistence;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TemplateEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.mapper.TemplateOutputMapper;
import edu.market.notification.infrastructure.adapter.output.persistence.repository.JpaTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para plantillas que implementa el puerto de repositorio del dominio.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
@Component
@RequiredArgsConstructor
public class TemplateRepositoryAdapter implements TemplateRepositoryPort {

    private final JpaTemplateRepository templateRepository;
    private final TemplateOutputMapper templateOutputMapper;

    @Override
    @Transactional
    public Template save(Template template) {
        TemplateEntity entity = templateOutputMapper.toEntity(template);
        TemplateEntity savedEntity = templateRepository.save(entity);
        return templateOutputMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findById(UUID id) {
        return templateRepository.findByUuid(id)
                .map(templateOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findByCode(String code, boolean active) {
        if (active) {
            return templateRepository.findByCode(code)
                    .filter(entity -> entity.getActive())
                    .map(templateOutputMapper::toDomain);
        } else {
            return templateRepository.findByCode(code)
                    .map(templateOutputMapper::toDomain);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findByCodeAndVersion(String code, int version) {
        return templateRepository.findByCodeAndVersion(code, version)
                .map(templateOutputMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findAllVersionsByCode(String code) {
        return templateRepository.findByCode(code)
                .stream()
                .map(templateOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findLatestVersionByCode(String code) {
        return templateRepository.findByCode(code)
                .stream()
                .max(Comparator.comparing(entity -> entity.getVersion()))
                .map(templateOutputMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<Template> findAll() {
        return templateRepository.findAll().stream()
                .map(templateOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    // Este método no está en la interfaz TemplateRepositoryPort
    @Transactional(readOnly = true)
    public List<Template> findByActive(boolean active) {
        if (active) {
            return findAllActive();
        } else {
            return templateRepository.findAll().stream()
                    .filter(entity -> !entity.getActive())
                    .map(templateOutputMapper::toDomain)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findBySupportedChannel(NotificationChannelType channelType) {
        return templateRepository.findAll().stream()
                .filter(entity -> entity.getSupportedChannelEntities().stream()
                        .anyMatch(channel -> channel.getChannel().equals(channelType)))
                .map(templateOutputMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return templateRepository.findByCode(code).isPresent();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        templateRepository.findByUuid(id)
                .ifPresent(templateRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findAllActive() {
        return templateRepository.findByActiveTrue().stream()
                .map(templateOutputMapper::toDomain)
                .collect(Collectors.toList());
    }
}
