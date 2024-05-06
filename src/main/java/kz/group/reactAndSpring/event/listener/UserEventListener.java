package kz.group.reactAndSpring.event.listener;

import kz.group.reactAndSpring.event.UserEvent;
import kz.group.reactAndSpring.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent userEvent) {
        switch (userEvent.getType()) {
            case REGISTRATION -> emailService.sendNewAccountEmail(userEvent.getUser().getFirstName(),userEvent.getUser().getOtpCode(), userEvent.getUser().getEmail(), (String)userEvent.getData().get("key"));
            case RESETPASSWORD -> emailService.sendPasswordResetEmail(userEvent.getUser().getFirstName(), userEvent.getUser().getEmail(), (String)userEvent.getData().get("key"));
            default -> {}
        }
    }
}
