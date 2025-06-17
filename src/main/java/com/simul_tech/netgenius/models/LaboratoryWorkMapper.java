package com.simul_tech.netgenius.models;

public class LaboratoryWorkMapper {
    public static LaboratoryWorkDto toDto(LaboratoryWork work) {
        if (work == null) return null;
        LaboratoryWorkDto dto = new LaboratoryWorkDto();
        dto.setId_work(work.getId_work());
        dto.setId_user(work.getId_user());
        dto.setId_recipient(work.getId_recipient());
        dto.setId_departure(work.getId_departure());
        dto.setId_time(work.getId_time());
        dto.setFile_name(work.getFile_name());
        dto.setFile_size(work.getFile_size());
        dto.setFile_type(work.getFile_type());
        dto.setComment(work.getComment());
        dto.setStatus(work.getStatus());
        dto.setGrade(work.getGrade());
        dto.setIs_closed(work.getIs_closed());
        return dto;
    }
} 