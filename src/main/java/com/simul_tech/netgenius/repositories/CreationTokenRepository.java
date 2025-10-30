package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CreationTokenRepository extends JpaRepository<PasswordResetToken, Integer>{
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUserId(int userId);
    void deleteByUserId(int userId);
    int deleteByExpiryDateBefore(LocalDateTime date);
}
