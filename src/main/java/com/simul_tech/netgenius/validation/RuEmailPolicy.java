package com.simul_tech.netgenius.validation;

import com.simul_tech.netgenius.exceptions.InvalidRequestException;

import java.util.regex.Pattern;

/** Ограничение домена для новых аккаунтов и смены email; формат проверяется отдельно через @Email. */
public final class RuEmailPolicy {
    public static final String REGEXP = "^[^\\r\\n]+@[^@\\r\\n]+\\.[rR][uU]$";
    public static final String MESSAGE = "Разрешена почта только с доменом .ru";
    private static final Pattern ALLOWED = Pattern.compile(REGEXP);

    private RuEmailPolicy() { }

    public static void requireAllowed(String email) {
        if (email == null || !ALLOWED.matcher(email).matches()) {
            throw new InvalidRequestException(MESSAGE);
        }
    }
}
