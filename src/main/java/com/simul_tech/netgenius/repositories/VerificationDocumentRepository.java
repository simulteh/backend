package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, Long> {
    List<VerificationDocument> findByRequestId(Long requestId);
}
