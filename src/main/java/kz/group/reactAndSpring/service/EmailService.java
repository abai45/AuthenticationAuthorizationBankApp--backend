package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.entity.UserEntity;

public interface EmailService {
//    void sendNewAccountEmail(String name, String otpCode, String email, String token);
//    void sendPasswordResetEmail(String name, String email, String token);
//    void sendOtpMessage(String email, String message);
    void sendNewAccountHtmlPage(String name, String otpCode, String email, String key);
    void sendPasswordResetEmailHtmlPage(String firstName, String email, String key);
    void sendOtpMessageHtmlPage(String name, String email, String otpCode);
    void sendIpAddressVerify(String name, String email, String key);
}
