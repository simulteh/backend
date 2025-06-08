package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
