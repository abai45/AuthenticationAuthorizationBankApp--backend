package kz.group.reactAndSpring.service;

public interface EmailService {
    void sendNewAccountHtmlPage(String name, String otpCode, String email, String key);
    void sendPasswordResetEmailHtmlPage(String firstName, String email, String key);
    void sendOtpMessageHtmlPage(String name, String email, String otpCode);
    void sendIpAddressVerify(String name, String email, String key, String clientIp);
}
