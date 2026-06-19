package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.University;
import com.simul_tech.netgenius.models.UniversityCreationRequest;
import com.simul_tech.netgenius.repositories.UniversityCreationRequestRepository;
import com.simul_tech.netgenius.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Модуль "Университеты": публичный просмотр вузов, заявки преподавателей на создание
 * вуза и администрирование (одобрение/отклонение заявок, прямой CRUD по вузам).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UniversityService {
    private static final String ENTITY_UNIVERSITY = "University";
    private static final String ENTITY_REQUEST = "UniversityCreationRequest";

    private final UniversityRepository universityRepository;
    private final UniversityCreationRequestRepository universityCreationRequestRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    /** Публичный список вузов; при непустом поиске — фильтр по названию. */
    @Transactional(readOnly = true)
    public List<University> listUniversities(String search) {
        if (search != null && !search.isBlank()) {
            return universityRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search.trim());
        }
        return universityRepository.findAll();
    }

    /** Один вуз по id (404, если нет). */
    @Transactional(readOnly = true)
    public University getUniversity(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Вуз не найден: " + id));
    }

    /** Создание заявки преподавателем (статус PENDING). */
    public UniversityCreationRequest createRequest(String universityName, String faculty,
                                                   String speciality, String direction) {
        Long teacherId = currentUserService.getCurrentTeacher().getId();

        UniversityCreationRequest request = new UniversityCreationRequest();
        request.setTeacherId(teacherId);
        request.setUniversityName(universityName);
        request.setFaculty(faculty);
        request.setSpeciality(speciality);
        request.setDirection(direction);
        request.setStatus(VerificationStatus.PENDING);

        UniversityCreationRequest saved = universityCreationRequestRepository.save(request);
        auditService.log(currentUserService.getCurrentUserId(), ENTITY_REQUEST, saved.getId(), "CREATE");
        return saved;
    }

    /** Список заявок для администратора; при заданном статусе — фильтр по нему. */
    @Transactional(readOnly = true)
    public List<UniversityCreationRequest> listRequests(VerificationStatus status) {
        if (status != null) {
            return universityCreationRequestRepository.findByStatus(status);
        }
        return universityCreationRequestRepository.findAll();
    }

    /** Одобрение заявки: создаётся вуз, заявка переводится в APPROVED. */
    public University approveRequest(Long requestId) {
        UniversityCreationRequest request = findRequest(requestId);
        if (request.getStatus() != VerificationStatus.PENDING) {
            throw new ConflictException("Заявка уже обработана: " + request.getStatus());
        }
        Long adminUserId = currentUserService.getCurrentUserId();

        University university = new University();
        university.setName(request.getUniversityName());
        university.setFaculty(request.getFaculty());
        university.setSpeciality(request.getSpeciality());
        university.setDirection(request.getDirection());
        university.setCreatedByAdmin(adminUserId);
        University savedUniversity = universityRepository.save(university);

        request.setStatus(VerificationStatus.APPROVED);
        request.setReviewedAt(Instant.now());
        request.setReviewedBy(adminUserId);
        universityCreationRequestRepository.save(request);

        auditService.log(adminUserId, ENTITY_REQUEST, request.getId(), "APPROVE");
        auditService.log(adminUserId, ENTITY_UNIVERSITY, savedUniversity.getId(), "CREATE");
        return savedUniversity;
    }

    /** Отклонение заявки: статус REJECTED. */
    public UniversityCreationRequest rejectRequest(Long requestId) {
        UniversityCreationRequest request = findRequest(requestId);
        if (request.getStatus() != VerificationStatus.PENDING) {
            throw new ConflictException("Заявка уже обработана: " + request.getStatus());
        }
        Long adminUserId = currentUserService.getCurrentUserId();

        request.setStatus(VerificationStatus.REJECTED);
        request.setReviewedAt(Instant.now());
        request.setReviewedBy(adminUserId);
        UniversityCreationRequest saved = universityCreationRequestRepository.save(request);

        auditService.log(adminUserId, ENTITY_REQUEST, request.getId(), "REJECT");
        return saved;
    }

    /** Прямое создание вуза администратором. */
    public University createUniversity(String name, String faculty, String speciality, String direction) {
        if (universityRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Вуз с таким названием уже существует: " + name);
        }
        Long adminUserId = currentUserService.getCurrentUserId();

        University university = new University();
        university.setName(name);
        university.setFaculty(faculty);
        university.setSpeciality(speciality);
        university.setDirection(direction);
        university.setCreatedByAdmin(adminUserId);
        University saved = universityRepository.save(university);

        auditService.log(adminUserId, ENTITY_UNIVERSITY, saved.getId(), "CREATE");
        return saved;
    }

    /** Обновление вуза администратором (незаданные поля не трогаются). */
    public University updateUniversity(Long id, String name, String faculty,
                                       String speciality, String direction) {
        University university = getUniversity(id);
        if (name != null && !name.isBlank()) {
            university.setName(name);
        }
        if (faculty != null) {
            university.setFaculty(faculty);
        }
        if (speciality != null) {
            university.setSpeciality(speciality);
        }
        if (direction != null) {
            university.setDirection(direction);
        }
        University saved = universityRepository.save(university);

        auditService.log(currentUserService.getCurrentUserId(), ENTITY_UNIVERSITY, saved.getId(), "UPDATE");
        return saved;
    }

    /** Удаление вуза администратором (404, если нет). */
    public void deleteUniversity(Long id) {
        University university = getUniversity(id);
        universityRepository.delete(university);
        auditService.log(currentUserService.getCurrentUserId(), ENTITY_UNIVERSITY, id, "DELETE");
    }

    private UniversityCreationRequest findRequest(Long requestId) {
        return universityCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + requestId));
    }
}
