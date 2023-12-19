package com.portal.ratelimit.exception;


import java.io.Serial;

public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String string) {
        super(string);
    }
}