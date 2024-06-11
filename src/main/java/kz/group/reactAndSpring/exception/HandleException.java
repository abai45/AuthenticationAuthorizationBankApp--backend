package kz.group.reactAndSpring.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionalException;
import kz.group.reactAndSpring.domain.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    private final HttpServletRequest request;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
        log.error(String.format("handleExceptionInternal: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(),getRootCauseMessage(ex), request,statusCode), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
        log.error(String.format("handleMethodArgumentNotValid: %s", ex.getMessage()));
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var fieldMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(handleErrorResponse(fieldMessage, getRootCauseMessage(ex), request, statusCode), statusCode);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response> apiException(ApiException ex) {
        log.error(String.format("ApiException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> badCredentialsException(BadCredentialsException ex) {
        log.error(String.format("BadCredentialsException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Response> sqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error(String.format("SQLIntegrityConstraintViolationException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Response> unrecognizedPropertyException(UnrecognizedPropertyException ex) {
        log.error(String.format("UnrecognizedPropertyException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> accessDeniedException(AccessDeniedException ex) {
        log.error(String.format("AccessDeniedException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("Access denied. You don't have access", getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> exception(Exception ex) {
        log.error(String.format("Exception: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(ex), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Response> transactionSystemException(TransactionSystemException ex) {
        log.error(String.format("TransactionSystemException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(ex), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Response> emptyResultDataAccessException(EmptyResultDataAccessException ex) {
        log.error(String.format("EmptyResultDataAccessException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<Response> credentialsExpiredException(CredentialsExpiredException ex) {
        log.error(String.format("CredentialsExpiredException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Response> disabledException(DisabledException ex) {
        log.error(String.format("DisabledException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse("User account is disabled", getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Response> lockedException(LockedException ex) {
        log.error(String.format("LockedException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(ex.getMessage(), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Response> duplicateKeyException(DuplicateKeyException ex) {
        log.error(String.format("DuplicateKeyException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(ex), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> dataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error(String.format("DataIntegrityViolationException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(ex), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Response> dataAccessException(DataAccessException ex) {
        log.error(String.format("DataAccessException: %s", ex.getMessage()));
        return new ResponseEntity<>(handleErrorResponse(processErrorMessage(ex), getRootCauseMessage(ex), request, BAD_REQUEST), BAD_REQUEST);
    }

    private String processErrorMessage(Exception ex) {
        if(ex instanceof ApiException) {
            return ex.getMessage();
        }
        if(ex.getMessage()!=null) {
            if(ex.getMessage().contains("duplicate") && ex.getMessage().contains("AccountVerifications")) {
                return "You already verified your account.";
            }
            if(ex.getMessage().contains("duplicate") && ex.getMessage().contains("ResetPasswordVerifications")) {
                return "We already sent to your email password reset link.";
            }
            if(ex.getMessage().contains("duplicate") && ex.getMessage().contains("Key (email)")) {
                return "Email already exists. Use a different email.";
            }
            if(ex.getMessage().contains("duplicate")) {
                return "Duplicate entry. Please try again.";
            }
        }
        return "An error occurred. Please try again";
    }
}
