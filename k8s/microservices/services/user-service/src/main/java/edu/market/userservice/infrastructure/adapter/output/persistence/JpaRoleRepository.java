package edu.market.userservice.infrastructure.adapter.output.persistence;

import edu.market.userservice.domain.model.Role;
import edu.market.userservice.domain.port.output.RoleRepositoryPort;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.RoleEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.mapper.RoleMapper;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaRoleRepository implements RoleRepositoryPort {
    
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;    

    @Override
    public Role save(Role role) {
        log.debug("save role with name: {}", role.getName());
        RoleEntity saved = roleRepository.save(roleMapper.toEntity(role));
        return roleMapper.toDomain(saved);
    }

    @Override
    public Optional<Role> findById(Long id) {
        log.debug("findById role with id: {}", id);
        return roleRepository.findById(id).map(roleMapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        log.debug("findByName role with name: {}", name);
        return roleRepository.findByName(name).map(roleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        log.debug("findAll roles");
        return roleMapper.toDomainList(roleRepository.findAll());
    }
}
