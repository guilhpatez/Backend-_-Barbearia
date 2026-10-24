package com.barbshop.system.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
