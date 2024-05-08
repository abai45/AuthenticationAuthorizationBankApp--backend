package kz.group.reactAndSpring.security;

import kz.group.reactAndSpring.domain.ApiAuthentication;
import kz.group.reactAndSpring.domain.UserPrincipal;
import kz.group.reactAndSpring.exception.*;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import static kz.group.reactAndSpring.constant.Constants.NINETY_DAYS;
import static kz.group.reactAndSpring.domain.ApiAuthentication.*;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var apiAuth = apiAuthenticationFunction.apply(authentication);
        var user = userService.getUserByEmail(apiAuth.getEmail());
        if(user != null) {
            var userCredential = userService.getUserCredentialById(user.getId());
            if(userCredential.getUpdatedAt().minusDays(NINETY_DAYS).isAfter(LocalDateTime.now())) {
                throw new ApiException("Credential expired. Please reset your password.");
            }
            var userPrincipal = new UserPrincipal(user, userCredential);
            validAccount.accept(userPrincipal);
            if(encoder.matches(apiAuth.getPassword(), userCredential.getPassword())) {
                return authenticated(user, userPrincipal.getAuthorities());
            } else {
                throw new BadCredentialsException("Email or password is incorrect.");
            }
        } else {
            throw new ApiException("Unable to authentication");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiAuthentication.class.isAssignableFrom(authentication);
    }

    private final Function<Authentication, ApiAuthentication> apiAuthenticationFunction = authentication -> (ApiAuthentication) authentication;

    private final Consumer<UserPrincipal> validAccount = userPrincipal -> {
        if(!userPrincipal.isAccountNonLocked()) {
            throw new LockedException("Your account is locked");
        }
        if(!userPrincipal.isEnabled()) {
            throw new DisabledException("Your account is disabled");
        }
        if(!userPrincipal.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Your password has expired. Please reset your password");
        }
        if(!userPrincipal.isAccountNonExpired()) {
            throw new DisabledException("Your account has expired. Please contact with administrator");
        }
    };
}
