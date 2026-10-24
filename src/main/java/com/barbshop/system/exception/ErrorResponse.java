package com.barbshop.system.exception;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse
{
    private String message;
    private int status;
}
