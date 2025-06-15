package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.LaboratoryWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LaboratoryWorkRepository extends JpaRepository<LaboratoryWork, Long> {
    List<LaboratoryWork> findAllByIdUser(Long idUser);
    List<LaboratoryWork> findAllByIdRecipient(Long idRecipient);
    List<LaboratoryWork> findByStatus(String status);
    List<LaboratoryWork> findByIdTimeBetween(LocalDateTime start, LocalDateTime end);
    void deleteAllByIdUser(Long idUser);
} 