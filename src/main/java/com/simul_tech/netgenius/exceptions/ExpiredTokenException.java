package com.simul_tech.netgenius.exceptions;

public class ExpiredTokenException extends PasswordResetException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
