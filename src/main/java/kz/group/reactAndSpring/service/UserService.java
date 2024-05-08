package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.LoginType;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
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
    void verifyOtpCode(String otpCode);
    UserDto setMfa(Long id);
    void saveOtpCode(String email, String otpCode);
    UserDto UserOtpVerify(String otpCode);
    UserDto updateUser(String userId, String firstName, String lastName, String email, String phone);
    void updateRole(String userId, String role);
    void deleteUser(String email);

    void toggleAccountExpired(String userId);
    void toggleAccountLocked(String userId);
    void toggleAccountEnabled(String userId);

}
