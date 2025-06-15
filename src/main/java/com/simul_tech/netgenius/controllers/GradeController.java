package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.models.GradeDTO;
import com.simul_tech.netgenius.services.GradeService;
import lombok.RequiredArgsConstructor; // Убедись, что Lombok подключен
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Grade Controller", description = "Управление оценками")
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor // Lombok автоматически создаст конструктор для final полей
public class GradeController {

    private final GradeService gradeService; // Lombok создаст конструктор для этого поля
    @Operation(summary = "Получить все оценки или отфильтровать по score",
            description = "Возвращает список всех оценок, или оценок, соответствующих указанному score, если он предоставлен.")
    @ApiResponse(responseCode = "200", description = "Успешное получение оценок")
    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGradesOrFilterByScore(
            @RequestParam(required = false) Integer score) {
        List<GradeDTO> grades;
        if (score != null) {
            grades = gradeService.getGradesByScore(score); // Используем новый метод сервиса
        } else {
            grades = gradeService.getAllGrades(); // Используем существующий метод
        }
        return ResponseEntity.ok(grades);
    }

    @Operation(summary = "Получить оценку по ID",
            description = "Возвращает одну оценку по ее уникальному ID.")
    @ApiResponse(responseCode = "200", description = "Оценка успешно найдена")
    @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        return gradeService.getGradeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Создать новую оценку",
            description = "Создает новую запись об оценке в базе данных.")
    @ApiResponse(responseCode = "201", description = "Оценка успешно создана")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@RequestBody GradeDTO gradeDTO) {
        GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
        return new ResponseEntity<>(createdGrade, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить существующую оценку",
            description = "Обновляет данные существующей оценки по ее ID.")
    @ApiResponse(responseCode = "200", description = "Оценка успешно обновлена")
    @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, @RequestBody GradeDTO gradeDTO) {
        return gradeService.updateGrade(id, gradeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Удалить оценку по ID",
            description = "Удаляет оценку из базы данных по ее уникальному ID.")
    @ApiResponse(responseCode = "204", description = "Оценка успешно удалена (нет содержимого)")
    @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        if (gradeService.deleteGrade(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}