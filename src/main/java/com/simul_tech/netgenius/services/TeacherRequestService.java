package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.dto.profile.TeacherRequestForm;
import com.simul_tech.netgenius.dto.verification.TeacherVerificationRequest;
import com.simul_tech.netgenius.exceptions.BadRequestException;
import com.simul_tech.netgenius.exceptions.ResourceNotFoundException;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TeacherRequestService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final CurrentUserService currentUserService;
    private final UserRepository users;
    private final VerificationService verificationService;
    private final MailSenderService mailSenderService;

    @Transactional
    public void submit(TeacherRequestForm form) {
        MultipartFile file = form.getFile();
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Приложите выписку из трудовой");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Размер файла не должен превышать 5 МиБ");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("\r") || filename.contains("\n")) {
            throw new BadRequestException("Некорректное имя файла");
        }
        filename = filename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        byte[] contents;
        try {
            contents = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Не удалось прочитать файл");
        }
        String contentType = switch (extension) {
            case "pdf" -> hasPrefix(contents, 0x25, 0x50, 0x44, 0x46, 0x2d) ? "application/pdf" : null;
            case "jpg", "jpeg" -> hasPrefix(contents, 0xff, 0xd8, 0xff) ? "image/jpeg" : null;
            case "png" -> hasPrefix(contents, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a) ? "image/png" : null;
            case "doc" -> hasPrefix(contents, 0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1) ? "application/msword" : null;
            default -> null;
        };
        if (contentType == null) {
            throw new BadRequestException("Допустимы файлы PDF, JPG, PNG и DOC соответствующего формата");
        }
        User user = users.findByIdForUpdate(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        var request = verificationService.requestTeacher(new TeacherVerificationRequest(
                form.getFullName().trim(), null, form.getOrganization().trim(), null));
        // Роль остаётся USER. При сбое SMTP заявка откатывается и может быть отправлена повторно.
        mailSenderService.sendTeacherRequest(user, request.id(), form.getFullName().trim(),
                form.getOrganization().trim(), filename, contentType, contents);
    }

    private boolean hasPrefix(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (Byte.toUnsignedInt(bytes[i]) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
