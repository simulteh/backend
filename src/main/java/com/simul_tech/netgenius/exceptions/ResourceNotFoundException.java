package com.simul_tech.netgenius.exceptions;

/** Запрашиваемый ресурс не найден -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
