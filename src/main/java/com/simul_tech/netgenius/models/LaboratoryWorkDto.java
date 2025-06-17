package com.simul_tech.netgenius.models;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для лабораторной работы, используется для возврата данных клиенту.
 */
@Data
public class LaboratoryWorkDto {
    private Long id_work;
    private Long id_user;
    private Long id_recipient;
    private String id_departure;
    private LocalDateTime id_time;
    private String file_name;
    private Double file_size;
    private String file_type;
    private String comment;
    private String status;
    private Integer grade;
    private Boolean is_closed;
} 