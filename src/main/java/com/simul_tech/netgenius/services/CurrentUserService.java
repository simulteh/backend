package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.ForbiddenOperationException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.Student;
import com.simul_tech.netgenius.models.Teacher;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.StudentRepository;
import com.simul_tech.netgenius.repositories.TeacherRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Доступ к текущему аутентифицированному пользователю и связанным профилям.
 * Единая точка резолва "кто выполняет запрос" для всех модулей.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public UserDetailsImpl getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new AuthenticationCredentialsNotFoundException("Требуется аутентификация");
        }
        return principal;
    }

    public Long getCurrentUserId() {
        return getPrincipal().getId();
    }

    public User getCurrentUser() {
        Long id = getCurrentUserId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    /** Профиль студента текущего пользователя (должен иметь роль STUDENT). */
    public Student getCurrentStudent() {
        Long userId = getCurrentUserId();
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenOperationException("Профиль студента не найден"));
    }

    /** Профиль преподавателя текущего пользователя (должен иметь роль TEACHER). */
    public Teacher getCurrentTeacher() {
        Long userId = getCurrentUserId();
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenOperationException("Профиль преподавателя не найден"));
    }

    public void requireEmailConfirmed() {
        if (!getCurrentUser().isEnabled()) {
            throw new ForbiddenOperationException("Требуется подтверждение email");
        }
    }
}
