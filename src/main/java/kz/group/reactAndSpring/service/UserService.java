package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.enumeration.LoginType;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
    RoleEntity getRoleName(String name);
    void verifyAccountKey(String key);
    void updateLoginAttempt(String email, LoginType loginType);
    UserDto getUserByUserId(String userId);
}
