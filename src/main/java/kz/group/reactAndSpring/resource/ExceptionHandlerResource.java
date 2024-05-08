package kz.group.reactAndSpring.resource;

import kz.group.reactAndSpring.domain.ErrorResponse;
import kz.group.reactAndSpring.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.lang.Exception;

import static java.time.LocalDateTime.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


@ControllerAdvice
public class ExceptionHandlerResource {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(LockedExceptionClass.class)
    public ResponseEntity<ErrorResponse> handleLockedException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(DisabledExceptionClass.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(CredentialsExpiredExceptionClass.class)
    public ResponseEntity<ErrorResponse> handleCredentialsExpiredException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(BadCredentialsExceptionClass.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
}
