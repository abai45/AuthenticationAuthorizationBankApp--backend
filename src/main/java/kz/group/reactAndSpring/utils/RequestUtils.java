package kz.group.reactAndSpring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
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


    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response,Exception e) {
        if(e instanceof AccessDeniedException) {
            Response apiResponse = getErrorResponse(request, response, e, FORBIDDEN);
            writeResponse.accept(response, apiResponse);
        }
    }
    private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception e, HttpStatus status) {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), errorReason.apply(e, status), getRootCauseMessage(e),emptyMap());
    }
}
