package kz.group.reactAndSpring.exception;

import org.springframework.security.authentication.CredentialsExpiredException;

public class CredentialsExpiredExceptionClass extends CredentialsExpiredException {
    public CredentialsExpiredExceptionClass(String message) {
        super(message);
    }
    public CredentialsExpiredExceptionClass() {
        super("An error occured");
    }
}
