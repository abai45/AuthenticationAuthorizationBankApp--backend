package kz.group.reactAndSpring.resource;

import kz.group.reactAndSpring.domain.ErrorResponse;
import kz.group.reactAndSpring.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ErrorResponse> handleCredentialsExpiredException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(now());

        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
}
