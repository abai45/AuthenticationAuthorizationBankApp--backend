package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;

public interface TokenService {
    UserTokenResponseDto generateTokens(UserDto user);
}
