package com.barbshop.system.exception;

import lombok.Getter;

@Getter
public class BadAuthentication extends RuntimeException {
    public BadAuthentication(String message) {
        super(message);
    }
}
