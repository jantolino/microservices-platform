package edu.market.userservice.infrastructure.adapter.output.persistence;

import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.mapper.UserMapper;
import edu.market.userservice.infrastructure.adapter.output.persistence.repository.UserRepository;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.port.output.UserRepositoryPort;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepositoryPort {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;    

    @Override
    public User save(User user) {
        try {
            log.info("init - save");
            
            if (user == null) {
                log.debug("save user is null, returning null");
                log.info("end - save");
                return null;
            }
            
            log.debug("save processing user with email: {}", user.getEmail());
            
            UserEntity saved = userRepository.save(userMapper.toEntity(user));
            User result = userMapper.toDomain(saved);
            
            log.debug("save user saved successfully with ID: {}", result.getId());
            log.info("end - save");
            return result;
        } catch (Exception e) {
            log.error("save error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            log.info("init - findById");
            
            log.debug("findById searching user with ID: {}", id);
            Optional<UserEntity> userEntity = userRepository.findById(id);
            
            if (userEntity.isPresent()) {
                log.debug("findById user found with ID: {}", id);
            } else {
                log.debug("findById user not found with ID: {}", id);
            }
            
            log.info("end - findById");
            return userEntity.map(userMapper::toDomain);
        } catch (Exception e) {
            log.error("findById error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        try {
            log.info("init - findByEmail");
            
            // Realizar la búsqueda en la base de datos
            log.debug("findByEmail searching user with email: {}", email);
            Optional<UserEntity> userEntityOpt = userRepository.findByEmail(email);
            
            if (userEntityOpt.isEmpty()) {
                log.debug("findByEmail user not found with email: {}", email);
                return Optional.empty();
            }
            
            UserEntity userEntity = userEntityOpt.get();
            log.debug("findByEmail user found in database with ID: {}", userEntity.getId());
            
            // Mapear la entidad al objeto de dominio
            try {
                User user = userMapper.toDomain(userEntity);
                log.debug("findByEmail user mapped successfully with ID: {}", user.getId());
                
                log.info("end - findByEmail");
                return Optional.of(user);
            } catch (Exception e) {
                log.error("findByEmail error mapping UserEntity to User: {}", e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("findByEmail error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> findAll() {
        try {
            log.info("init - findAll");
            
            log.debug("findAll retrieving all users");
            List<UserEntity> userEntities = userRepository.findAll();
            
            log.debug("findAll found {} users", userEntities.size());
            List<User> users = userEntities.stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
            
            log.debug("findAll mapped {} users", users.size());
            log.info("end - findAll");
            return users;
        } catch (Exception e) {
            log.error("findAll error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<User> findAll(int page, int size) {
        try {
            log.info("init - findAll with pagination");
            
            log.debug("findAll retrieving users with page: {} and size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<UserEntity> userEntityPage = userRepository.findAll(pageable);
            
            log.debug("findAll found {} users in page {} of {}", 
                    userEntityPage.getNumberOfElements(), 
                    userEntityPage.getNumber(), 
                    userEntityPage.getTotalPages());
            
            List<User> users = userEntityPage.getContent().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
            
            log.debug("findAll mapped {} users", users.size());
            log.info("end - findAll with pagination");
            return users;
        } catch (Exception e) {
            log.error("findAll with pagination error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            log.info("init - deleteById");
            
            log.debug("deleteById deleting user with ID: {}", id);
            userRepository.deleteById(id);
            
            log.debug("deleteById user deleted successfully with ID: {}", id);
            log.info("end - deleteById");
        } catch (Exception e) {
            log.error("deleteById error: {}", e.getMessage(), e);
            throw e;
        }
    }    
}
