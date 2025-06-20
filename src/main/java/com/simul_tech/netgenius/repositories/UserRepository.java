package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск по имени пользователя (точное совпадение)
    Optional<User> findByUsername(String username);

    // Поиск по email (точное совпадение)
    Optional<User> findByEmail(String email);

    // Проверка существования пользователя по имени
    boolean existsByUsername(String username);

    // Проверка существования пользователя по email
    boolean existsByEmail(String email);

    // Поиск активных пользователей
    List<User> findByIsActiveTrue();

    // Поиск пользователей по роли
    List<User> findByRole(String role);

    // Поиск пользователей по части имени (без учета регистра)
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :usernamePart, '%'))")
    List<User> findByUsernameContainingIgnoreCase(String usernamePart);

    // Поиск пользователей по части email (без учета регистра)
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :emailPart, '%'))")
    List<User> findByEmailContainingIgnoreCase(String emailPart);
}
