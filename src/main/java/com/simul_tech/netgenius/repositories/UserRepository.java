package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String username);
    Optional<User> findById(int id);
    Boolean existsByEmail(String email);
}
