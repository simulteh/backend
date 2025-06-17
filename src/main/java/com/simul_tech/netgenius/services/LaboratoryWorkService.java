package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.models.LaboratoryWork;
import com.simul_tech.netgenius.models.LaboratoryWorkUpdateRequest;
import com.simul_tech.netgenius.repositories.LaboratoryWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.simul_tech.netgenius.models.LaboratoryWorkDto;
import com.simul_tech.netgenius.models.LaboratoryWorkMapper;

@Service
public class LaboratoryWorkService {
    private final LaboratoryWorkRepository laboratoryWorkRepository;
    private final String uploadDir = "uploads/";

    @Autowired
    public LaboratoryWorkService(LaboratoryWorkRepository laboratoryWorkRepository) {
        this.laboratoryWorkRepository = laboratoryWorkRepository;
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public List<LaboratoryWork> getAllWorks(String status, String startDate, String endDate, Long idUser, Long idRecipient, String fileType, Integer grade, Boolean isClosed) {
        List<LaboratoryWork> works = laboratoryWorkRepository.findAll();
        return works.stream()
                .filter(w -> status == null || status.equals(w.getStatus()))
                .filter(w -> idUser == null || idUser.equals(w.getId_user()))
                .filter(w -> idRecipient == null || idRecipient.equals(w.getId_recipient()))
                .filter(w -> fileType == null || fileType.equalsIgnoreCase(w.getFile_type()))
                .filter(w -> grade == null || grade.equals(w.getGrade()))
                .filter(w -> isClosed == null || isClosed.equals(w.getIs_closed()))
                .filter(w -> {
                    if (startDate == null && endDate == null) return true;
                    if (w.getId_time() == null) return false;
                    if (startDate != null && endDate != null) {
                        return !w.getId_time().isBefore(java.time.LocalDateTime.parse(startDate)) && !w.getId_time().isAfter(java.time.LocalDateTime.parse(endDate));
                    } else if (startDate != null) {
                        return !w.getId_time().isBefore(java.time.LocalDateTime.parse(startDate));
                    } else {
                        return !w.getId_time().isAfter(java.time.LocalDateTime.parse(endDate));
                    }
                })
                .collect(Collectors.toList());
    }

    public List<LaboratoryWorkDto> getAllWorksDto(String status, String startDate, String endDate, Long idUser, Long idRecipient, String fileType, Integer grade, Boolean isClosed) {
        return getAllWorks(status, startDate, endDate, idUser, idRecipient, fileType, grade, isClosed)
                .stream()
                .map(LaboratoryWorkMapper::toDto)
                .toList();
    }

    public LaboratoryWork getWorkById(Long id) {
        return laboratoryWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work not found"));
    }

    public List<LaboratoryWork> getStudentWorks(Long userId) {
        return laboratoryWorkRepository.findAllByIdUser(userId);
    }

    public LaboratoryWork createWork(MultipartFile file, String comment, Long userId, Long recipientId) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Validate file size (max 100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 100MB limit");
        }

        // Validate file type
        String fileType = getFileExtension(file.getOriginalFilename());
        if (!isValidFileType(fileType)) {
            throw new RuntimeException("Invalid file type");
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "." + fileType;
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath);

        LaboratoryWork work = new LaboratoryWork();
        work.setId_user(userId);
        work.setId_recipient(recipientId);
        work.setId_departure(UUID.randomUUID().toString());
        work.setFile_name(file.getOriginalFilename());
        work.setFile_path(filePath.toString());
        work.setFile_size(file.getSize() / (1024.0 * 1024.0)); // Convert to MB
        work.setFile_type(fileType);
        work.setComment(comment);
        work.setStatus("in_progress");
        work.setIs_closed(false);

        return laboratoryWorkRepository.save(work);
    }

    public LaboratoryWork updateWork(Long id, LaboratoryWorkUpdateRequest updateRequest) {
        LaboratoryWork work = getWorkById(id);
        
        if (updateRequest.getStatus() != null) {
            work.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getGrade() != null) {
            work.setGrade(updateRequest.getGrade());
        }
        if (updateRequest.getIsClosed() != null) {
            work.setIs_closed(updateRequest.getIsClosed());
        }

        return laboratoryWorkRepository.save(work);
    }

    public void deleteWork(Long id) {
        LaboratoryWork work = getWorkById(id);
        try {
            Files.deleteIfExists(Paths.get(work.getFile_path()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
        laboratoryWorkRepository.deleteById(id);
    }

    public void deleteAllStudentWorks(Long userId) {
        List<LaboratoryWork> works = laboratoryWorkRepository.findAllByIdUser(userId);
        for (LaboratoryWork work : works) {
            try {
                Files.deleteIfExists(Paths.get(work.getFile_path()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file", e);
            }
        }
        laboratoryWorkRepository.deleteAllByIdUser(userId);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidFileType(String fileType) {
        return List.of("pdf", "doc", "docx", "xlsx", "txt", "png", "jpeg", "jpg").contains(fileType.toLowerCase());
    }
} 