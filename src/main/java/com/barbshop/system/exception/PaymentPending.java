package com.barbshop.system.exception;

import lombok.Getter;

@Getter
public class PaymentPending extends RuntimeException {
    public PaymentPending(String message) {
        super(message);
    }
}
