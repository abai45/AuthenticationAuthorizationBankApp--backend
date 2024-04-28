package kz.group.reactAndSpring.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Token {
    private String access_token;
    private String refresh_token;
}
