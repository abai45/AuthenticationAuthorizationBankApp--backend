package kz.group.reactAndSpring.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class JwtConfiguration {
    @Value("${JWT_EXPIRATION}")
    private Long expiration;
    @Value("${jwt.secret}")
    private String secret;
}
