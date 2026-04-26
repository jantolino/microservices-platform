package edu.market.userservice.domain.port.output;

import java.util.List;
import java.util.Optional;

import edu.market.userservice.domain.model.User;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findAll(int page, int size);
    void deleteById(Long id);
}
