package com.simul_tech.netgenius.enums;

/**
 * Роль пользователя. Один пользователь имеет ровно одну роль.
 * Переход возможен только USER -> STUDENT или USER -> TEACHER.
 * GUEST моделируется отсутствием аутентификации и в БД не хранится.
 */
public enum Role {
    USER,
    STUDENT,
    TEACHER,
    ADMIN
}
