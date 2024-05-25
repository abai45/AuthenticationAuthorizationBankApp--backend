package kz.group.reactAndSpring.service.impl;

import jakarta.mail.internet.MimeMessage;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static kz.group.reactAndSpring.utils.EmailUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String RESET_PASSWORD_REQUEST = "Reset Password Request";
    private static final String VERIFY_OTP_LOGIN = "Verify OTP Login";
    private static final String VERIFY_IP_ADDRESS = "Verify your new location";
    public static final String EMAIL_VERIFY_TEMPLATE = "emailVerify";
    public static final String EMAIL_RESET_TEMPLATE = "emailReset";
    public static final String EMAIL_OTP_TEMPLATE = "emailOtp";
    public static final String EMAIL_IP_LOCATION_TEMPLATE = "emailIpVerify";
    public static final String UTF_8_ENCODING = "UTF-8";
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountHtmlPage(String name, String otpCode, String email, String key) {
        try {
            Context context = new Context();
            context.setVariables(Map.of("name",name,"otpCode",otpCode,"url",getVerificationUrl(host, key)));
            String text = templateEngine.process(EMAIL_VERIFY_TEMPLATE, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage, true, UTF_8_ENCODING);
            message.setPriority(1);
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmailHtmlPage(String name, String email, String key) {
        try {
            Context context = new Context();
            context.setVariables(Map.of("name",name,"url",getResetPasswordUrl(host, key)));
            String text = templateEngine.process(EMAIL_RESET_TEMPLATE, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage, true, UTF_8_ENCODING);
            message.setPriority(1);
            message.setSubject(RESET_PASSWORD_REQUEST);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendOtpMessageHtmlPage(String name, String email, String otpCode) {
        try {
            Context context = new Context();
            context.setVariables(Map.of("name", name, "otpCode", otpCode));
            String text = templateEngine.process(EMAIL_OTP_TEMPLATE, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage, true, UTF_8_ENCODING);
            message.setSubject(VERIFY_OTP_LOGIN);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email");
        }
    }

    @Override
    @Async
    public void sendIpAddressVerify(String name, String email, String key) {
        try {
            Context context = new Context();
            context.setVariables(Map.of("name", name, "url", getVerificationUrl(host, key)));
            String text = templateEngine.process(EMAIL_IP_LOCATION_TEMPLATE, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage, true, UTF_8_ENCODING);
            message.setSubject(VERIFY_IP_ADDRESS);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email");
        }
    }
}
