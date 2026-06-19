package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.GroupReportResponse;
import com.simul_tech.netgenius.dto.group.GroupResponse;
import com.simul_tech.netgenius.dto.group.InvitationResponse;
import com.simul_tech.netgenius.dto.group.StudentResponse;
import com.simul_tech.netgenius.enums.InvitationStatus;
import com.simul_tech.netgenius.enums.LabStatus;
import com.simul_tech.netgenius.exceptions.BadRequestException;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.Group;
import com.simul_tech.netgenius.models.GroupInvitation;
import com.simul_tech.netgenius.models.Lab;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.StudentGroup;
import com.simul_tech.netgenius.models.StudentLabProgress;
import com.simul_tech.netgenius.repositories.GroupInvitationRepository;
import com.simul_tech.netgenius.repositories.GroupRepository;
import com.simul_tech.netgenius.repositories.LabRepository;
import com.simul_tech.netgenius.repositories.StudentGroupRepository;
import com.simul_tech.netgenius.repositories.StudentLabProgressRepository;
import com.simul_tech.netgenius.repositories.StudentRepository;
import com.simul_tech.netgenius.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Группы преподавателя: CRUD, приглашения со стороны преподавателя,
 * управление студентами групп и отчёт по успеваемости.
 * Владение группой проверяется через teacherId текущего преподавателя.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private static final Duration INVITATION_TTL = Duration.ofDays(7);

    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final StudentRepository studentRepository;
    private final LabRepository labRepository;
    private final StudentLabProgressRepository studentLabProgressRepository;
    private final UniversityRepository universityRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    // ---------------------------------------------------------------------
    // Группы (CRUD)
    // ---------------------------------------------------------------------

    public GroupResponse createGroup(String groupName, Long universityId) {
        currentUserService.requireEmailConfirmed();
        Long teacherId = currentUserService.getCurrentTeacher().getId();

        // Группа всегда принадлежит существующему вузу.
        if (!universityRepository.existsById(universityId)) {
            throw new BadRequestException("Вуз не найден: " + universityId);
        }

        Group group = new Group();
        group.setTeacherId(teacherId);
        group.setUniversityId(universityId);
        group.setGroupName(groupName);
        Group saved = groupRepository.save(group);

        auditService.log(currentUserService.getCurrentUserId(), "GROUP", saved.getId(), "CREATE");
        return GroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups() {
        Long teacherId = currentUserService.getCurrentTeacher().getId();
        return groupRepository.findByTeacherId(teacherId).stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId) {
        return GroupResponse.from(requireOwnedGroup(groupId));
    }

    public GroupResponse updateGroup(Long groupId, String groupName) {
        Group group = requireOwnedGroup(groupId);
        String oldName = group.getGroupName();
        group.setGroupName(groupName);
        Group saved = groupRepository.save(group);

        auditService.log(currentUserService.getCurrentUserId(), "GROUP", saved.getId(),
                "UPDATE", oldName, groupName);
        return GroupResponse.from(saved);
    }

    public MessageResponse deleteGroup(Long groupId) {
        Group group = requireOwnedGroup(groupId);

        List<StudentGroup> links = studentGroupRepository.findByGroupId(groupId);
        if (!links.isEmpty()) {
            studentGroupRepository.deleteAll(links);
        }
        groupRepository.delete(group);

        auditService.log(currentUserService.getCurrentUserId(), "GROUP", groupId, "DELETE");
        return MessageResponse.of("Группа удалена");
    }

    // ---------------------------------------------------------------------
    // Приглашения (со стороны преподавателя)
    // ---------------------------------------------------------------------

    public InvitationResponse invite(Long groupId, String email, Long studentId) {
        currentUserService.requireEmailConfirmed();
        Group group = requireOwnedGroup(groupId);

        boolean hasEmail = email != null && !email.isBlank();
        if (!hasEmail && studentId == null) {
            throw new BadRequestException("Укажите email или studentId");
        }

        if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Студент не найден: " + studentId));
            if (studentGroupRepository.existsByStudentIdAndGroupId(student.getId(), groupId)) {
                throw new ConflictException("Студент уже состоит в группе");
            }
            if (groupInvitationRepository.existsByGroupIdAndStudentIdAndStatus(
                    groupId, student.getId(), InvitationStatus.PENDING)) {
                throw new ConflictException("Активное приглашение уже существует");
            }
        } else if (hasEmail) {
            // Для приглашений по email тоже не плодим дубли активных приглашений в ту же группу.
            boolean duplicate = groupInvitationRepository
                    .findByEmailAndStatus(email, InvitationStatus.PENDING).stream()
                    .anyMatch(inv -> groupId.equals(inv.getGroupId()));
            if (duplicate) {
                throw new ConflictException("Активное приглашение на этот email уже существует");
            }
        }

        GroupInvitation invitation = new GroupInvitation();
        invitation.setTeacherId(group.getTeacherId());
        invitation.setGroupId(groupId);
        invitation.setStudentId(studentId);
        invitation.setEmail(hasEmail ? email : null);
        invitation.setInviteToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(INVITATION_TTL));
        GroupInvitation saved = groupInvitationRepository.save(invitation);

        auditService.log(currentUserService.getCurrentUserId(), "GROUP_INVITATION", saved.getId(), "CREATE");
        return InvitationResponse.from(saved);
    }

    // ---------------------------------------------------------------------
    // Студенты групп преподавателя
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents() {
        Long teacherId = currentUserService.getCurrentTeacher().getId();

        Set<Long> studentIds = new LinkedHashSet<>();
        for (Group group : groupRepository.findByTeacherId(teacherId)) {
            for (StudentGroup link : studentGroupRepository.findByGroupId(group.getId())) {
                studentIds.add(link.getStudentId());
            }
        }

        List<StudentResponse> result = new ArrayList<>();
        for (Long studentId : studentIds) {
            studentRepository.findById(studentId)
                    .map(StudentResponse::from)
                    .ifPresent(result::add);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long studentId) {
        requireStudentInTeacherGroups(studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден: " + studentId));
        return StudentResponse.from(student);
    }

    public MessageResponse removeStudentFromGroup(Long groupId, Long studentId) {
        requireOwnedGroup(groupId);
        studentGroupRepository.deleteByStudentIdAndGroupId(studentId, groupId);

        auditService.log(currentUserService.getCurrentUserId(), "STUDENT_GROUP", groupId,
                "REMOVE_STUDENT", null, "studentId=" + studentId);
        return MessageResponse.of("Студент удалён из группы");
    }

    // ---------------------------------------------------------------------
    // Отчёт по группе
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public GroupReportResponse report(Long groupId) {
        Group group = requireOwnedGroup(groupId);

        List<Lab> labs = labRepository.findByActiveTrueOrderBySortOrderAsc();
        List<GroupReportResponse.LabColumn> labColumns = labs.stream()
                .map(lab -> new GroupReportResponse.LabColumn(lab.getId(), lab.getTitle()))
                .toList();

        // Студенты группы (в порядке вступления).
        List<Long> studentIds = studentGroupRepository.findByGroupId(groupId).stream()
                .map(StudentGroup::getStudentId)
                .distinct()
                .toList();

        // Прогресс всех студентов группы одним запросом: studentId -> (labId -> progress).
        Map<Long, Map<Long, StudentLabProgress>> progressByStudent = new LinkedHashMap<>();
        if (!studentIds.isEmpty()) {
            for (StudentLabProgress progress : studentLabProgressRepository.findByStudentIdIn(studentIds)) {
                progressByStudent
                        .computeIfAbsent(progress.getStudentId(), k -> new LinkedHashMap<>())
                        .put(progress.getLabId(), progress);
            }
        }

        List<GroupReportResponse.StudentRow> rows = new ArrayList<>();
        for (Long studentId : studentIds) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }
            Map<Long, StudentLabProgress> studentProgress =
                    progressByStudent.getOrDefault(studentId, Map.of());

            List<GroupReportResponse.Cell> cells = new ArrayList<>();
            for (Lab lab : labs) {
                StudentLabProgress progress = studentProgress.get(lab.getId());
                LabStatus status = progress != null ? progress.getStatus() : LabStatus.NOT_STARTED;
                Integer bestScore = progress != null ? progress.getBestScore() : null;
                Instant lastCompletedAt = progress != null ? progress.getLastCompletedAt() : null;
                cells.add(new GroupReportResponse.Cell(lab.getId(), bestScore, status, lastCompletedAt));
            }
            rows.add(new GroupReportResponse.StudentRow(studentId, student.getFullName(), cells));
        }

        return new GroupReportResponse(group.getId(), labColumns, rows);
    }

    // ---------------------------------------------------------------------
    // Внутренние помощники
    // ---------------------------------------------------------------------

    private Group requireOwnedGroup(Long groupId) {
        Long teacherId = currentUserService.getCurrentTeacher().getId();
        return groupRepository.findByIdAndTeacherId(groupId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Группа не найдена: " + groupId));
    }

    private void requireStudentInTeacherGroups(Long studentId) {
        Long teacherId = currentUserService.getCurrentTeacher().getId();
        for (Group group : groupRepository.findByTeacherId(teacherId)) {
            if (studentGroupRepository.existsByStudentIdAndGroupId(studentId, group.getId())) {
                return;
            }
        }
        throw new ResourceNotFoundException("Студент не найден в группах преподавателя: " + studentId);
    }
}
