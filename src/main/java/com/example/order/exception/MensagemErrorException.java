package com.example.order.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MensagemErrorException extends RuntimeException {
    private final HttpStatus status;

    public MensagemErrorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}