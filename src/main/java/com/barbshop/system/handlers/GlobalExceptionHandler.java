package com.barbshop.system.handlers;

import com.barbshop.system.exception.*;
import org.apache.catalina.connector.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Criando uma exceção caso o corte não seja encontrado
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException notFoundException)
    {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(notFoundException.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);


    }

    //Criando resposta caso ocorra um erro no envio dos dados do corte
    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest badReponse)
    {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(badReponse.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);


    }

    //Criando resposta cado o pagamento não foi aprovado
    @ExceptionHandler(PaymentPending.class)
    public ResponseEntity<ErrorResponse> handlePaymentPending(PaymentPending paymentPending)
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(paymentPending.getMessage())
                .status(HttpStatus.PAYMENT_REQUIRED.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYMENT_REQUIRED);
    }


    //Criado para erros de autenticação
    @ExceptionHandler(BadAuthentication.class)
    public ResponseEntity<ErrorResponse> handleBadAuthenticationResponse(BadAuthentication badAuth)
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(badAuth.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "A operação viola uma regra de integridade.",
                        HttpStatus.CONFLICT.value()
                ));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "O agendamento foi alterado por outra operação. Tente novamente.",
                        HttpStatus.CONFLICT.value()
                ));
    }

    //Validacao do cloudflare
    @ExceptionHandler(ValidacaoCloudflare.class)
    public ResponseEntity<ErrorResponse> handlerValidacaoCloudflare (ValidacaoCloudflare exception)
    {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception.getMessage(),
                        HttpStatus.FORBIDDEN.value()));

    }
}
