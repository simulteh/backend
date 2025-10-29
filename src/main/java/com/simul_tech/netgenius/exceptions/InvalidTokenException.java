package com.simul_tech.netgenius.exceptions;

public class InvalidTokenException extends PasswordResetException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
