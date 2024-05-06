//package kz.group.reactAndSpring.service;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import kz.group.reactAndSpring.domain.Token;
//import kz.group.reactAndSpring.domain.TokenData;
//import kz.group.reactAndSpring.dto.UserDto;
//import kz.group.reactAndSpring.enumeration.TokenType;
//
//import java.util.Optional;
//import java.util.function.Function;
//
//public interface JwtService {
//    String createToken(UserDto user, Function<Token,String> tokenFunction);
//    Optional<String> extractToken(HttpServletRequest request, String cookieName);
//    void addCokie(HttpServletResponse response, UserDto user, TokenType type);
//    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);
//    void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
//}

package kz.group.reactAndSpring.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.domain.TokenData;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.enumeration.TokenType;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {
    String createToken(UserDto user, Function<Token,String> tokenFunction);
    Optional<String> extractToken(HttpServletRequest request, String headerName);
    void addHeader(HttpServletResponse response, UserDto user, TokenType type, String headerName);
    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);
    void removeHeader(HttpServletResponse response, String headerName);
}
