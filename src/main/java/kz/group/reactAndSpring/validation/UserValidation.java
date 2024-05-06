package kz.group.reactAndSpring.validation;

import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;

public class UserValidation {
    public static void verifyAccountStatus(UserEntity user) {
        if(!user.isEnabled()) {
            throw new ApiException("Account is not enabled");
        }
        if(!user.isAccountNonExpired()) {
            throw new ApiException("Account is expired");
        }
        if(!user.isAccountNonLocked()) {
            throw new ApiException("Account is locked");
        }
    }
}
