package kz.group.reactAndSpring.service;

public interface EmailService {
    void sendNewAccountEmail(String name, String otpCode, String email, String token);
    void sendPasswordResetEmail(String name, String email, String token);

    void sendOtpMessage(String email, String message);
}
