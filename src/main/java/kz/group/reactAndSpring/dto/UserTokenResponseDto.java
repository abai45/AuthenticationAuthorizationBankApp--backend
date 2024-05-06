package kz.group.reactAndSpring.dto;

import lombok.Data;

@Data
public class UserTokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserDto userDto;
}
