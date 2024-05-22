package kz.group.reactAndSpring.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.domain.TokenData;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.enumeration.TokenType;
import kz.group.reactAndSpring.security.JwtConfiguration;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static kz.group.reactAndSpring.constant.Constants.*;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService {
    private final UserService userService;
    private final Supplier<SecretKey> key = () -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecret()));

    private final Function<String, Claims> claimsFunction = token ->
            Jwts.parser()
                    .verifyWith(key.get())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

    private <T> T getClaimsValue(String token, Function<Claims, T> claims) {
        return claimsFunction.andThen(claims).apply(token);
    }

    private final Function<String, String> subject = token ->
            getClaimsValue(token, Claims::getSubject);

    private final Supplier<JwtBuilder> builder = () ->
            Jwts.builder()
                    .header().add(Map.of(TYPE, JWT_TYPE))
                    .and()
                    .audience().add(BANK_APPLICATION)
                    .and()
                    .id(UUID.randomUUID().toString())
                    .issuedAt(Date.from(Instant.now()))
                    .notBefore(new Date())
                    .signWith(key.get(), Jwts.SIG.HS512);

    private final Function<UserDto, String> buildToken = userDto ->
            builder.get()
                    .subject(userDto.getUserId())
                    .claim(AUTHORITIES, userDto.getAuthorities())
                    .claim(ROLE, userDto.getRoles())
                    .expiration(Date.from(Instant.now().plusSeconds(getExpiration())))
                    .compact();

    @Override
    public String createToken(UserDto user, Function<Token, String> tokenFunction) {
        var token = Token.builder()
                .access_token(buildToken.apply(user))
                .refresh_token(buildToken.apply(user))
                .build();
        return tokenFunction.apply(token);
    }

    @Override
    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        return tokenFunction.apply(
                TokenData.builder()
                        .valid(Objects.equals(userService.getUserByUserId(subject.apply(token)).getUserId(), claimsFunction.apply(token).getSubject()))
                        .authorities(commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER)
                                .add(claimsFunction.apply(token).get(AUTHORITIES, String.class))
                                .add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE, String.class)).toString()))
                        .claims(claimsFunction.apply(token))
                        .user(userService.getUserByUserId(subject.apply(token)))
                        .build());
    }

//    @Override
//    public void removeHeader(HttpServletResponse response, String headerName) {
//        response.setHeader(headerName, "");
//    }
//
//    @Override
//    public Optional<String> extractToken(HttpServletRequest request, String headerName) {
//        String token = request.getHeader(headerName);
//        return Optional.ofNullable(token);
//    }
//
//    @Override
//    public void addHeader(HttpServletResponse response, UserDto user, TokenType type, String headerName) {
//        String tokenValue = createToken(user, token -> type == TokenType.ACCESS ? token.getAccess_token() : token.getRefresh_token());
//        response.setHeader(headerName, tokenValue);
//    }
//public Function<String, List<GrantedAuthority>> authorities = token ->
//        commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER)
//                .add(claimsFunction.apply(token).get(AUTHORITIES, String.class))
//                .add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE, String.class)).toString());
}

