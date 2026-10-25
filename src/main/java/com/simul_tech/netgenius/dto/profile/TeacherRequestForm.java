package com.simul_tech.netgenius.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TeacherRequestForm {
    @NotBlank @Size(max = 255)
    @Schema(description = "ФИО заявителя")
    private String fullName;

    @NotBlank @Size(max = 255)
    @Schema(description = "Юридическое название вуза")
    private String organization;

    @NotNull
    @Schema(description = "Выписка из трудовой, PDF/JPG/PNG/DOC, до 5 МиБ", type = "string", format = "binary")
    private MultipartFile file;
}
