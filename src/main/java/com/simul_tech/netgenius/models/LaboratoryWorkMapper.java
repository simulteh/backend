package com.simul_tech.netgenius.models;

import org.springframework.stereotype.Component;

@Component
public class LaboratoryWorkMapper {
    
    public static LaboratoryWorkDto toDto(LaboratoryWork work) {
        if (work == null) {
            return null;
        }

        LaboratoryWorkDto dto = new LaboratoryWorkDto();
        dto.setIdWork(work.getIdWork());
        dto.setIdUser(work.getIdUser());
        dto.setIdRecipient(work.getIdRecipient());
        dto.setStatus(work.getStatus());
        dto.setComment(work.getComment());
        dto.setGrade(work.getGrade());
        dto.setIsClosed(work.getIsClosed());
        dto.setFileType(work.getFileType());
        
        return dto;
    }

    public static LaboratoryWork toEntity(LaboratoryWorkDto dto) {
        if (dto == null) {
            return null;
        }

        LaboratoryWork work = new LaboratoryWork();
        work.setIdWork(dto.getIdWork());
        work.setIdUser(dto.getIdUser());
        work.setIdRecipient(dto.getIdRecipient());
        work.setStatus(dto.getStatus());
        work.setComment(dto.getComment());
        work.setGrade(dto.getGrade());
        work.setIsClosed(dto.getIsClosed());
        work.setFileType(dto.getFileType());
        
        return work;
    }
} 