package com.training.app.controller;

import com.training.app.exception.UnsupportedFileType;
import com.training.app.exception.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().errorMessage(ex.getMessage()).build());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(Throwable ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder().errorMessage(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(UnsupportedFileType.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedFile(UnsupportedFileType ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().errorMessage(ex.getMessage()).build());
    }
}
