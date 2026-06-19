package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.SignUpRequest;
import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.exceptions.EmailNotFound;
import com.simul_tech.netgenius.exceptions.InvalidRequestException;
import com.simul_tech.netgenius.exceptions.InvalidTokenException;
import com.simul_tech.netgenius.exceptions.UserNotFoundException;
import com.simul_tech.netgenius.impls.TokenType;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.ResetToken;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.models.VerifyToken;
import com.simul_tech.netgenius.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws EmailNotFound {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFound("Почта не найдена: " + email));
        return UserDetailsImpl.build(user);
    }

    public User findByEmail(String email) throws EmailNotFound {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFound("Почта не найдена: " + email));
    }


    public void signUpUser(SignUpRequest signUpRequest) throws InvalidRequestException {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new InvalidRequestException("Пользователь с таким email уже существует");
        }
        String hashed = passwordEncoder.encode(signUpRequest.getPassword());

        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setMiddleName(signUpRequest.getMiddleName());
        user.setNickname(signUpRequest.getNickname());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(hashed);
        user.setRole(Role.USER);
        user.setEnabled(false);

        userRepository.save(user);
        sendVerifyToken(user.getEmail());
    }

    /** Смена пароля авторизованным пользователем (проверяется текущий пароль). */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidRequestException("Текущий пароль неверен");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    /** Обновление профиля (ФИО, никнейм). Незаданные поля не меняются. */
    public User updateProfile(Long userId, String firstName, String lastName, String middleName, String nickname) {
        User user = findById(userId);
        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (middleName != null) {
            user.setMiddleName(middleName);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        return userRepository.save(user);
    }

    /** Смена email: проверяется пароль, новый email сбрасывает подтверждение и шлёт письмо. */
    public void updateEmail(Long userId, String newEmail, String password) {
        User user = findById(userId);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidRequestException("Пароль неверен");
        }
        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new InvalidRequestException("Новый email совпадает с текущим");
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new InvalidRequestException("Пользователь с таким email уже существует");
        }
        user.setEmail(newEmail);
        user.setEnabled(false);
        userRepository.save(user);
        sendVerifyToken(newEmail);
        log.info("Email changed for user id {} -> {}", userId, newEmail);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким ID не существует: " + id));
    }

    public void sendVerifyToken(String email) {
        User user = findByEmail(email);
        String token = tokenService.createVerifyToken(user.getId());
        // Доставка письма — побочный канал: её сбой не должен срывать регистрацию.
        // Токен уже сохранён; пользователь сможет запросить повторную отправку.
        try {
            mailSenderService.sendCreationEmail(email, token, user.getFirstName());
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Не удалось отправить письмо подтверждения на {}: {}", email, e.getMessage());
        }
    }

    public void sendResetToken(String email) {
        User user = findByEmail(email);
        String token = tokenService.createResetToken(user.getId());
        try {
            mailSenderService.sendPasswordResetEmail(email, token, user.getFirstName());
            log.info("Reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Не удалось отправить письмо сброса пароля на {}: {}", email, e.getMessage());
        }
    }

    public void confirmUser(String token) {
        Long userId = tokenService.validateToken(token, TokenType.VERIFY);
        try {
            User user = findById(userId);
            user.setEnabled(true);

            userRepository.save(user);
            tokenService.consumeToken(token);
            log.info("Successfully confirmed registration for user: {}", user.getEmail());
        } catch (UserNotFoundException e) {
            log.info("User not found: {}", e.getMessage());
            throw e;
        }
    }

    public void resetPassword(String token, String newPassword) {
        Long userId = tokenService.validateToken(token, TokenType.RESET);
        try {
            User user = findById(userId);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            tokenService.consumeToken(token);
            log.info("Password successfully reset for user: {}", user.getEmail());
        } catch (UserNotFoundException e) {
            log.info("User not found: {}", e.getMessage());
            throw e;
        }
    }
}
