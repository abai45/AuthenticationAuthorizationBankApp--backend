package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.LoginType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password, String phone, String clientIp);
    RoleEntity getRoleName(String name);
    UserDto verifyAccountKey(String key);
    void updateLoginAttempt(String email, LoginType loginType);
    UserDto getUserByUserId(String userId);
    UserDto getUserByEmail(String email);
    CredentialEntity getUserCredentialById(Long id);
    void resetPassword(String email);
    void updatePassword(String userId, String password, String newPassword, String confirmNewPassword);
    UserDto verifyConfirmationKey(String key);
    void updatePassword(String userId, String newPassword, String confirmNewPassword);
    void verifyOtpCode(String email, String otpCode);
    UserDto setMfa(Long id);
    void sendOtpCodeMessage(String email);
    UserDto userOtpVerify(String email, String otpCode);
    UserDto updateUser(String userId, String firstName, String lastName, String email, String phone);
    void updateRole(String userId, String role);
    void deleteUser(String email);
    String uploadPhoto(String userId, MultipartFile file);

    void toggleAccountExpired(String userId);
    void toggleAccountLocked(String userId);
    void toggleAccountEnabled(String userId);

    List<UserDto> getUsers();

    void sendLocationValidateLink(String email);
}
