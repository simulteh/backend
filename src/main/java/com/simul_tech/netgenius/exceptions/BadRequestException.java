package com.simul_tech.netgenius.exceptions;

/** Некорректный запрос / нарушение предусловия -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
