package com.simul_tech.netgenius.mappers;

import com.simul_tech.netgenius.entity.Grade;
import com.simul_tech.netgenius.models.GradeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {
    GradeDTO toDto(Grade grade); // Преобразование сущности Grade в GradeDTO
    Grade toEntity(GradeDTO gradeDTO); // Преобразование GradeDTO в сущность Grade
}
