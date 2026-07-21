package com.secondhand.backend.repository;

import com.secondhand.backend.model.User;
import com.secondhand.backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByStatus(UserStatus status);
}
