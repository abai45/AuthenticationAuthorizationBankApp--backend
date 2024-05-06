package kz.group.reactAndSpring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.dto.LoginRequestDto;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static kz.group.reactAndSpring.constant.Constants.LOGIN_PATH;
import static kz.group.reactAndSpring.domain.ApiAuthentication.unauthenticated;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_ATTEMPT;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_SUCCESS;
import static kz.group.reactAndSpring.enumeration.TokenType.ACCESS;
import static kz.group.reactAndSpring.enumeration.TokenType.REFRESH;
import static kz.group.reactAndSpring.utils.RequestUtils.getResponse;
import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()), authenticationManager);
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequestDto.class);
            userService.updateLoginAttempt(user.getEmail(), LOGIN_ATTEMPT);
            var authentication = unauthenticated(user.getEmail(), user.getPassword());
            return getAuthenticationManager().authenticate(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
            handleErrorResponse(request, response, e);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        var user = (UserDto) authentication.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESS);
        var httpResponse = user.isMfa() ? sendQrCode(request, user) : sendResponse(request, response, user);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        var out = response.getOutputStream();
        var mapper = new ObjectMapper();
        mapper.writeValue(out, httpResponse);
        out.flush();
    }

    private Object sendResponse(HttpServletRequest request, HttpServletResponse response, UserDto user) {
        jwtService.addCokie(response,user, ACCESS);
        jwtService.addCokie(response,user, REFRESH);
        return getResponse(request, Map.of("user",user), "Login Success", OK);
    }

    private Object sendQrCode(HttpServletRequest request, UserDto user) {
        return getResponse(request, Map.of("user",user), "Please enter QR code", OK);
    }

//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
//        super.unsuccessfulAuthentication(request, response, failed);
//    }
}
