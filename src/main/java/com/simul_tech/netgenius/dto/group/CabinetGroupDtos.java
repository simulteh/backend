package com.simul_tech.netgenius.dto.group;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Контракт личных кабинетов /groups; все публичные studentId — ID пользователя. */
public final class CabinetGroupDtos {
    private CabinetGroupDtos() { }

    @Schema(name = "CabinetGroupRequest")
    public record GroupRequest(@NotBlank @Size(max = 255) String name,
                               @NotBlank @Size(max = 255) String organization) { }

    @Schema(name = "AddGroupStudentRequest")
    public record AddStudentRequest(
            @Schema(description = "ID пользователя из GET /profile/me, не внутренний ID профиля студента")
            @NotNull @Positive Long studentId) { }

    @Schema(name = "CabinetGroupSummary")
    public record Summary(Long id, String name, String organization, long studentsCount) { }

    @Schema(name = "CabinetGroupDetail")
    public record Detail(Long id, String name, String organization, String inviteCode,
                         List<StudentItem> students) { }

    @Schema(name = "CabinetGroupStudent")
    public record StudentItem(@Schema(description = "ID пользователя") Long id,
                              String lastName, String firstName,
                              @ArraySchema(minItems = 11, maxItems = 11,
                                      schema = @Schema(type = "integer", nullable = true, minimum = "0", maximum = "10"))
                              List<Integer> scores) { }

    @Schema(name = "CabinetTeacherName")
    public record TeacherName(String lastName, String firstName, String middleName) { }

    @Schema(name = "CabinetStudentGroup")
    public record MyGroup(Long id, String name, String organization, TeacherName teacher) { }
}
