package kz.group.reactAndSpring.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class BadCredentialsExceptionClass extends BadCredentialsException {
    public BadCredentialsExceptionClass(String message) {
        super(message);
    }
    public BadCredentialsExceptionClass() {
        super("An error occured");
    }
}
