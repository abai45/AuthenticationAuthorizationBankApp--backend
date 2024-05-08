package kz.group.reactAndSpring.exception;

import org.springframework.security.authentication.DisabledException;

public class DisabledExceptionClass extends DisabledException {
    public DisabledExceptionClass(String message) {
        super(message);
    }
    public DisabledExceptionClass() {
        super("An error occured");
    }
}
