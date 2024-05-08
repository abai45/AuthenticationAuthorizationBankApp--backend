package kz.group.reactAndSpring.exception;

import org.springframework.security.authentication.LockedException;

public class LockedExceptionClass extends LockedException {
    public LockedExceptionClass(String message) {
        super(message);
    }
    public LockedExceptionClass() {
        super("An error occured");
    }
}
