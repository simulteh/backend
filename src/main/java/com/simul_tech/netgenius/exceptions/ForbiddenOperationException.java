package com.simul_tech.netgenius.exceptions;

/** Операция запрещена для текущего пользователя -> HTTP 403. */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
