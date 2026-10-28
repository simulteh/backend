package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.verification.AdminVerificationResponse;
import com.simul_tech.netgenius.dto.verification.StudentVerificationRequest;
import com.simul_tech.netgenius.dto.verification.TeacherVerificationRequest;
import com.simul_tech.netgenius.dto.verification.VerificationRequestResponse;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.enums.VerificationRequestType;
import com.simul_tech.netgenius.enums.VerificationStatus;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.Teacher;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.models.VerificationRequest;
import com.simul_tech.netgenius.repositories.StudentRepository;
import com.simul_tech.netgenius.repositories.TeacherRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import com.simul_tech.netgenius.repositories.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Подтверждение ролей: подача заявок пользователями (студент/преподаватель)
 * и их рассмотрение администратором. Переход роли только USER -> STUDENT/TEACHER.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class VerificationService {

    private static final String AUDIT_ENTITY = "VERIFICATION_REQUEST";

    private final VerificationRequestRepository verificationRequestRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final MailSenderService mailSenderService;

    /** Подача заявки на роль студента. */
    public VerificationRequestResponse requestStudent(StudentVerificationRequest request) {
        User user = userRepository.findByIdForUpdate(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        currentUserService.requireEmailConfirmed();
        requireUserRole(user);
        requireNoPending(user.getId());
        if (teacherRepository.findByUserId(user.getId()).filter(Teacher::isVerified).isPresent()) {
            throw new ConflictException("Уже есть профиль преподавателя");
        }

        Student student = studentRepository.findByUserId(user.getId()).orElseGet(Student::new);
        student.setUserId(user.getId());
        student.setFullName(request.fullName());
        student.setBirthYear(request.birthYear());
        student.setPhone(request.phone());
        student.setUniversityId(request.universityId());
        student.setFaculty(request.faculty());
        student.setSpeciality(request.speciality());
        student.setDirection(request.direction());
        student.setCourse(request.course());
        student.setDegree(request.degree());
        student.setVerified(false);
        studentRepository.save(student);

        VerificationRequest verification = createRequest(user.getId(), VerificationRequestType.STUDENT);

        auditService.log(user.getId(), AUDIT_ENTITY, verification.getId(), "CREATE_STUDENT");
        // При сбое SMTP транзакция откатывается, поэтому пользователь может повторить отправку.
        mailSenderService.sendStudentRequest(user, verification.getId(), student);
        return VerificationRequestResponse.from(verification);
    }

    /** Подача заявки на роль преподавателя. */
    public VerificationRequestResponse requestTeacher(TeacherVerificationRequest request) {
        User user = userRepository.findByIdForUpdate(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        currentUserService.requireEmailConfirmed();
        requireUserRole(user);
        requireNoPending(user.getId());
        if (studentRepository.findByUserId(user.getId()).filter(Student::isVerified).isPresent()) {
            throw new ConflictException("Уже есть профиль студента");
        }

        Teacher teacher = teacherRepository.findByUserId(user.getId()).orElseGet(Teacher::new);
        teacher.setUserId(user.getId());
        teacher.setFullName(request.fullName());
        teacher.setPhone(request.phone());
        teacher.setWorkplace(request.workplace());
        teacher.setPosition(request.position());
        teacher.setVerified(false);
        teacherRepository.save(teacher);

        VerificationRequest verification = createRequest(user.getId(), VerificationRequestType.TEACHER);

        auditService.log(user.getId(), AUDIT_ENTITY, verification.getId(), "CREATE_TEACHER");
        return VerificationRequestResponse.from(verification);
    }

    /** Заявки текущего пользователя (новые сверху). */
    @Transactional(readOnly = true)
    public List<VerificationRequestResponse> myRequests() {
        Long userId = currentUserService.getCurrentUserId();
        return verificationRequestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(VerificationRequestResponse::from)
                .toList();
    }

    /** Список заявок для администратора, опционально по статусу. */
    @Transactional(readOnly = true)
    public List<AdminVerificationResponse> listForAdmin(VerificationStatus status) {
        List<VerificationRequest> requests = (status == null)
                ? verificationRequestRepository.findAll()
                : verificationRequestRepository.findByStatus(status);
        return requests.stream()
                .sorted(Comparator.comparing(VerificationRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(AdminVerificationResponse::from)
                .toList();
    }

    /** Одобрение заявки администратором: подтверждает профиль и повышает роль пользователя. */
    public AdminVerificationResponse approve(Long requestId) {
        Long adminId = currentUserService.getCurrentUserId();
        VerificationRequest verification = getPendingRequest(requestId);

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setReviewedAt(Instant.now());
        verification.setReviewedBy(adminId);
        verificationRequestRepository.save(verification);

        if (verification.getRequestType() == VerificationRequestType.STUDENT) {
            Student student = studentRepository.findByUserId(verification.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Профиль студента не найден для пользователя: " + verification.getUserId()));
            student.setVerified(true);
            studentRepository.save(student);
            promoteUser(verification.getUserId(), Role.STUDENT);
        } else {
            Teacher teacher = teacherRepository.findByUserId(verification.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Профиль преподавателя не найден для пользователя: " + verification.getUserId()));
            teacher.setVerified(true);
            teacherRepository.save(teacher);
            promoteUser(verification.getUserId(), Role.TEACHER);
        }

        auditService.log(adminId, AUDIT_ENTITY, requestId, "APPROVE");
        return AdminVerificationResponse.from(verification);
    }

    /** Отклонение заявки администратором: профиль остаётся неподтверждённым, роль не меняется. */
    public AdminVerificationResponse reject(Long requestId, String comment) {
        Long adminId = currentUserService.getCurrentUserId();
        VerificationRequest verification = getPendingRequest(requestId);

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setReviewedAt(Instant.now());
        verification.setReviewedBy(adminId);
        verification.setComment(comment);
        verificationRequestRepository.save(verification);

        auditService.log(adminId, AUDIT_ENTITY, requestId, "REJECT");
        return AdminVerificationResponse.from(verification);
    }

    // --- helpers ---

    private void requireUserRole(User user) {
        if (user.getRole() != Role.USER) {
            throw new ConflictException("Роль уже подтверждена/назначена");
        }
    }

    private void requireNoPending(Long userId) {
        if (verificationRequestRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING)) {
            throw new ConflictException("Уже есть заявка на рассмотрении");
        }
    }

    private VerificationRequest createRequest(Long userId, VerificationRequestType type) {
        VerificationRequest verification = new VerificationRequest();
        verification.setUserId(userId);
        verification.setRequestType(type);
        verification.setStatus(VerificationStatus.PENDING);
        return verificationRequestRepository.save(verification);
    }

    private VerificationRequest getPendingRequest(Long requestId) {
        Long userId = verificationRequestRepository.findUserIdById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + requestId));
        // Подача и рассмотрение сериализуются одной блокировкой владельца.
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));
        VerificationRequest verification = verificationRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + requestId));
        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new ConflictException("Заявка уже рассмотрена");
        }
        return verification;
    }

    private void promoteUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));
        requireUserRole(user);
        user.setRole(role);
        userRepository.save(user);
    }
}
