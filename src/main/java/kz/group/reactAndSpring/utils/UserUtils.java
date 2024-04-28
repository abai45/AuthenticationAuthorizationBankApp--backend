package kz.group.reactAndSpring.utils;

import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;

import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class UserUtils {
    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .enabled(false)
                .loginAttempts(0)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)
                .imgUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                .roles(role)
                .build();
    }
}
