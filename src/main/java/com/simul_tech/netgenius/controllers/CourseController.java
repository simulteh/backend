package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.CourseDTO;
import com.simul_tech.netgenius.services.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/courses")
@Tag(name = "Управление курсами", description = "CRUD операции и фильтрация курсов")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(
            summary = "Получить все курсы",
            description = "Возвращает список всех доступных курсов"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение списка курсов",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping
    public List<CourseDTO> findAll() {
        return courseService.findAll();
    }

    @Operation(
            summary = "Найти курс по ID",
            description = "Возвращает курс по указанному идентификатору"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Курс найден",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Курс не найден",
            content = @Content
    )
    @GetMapping("/{id}")
    public CourseDTO findById(
            @Parameter(description = "ID курса", required = true, example = "123")
            @PathVariable Long id
    ) {
        return courseService.findById(id);
    }

    @Operation(
            summary = "Создать курс",
            description = "Создает новый курс на основе переданных данных"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Курс успешно создан",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @PostMapping
    public CourseDTO create(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO курса для создания",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseDTO.class)))
            @RequestBody CourseDTO courseDTO
    ) {
        return courseService.create(courseDTO);
    }

    @Operation(
            summary = "Обновить курс",
            description = "Обновляет данные существующего курса по ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Курс успешно обновлен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Курс не найден",
            content = @Content
    )
    @PutMapping("/{id}")
    public CourseDTO update(
            @Parameter(description = "ID курса для обновления", required = true, example = "123")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные курса",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseDTO.class)))
            @RequestBody CourseDTO courseDTO
    ) {
        return courseService.update(id, courseDTO);
    }

    @Operation(
            summary = "Удалить курс",
            description = "Удаляет курс по указанному ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Курс успешно удален"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Курс не найден",
            content = @Content
    )
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID курса для удаления", required = true, example = "123")
            @PathVariable Long id
    ) {
        courseService.delete(id);
    }

    // Фильтрация по цене
    @Operation(
            summary = "Фильтр курсов по диапазону цен",
            description = "Возвращает курсы с ценой в указанном диапазоне [minPrice, maxPrice]"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/price/between")
    public List<CourseDTO> findByPriceBetween(
            @Parameter(description = "Минимальная цена", required = true, example = "100")
            @RequestParam Integer minPrice,
            @Parameter(description = "Максимальная цена", required = true, example = "500")
            @RequestParam Integer maxPrice
    ) {
        return courseService.findByPriceBetween(minPrice, maxPrice);
    }

    @Operation(
            summary = "Фильтр курсов по минимальной цене",
            description = "Возвращает курсы с ценой ≥ указанного значения"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/price/min")
    public List<CourseDTO> findByPriceGreaterThanEqual(
            @Parameter(description = "Минимальная цена", required = true, example = "200")
            @RequestParam Integer minPrice
    ) {
        return courseService.findByPriceGreaterThanEqual(minPrice);
    }

    @Operation(
            summary = "Фильтр курсов по максимальной цене",
            description = "Возвращает курсы с ценой ≤ указанного значения"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/price/max")
    public List<CourseDTO> findByPriceLessThanEqual(
            @Parameter(description = "Максимальная цена", required = true, example = "1000")
            @RequestParam Integer maxPrice
    ) {
        return courseService.findByPriceLessThanEqual(maxPrice);
    }

    // Фильтрация по продолжительности
    @Operation(
            summary = "Фильтр курсов по минимальной продолжительности",
            description = "Возвращает курсы с продолжительностью ≥ указанного значения (в часах)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/duration/min")
    public List<CourseDTO> findByDurationGreaterThanEqual(
            @Parameter(description = "Минимальная продолжительность (часы)", required = true, example = "10")
            @RequestParam Integer minDuration
    ) {
        return courseService.findByDurationGreaterThanEqual(minDuration);
    }

    @Operation(
            summary = "Фильтр курсов по максимальной продолжительности",
            description = "Возвращает курсы с продолжительностью ≤ указанного значения (в часах)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/duration/max")
    public List<CourseDTO> findByDurationLessThanEqual(
            @Parameter(description = "Максимальная продолжительность (часы)", required = true, example = "50")
            @RequestParam Integer maxDuration
    ) {
        return courseService.findByDurationLessThanEqual(maxDuration);
    }

    // Фильтрация по активности
    @Operation(
            summary = "Фильтр курсов по активности",
            description = "Возвращает активные/неактивные курсы в зависимости от параметра"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный результат фильтрации",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
    )
    @GetMapping("/filter/active")
    public List<CourseDTO> findByActive(
            @Parameter(description = "Статус активности курса", required = true, example = "true")
            @RequestParam Boolean active
    ) {
        return courseService.findByActive(active);
    }
}