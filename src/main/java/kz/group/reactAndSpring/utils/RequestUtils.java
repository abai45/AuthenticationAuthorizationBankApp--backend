package kz.group.reactAndSpring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequestUtils {

    private static final BiConsumer<HttpServletResponse, Response> writeResponse = (httpServletResponse, response) -> {
        try {
            var outputStream = httpServletResponse.getOutputStream();
            new ObjectMapper().writeValue(outputStream, response);
            outputStream.flush();
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    };

    private static final BiFunction<Exception, HttpStatus,String> errorReason = (exception, status) -> {
        if(status.isSameCodeAs(FORBIDDEN)) {
            return "You do not have permission to access this resource";
        }
        if(status.isSameCodeAs(UNAUTHORIZED)) {
            return "You are not authorized to access this resource";
        }
        if(exception instanceof DisabledException || exception instanceof LockedException || exception instanceof BadCredentialsException || exception instanceof CredentialsExpiredException || exception instanceof ApiException) {
            return exception.getMessage();
        }
        if (status.is5xxServerError()) {
            return "An error occurred while processing the request";
        } else {
            return "An error occurred. Please try again later.";
        }
    };

    public static Response getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {
        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), message, EMPTY, data);
    }

    public static Response handleErrorResponse(String message, String ex, HttpServletRequest request, HttpStatusCode status) {
        return new Response(now().toString(), status.value(),request.getRequestURI(), HttpStatus.valueOf(status.value()), message, ex, emptyMap());
    }

    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response,Exception e) {
        if(e instanceof AccessDeniedException) {
            var apiResponse = getErrorResponse(request, response, e, FORBIDDEN);
            writeResponse.accept(response, apiResponse);
        } else if (e instanceof InsufficientAuthenticationException) {
            var apiResponse = getErrorResponse(request, response, e, UNAUTHORIZED);
            writeResponse.accept(response, apiResponse);
        } else if (e instanceof MismatchedInputException) {
            var apiResponse = getErrorResponse(request, response, e, BAD_REQUEST);
            writeResponse.accept(response, apiResponse);
        } else if (e instanceof DisabledException || e instanceof LockedException || e instanceof BadCredentialsException || e instanceof CredentialsExpiredException || e instanceof ApiException) {
            var apiResponse = getErrorResponse(request, response, e, BAD_REQUEST);
            writeResponse.accept(response, apiResponse);
        } else {
            Response apiResponse = getErrorResponse(request, response, e, INTERNAL_SERVER_ERROR);
            writeResponse.accept(response, apiResponse);
        }
    }
    private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception e, HttpStatus status) {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), errorReason.apply(e, status), getRootCauseMessage(e),emptyMap());
    }
}
