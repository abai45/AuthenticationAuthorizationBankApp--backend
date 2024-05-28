package kz.group.reactAndSpring.utils;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.EncryptionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static kz.group.reactAndSpring.constant.Constants.NINETY_DAYS;
import static kz.group.reactAndSpring.constant.Constants.OTP_LENGTH;

@Component
public class UserUtils {

    private static UserRepository userRepository;
    private static EncryptionService encryptionService;

    @Autowired
    public UserUtils(UserRepository userRepository, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public static UserEntity createUserEntity(String firstName, String lastName, String email, String phone,RoleEntity role, String clientIp) {
        String otpCode;
        do {
            otpCode = generateOtpCode();
        } while(isOtpCodeExists(otpCode));

        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(now())
                .tokenCreatedAt(now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .locationAddress(encryptionService.encrypt(clientIp))
                .enabled(false)
                .loginAttempts(0)
                .otpCode(encryptionService.encrypt(otpCode))
                .phone(phone)
                .imgUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                .roles(role)
                .build();
    }

    public static UserDto fromUserEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
        UserDto user = new UserDto();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialsNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRoles(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        return user;
    }

    private static boolean isOtpCodeExists(String otpCode) {
        return userRepository.existsByOtpCode(otpCode);
    }

    public static boolean isCredentialsNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now());
    }

    public static String generateOtpCode() {
        var otpCode = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otpCode.append(random.nextInt(10));
        }
        return otpCode.toString();
    }
}