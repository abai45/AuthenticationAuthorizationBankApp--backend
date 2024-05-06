package kz.group.reactAndSpring.service.impl;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.TokenService;
import kz.group.reactAndSpring.domain.Token;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;

    public TokenServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public UserTokenResponseDto generateTokens(UserDto user) {
        String accessToken = jwtService.createToken(user, Token::getAccess_token);
        String refreshToken = jwtService.createToken(user, Token::getRefresh_token);
        UserTokenResponseDto userTokenResponseDto = new UserTokenResponseDto();
        userTokenResponseDto.setAccessToken(accessToken);
        userTokenResponseDto.setRefreshToken(refreshToken);
        userTokenResponseDto.setUserDto(user);
        return userTokenResponseDto;
    }
}