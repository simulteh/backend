package com.simul_tech.netgenius.exceptions;

/** Конфликт состояния (нарушение бизнес-правила) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
