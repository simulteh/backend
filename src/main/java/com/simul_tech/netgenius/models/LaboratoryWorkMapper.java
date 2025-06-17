package com.simul_tech.netgenius.models;

import org.springframework.stereotype.Component;

@Component
public class LaboratoryWorkMapper {
    
    public static LaboratoryWorkDto toDto(LaboratoryWork work) {
        if (work == null) {
            return null;
        }

        LaboratoryWorkDto dto = new LaboratoryWorkDto();
        dto.setId_work(work.getId_work());
        dto.setId_user(work.getId_user());
        dto.setId_recipient(work.getId_recipient());
        dto.setId_departure(work.getId_departure());
        dto.setFile_name(work.getFile_name());
        dto.setFile_size(work.getFile_size());
        dto.setFile_type(work.getFile_type());
        dto.setComment(work.getComment());
        dto.setStatus(work.getStatus());
        dto.setGrade(work.getGrade());
        dto.setIs_closed(work.getIs_closed());
        
        return dto;
    }

    public static LaboratoryWork toEntity(LaboratoryWorkDto dto) {
        if (dto == null) {
            return null;
        }

        LaboratoryWork work = new LaboratoryWork();
        work.setId_work(dto.getId_work());
        work.setId_user(dto.getId_user());
        work.setId_recipient(dto.getId_recipient());
        work.setId_departure(dto.getId_departure());
        work.setFile_name(dto.getFile_name());
        work.setFile_size(dto.getFile_size());
        work.setFile_type(dto.getFile_type());
        work.setComment(dto.getComment());
        work.setStatus(dto.getStatus());
        work.setGrade(dto.getGrade());
        work.setIs_closed(dto.getIs_closed());
        
        return work;
    }
} 