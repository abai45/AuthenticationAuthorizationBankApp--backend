package kz.group.reactAndSpring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.api.GeoLocation;
import kz.group.reactAndSpring.api.GeoLocationApi;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.dto.LoginRequestDto;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.service.EncryptionService;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyMap;
import static kz.group.reactAndSpring.constant.Constants.LOGIN_PATH;
import static kz.group.reactAndSpring.domain.ApiAuthentication.unauthenticated;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_ATTEMPT;
import static kz.group.reactAndSpring.enumeration.LoginType.LOGIN_SUCCESS;
import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final GeoLocationApi geoLocationApi;

    public ApiAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, EncryptionService encryptionService, GeoLocationApi geoLocationApi) {
        super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()));
        this.jwtService = jwtService;
        this.userService = userService;
        this.encryptionService = encryptionService;
        this.geoLocationApi = geoLocationApi;
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
        var clientIp = request.getRemoteAddr();
        var user = (UserDto) authentication.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESS);
        var encryptedIpAddress = user.getLocationAddress();
//        var currentCity = geoLocationApi.geoLocationApi(clientIp).getCity();
//        if (encryptedIpAddress != null && !encryptedIpAddress.isEmpty()) {
//            var decryptedIpAddress = encryptionService.decrypt(encryptedIpAddress);
//            var dbCity = geoLocationApi.geoLocationApi(decryptedIpAddress).getCity();
//            if (dbCity.equals(currentCity)) {
//                if(user.isMfa()) {
//                    sendOtpCode(request,response,user);
//                } else {
//                    sendResponse(request,response,user);
//                }
//            } else {
//                sendLocationValidateLink(request, response, user);
//            }
        if(encryptedIpAddress != null && !encryptedIpAddress.isEmpty()) {
            var decryptedIpAddress = encryptionService.decrypt(encryptedIpAddress);
            if(decryptedIpAddress.equals(clientIp)) {
                if(user.isMfa()) {
                    sendOtpCode(request,response,user);
                } else {
                    sendResponse(request,response,user);
                }
            } else {
                sendLocationValidateLink(request,response,user);
            }
        } else {
            log.warn("User does not have a stored IP address or it is empty.");
            handleErrorResponse(request, response, new RuntimeException("Stored IP address is null or empty"));
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

    private void sendLocationValidateLink(HttpServletRequest request, HttpServletResponse response, UserDto user) throws IOException {
        var clientIp = request.getRemoteAddr();
        userService.sendLocationValidateLink(user.getEmail(), clientIp);
        Response responseBody = new Response(
                now().format(DateTimeFormatter.ISO_DATE_TIME),
                OK.value(),
                request.getRequestURI(),
                OK,
                "Please, update your new location in your email",
                null,
                emptyMap()
        );
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), responseBody);
    }
}
