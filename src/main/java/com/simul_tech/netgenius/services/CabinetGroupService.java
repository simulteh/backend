package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.common.MessageResponse;
import com.simul_tech.netgenius.dto.group.CabinetGroupDtos.*;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.exceptions.ConflictException;
import com.simul_tech.netgenius.exceptions.ForbiddenOperationException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.*;
import com.simul_tech.netgenius.repositories.*;
import com.simul_tech.netgenius.utils.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Личные кабинеты используют существующие группы и связи, но публичные ID студентов — users.id. */
@Service
@Transactional
@RequiredArgsConstructor
public class CabinetGroupService {
    private final GroupRepository groups;
    private final StudentGroupRepository memberships;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final UserRepository users;
    private final UniversityRepository universities;
    private final CurrentUserService currentUserService;
    private final ScoreService scoreService;
    private final AuditService auditService;

    public List<Summary> list() {
        List<Group> owned = groups.findByTeacherId(currentTeacher().getId());
        if (owned.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> counts = memberships.countForGroups(owned.stream().map(Group::getId).toList()).stream()
                .collect(Collectors.toMap(StudentGroupRepository.GroupCount::getGroupId,
                        StudentGroupRepository.GroupCount::getTotal));
        return owned.stream().map(group -> summary(group, counts.getOrDefault(group.getId(), 0L))).toList();
    }

    public Summary create(GroupRequest request) {
        Teacher teacher = currentTeacher();
        Group group = new Group();
        group.setTeacherId(teacher.getId());
        group.setGroupName(request.name().trim());
        group.setOrganization(request.organization().trim());
        groups.save(group);
        auditService.log(teacher.getUserId(), "GROUP", group.getId(), "CREATE");
        return summary(group, 0);
    }

    public Detail detail(Long id) {
        Group group = ownedGroup(id, currentTeacher().getId());
        // Старые группы получают постоянный непредсказуемый код при первом открытии.
        if (group.getInviteCode() == null) {
            group.setInviteCode(TokenGenerator.urlSafe(24));
        }
        List<Long> studentIds = memberships.findByGroupId(id).stream().map(StudentGroup::getStudentId).toList();
        List<Student> members = students.findAllById(studentIds);
        Map<Long, User> userMap = users.findAllById(members.stream().map(Student::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, List<Integer>> scores = scoreService.forUsers(userMap.keySet());
        List<StudentItem> items = members.stream().map(student -> {
            User user = userMap.get(student.getUserId());
            if (user == null) {
                throw new ResourceNotFoundException("Пользователь студента не найден");
            }
            return new StudentItem(user.getId(), user.getLastName(), user.getFirstName(), scores.get(user.getId()));
        }).sorted(Comparator.comparing(StudentItem::lastName).thenComparing(StudentItem::firstName)
                .thenComparing(StudentItem::id)).toList();
        return new Detail(group.getId(), group.getGroupName(), organization(group), group.getInviteCode(), items);
    }

    public Summary update(Long id, GroupRequest request) {
        Group group = ownedGroup(id, currentTeacher().getId());
        group.setGroupName(request.name().trim());
        group.setOrganization(request.organization().trim());
        auditService.log(currentUserService.getCurrentUserId(), "GROUP", id, "UPDATE");
        return summary(group, memberships.countByGroupId(id));
    }

    public MessageResponse addStudent(Long groupId, Long userId) {
        Long teacherId = currentTeacher().getId();
        // При добавлении и вступлении одинаковый порядок: пользователь студента, затем группа.
        User user = lockedUser(userId);
        Student student = studentProfile(user);
        Group group = ownedGroup(groupId, teacherId);
        addMembership(group, student);
        return MessageResponse.of("Студент добавлен в группу");
    }

    public MessageResponse removeStudent(Long groupId, Long userId) {
        Long teacherId = currentTeacher().getId();
        lockedUser(userId);
        Group group = ownedGroup(groupId, teacherId);
        students.findByUserId(userId).ifPresent(student ->
                memberships.deleteByStudentIdAndGroupId(student.getId(), groupId));
        auditService.log(currentUserService.getCurrentUserId(), "GROUP", group.getId(),
                "REMOVE_STUDENT", null, "userId=" + userId);
        return MessageResponse.of("Студент удалён из группы");
    }

    public MyGroup join(String inviteCode) {
        requireRole(Role.STUDENT);
        User user = lockedUser(currentUserService.getCurrentUserId());
        Student student = studentProfile(user);
        Group group = groups.findByInviteCodeForUpdate(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Приглашение не найдено"));
        addMembership(group, student);
        Teacher teacher = teachers.findById(group.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));
        User owner = users.findById(teacher.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));
        return myGroup(group, owner);
    }

    @Transactional(readOnly = true)
    public List<MyGroup> myGroups() {
        requireRole(Role.STUDENT);
        Optional<Student> student = students.findByUserId(currentUserService.getCurrentUserId());
        if (student.isEmpty()) {
            return List.of();
        }
        List<Group> joined = groups.findAllById(memberships.findByStudentId(student.get().getId()).stream()
                .map(StudentGroup::getGroupId).toList());
        Map<Long, Teacher> teacherMap = teachers.findAllById(joined.stream().map(Group::getTeacherId).toList())
                .stream().collect(Collectors.toMap(Teacher::getId, Function.identity()));
        Map<Long, User> ownerMap = users.findAllById(teacherMap.values().stream().map(Teacher::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return joined.stream().map(group -> {
            Teacher teacher = teacherMap.get(group.getTeacherId());
            User owner = teacher == null ? null : ownerMap.get(teacher.getUserId());
            if (owner == null) {
                throw new ResourceNotFoundException("Преподаватель не найден");
            }
            return myGroup(group, owner);
        }).toList();
    }

    private Teacher currentTeacher() {
        requireRole(Role.TEACHER);
        User user = lockedUser(currentUserService.getCurrentUserId());
        return teachers.findByUserId(user.getId()).orElseGet(() -> {
            // Роль могла быть назначена вручную без создания расширенного профиля.
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getId());
            teacher.setFullName(fullName(user));
            teacher.setVerified(true);
            return teachers.save(teacher);
        });
    }

    private Student studentProfile(User user) {
        if (user.getRole() != Role.STUDENT) {
            throw new ConflictException("Указанный пользователь не является студентом");
        }
        return students.findByUserId(user.getId()).orElseGet(() -> {
            Student student = new Student();
            student.setUserId(user.getId());
            student.setFullName(fullName(user));
            student.setVerified(true);
            return students.save(student);
        });
    }

    private Group ownedGroup(Long id, Long teacherId) {
        return groups.findByIdForUpdate(id).filter(group -> group.getTeacherId().equals(teacherId))
                .orElseThrow(() -> new ResourceNotFoundException("Группа не найдена"));
    }

    private void addMembership(Group group, Student student) {
        if (group.getUniversityId() != null && student.getUniversityId() != null
                && !group.getUniversityId().equals(student.getUniversityId())) {
            throw new ConflictException("Группа принадлежит другому вузу");
        }
        if (!memberships.existsByStudentIdAndGroupId(student.getId(), group.getId())) {
            StudentGroup link = new StudentGroup();
            link.setStudentId(student.getId());
            link.setGroupId(group.getId());
            memberships.save(link);
            auditService.log(currentUserService.getCurrentUserId(), "GROUP", group.getId(),
                    "ADD_STUDENT", null, "userId=" + student.getUserId());
        }
    }

    private User lockedUser(Long id) {
        return users.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private void requireRole(Role role) {
        if (currentUserService.getPrincipal().getRole() != role) {
            throw new ForbiddenOperationException("Требуется роль " + role);
        }
    }

    private String organization(Group group) {
        if (group.getOrganization() != null) {
            return group.getOrganization();
        }
        return group.getUniversityId() == null ? null : universities.findById(group.getUniversityId())
                .map(University::getName).orElse(null);
    }

    private Summary summary(Group group, long count) {
        return new Summary(group.getId(), group.getGroupName(), organization(group), count);
    }

    private MyGroup myGroup(Group group, User teacher) {
        return new MyGroup(group.getId(), group.getGroupName(), organization(group),
                new TeacherName(teacher.getLastName(), teacher.getFirstName(), teacher.getMiddleName()));
    }

    private String fullName(User user) {
        return String.join(" ", user.getLastName(), user.getFirstName(),
                user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
    }
}
