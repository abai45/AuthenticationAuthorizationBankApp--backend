package kz.group.reactAndSpring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.domain.UserPrincipal;
import kz.group.reactAndSpring.dto.LoginRequestDto;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.service.EmailService;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static java.time.LocalDateTime.now;
import static kz.group.reactAndSpring.constant.Constants.LOGIN_PATH;
import static kz.group.reactAndSpring.domain.ApiAuthentication.unauthenticated;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_ATTEMPT;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_SUCCESS;
import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;
import static kz.group.reactAndSpring.utils.UserUtils.generateOtpCode;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public ApiAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()));
        this.jwtService = jwtService;
        this.userService = userService;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        try {
            LoginRequestDto loginRequestDto = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true)
                    .readValue(request.getInputStream(), LoginRequestDto.class);
            userService.updateLoginAttempt(loginRequestDto.getEmail(), LOGIN_ATTEMPT);
            Authentication authentication = unauthenticated(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return getAuthenticationManager().authenticate(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
            handleErrorResponse(request, response, e);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        UserDto user = (UserDto) authentication.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESS);
        if (user.isMfa()) {
            sendOtpCode(request, response, user);
        } else {
            sendResponse(request, response, user);
        }
    }

    private void sendResponse(HttpServletRequest request, HttpServletResponse response, UserDto user) throws IOException {
        var accessToken = jwtService.createToken(user, Token::getAccess_token);
        var refreshToken = jwtService.createToken(user, Token::getRefresh_token);
        UserTokenResponseDto userTokenResponseDto = new UserTokenResponseDto();
        userTokenResponseDto.setAccessToken(accessToken);
        userTokenResponseDto.setRefreshToken(refreshToken);
        userTokenResponseDto.setUserDto(user);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), userTokenResponseDto);
    }

    private void sendOtpCode(HttpServletRequest request, HttpServletResponse response, UserDto user) throws IOException {
        userService.sendOtpCodeMessage(user.getEmail());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), user);
    }
}
