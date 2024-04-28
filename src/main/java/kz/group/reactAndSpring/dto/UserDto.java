package kz.group.reactAndSpring.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kz.group.reactAndSpring.entity.RoleEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private Long createdBy;
    private Long updatedBy;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String createdAt;
    private String updatedAt;
    private String roles;
    private String authorithies;
    private String imgUrl;
    private String qrCodeImageUri;
    private String lastLogin;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private boolean mfa;
}
