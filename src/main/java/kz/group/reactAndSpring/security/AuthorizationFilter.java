package kz.group.reactAndSpring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.ApiAuthentication;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.domain.TokenData;
import kz.group.reactAndSpring.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static kz.group.reactAndSpring.constant.Constants.OPTIONS_HTTP_METHOD;
import static kz.group.reactAndSpring.constant.Constants.PUBLIC_URLS;
import static kz.group.reactAndSpring.enumeration.TokenType.ACCESS;
import static kz.group.reactAndSpring.enumeration.TokenType.REFRESH;
import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var accessToken = jwtService.extractToken(request, ACCESS.getValue());
            if(accessToken.isPresent() && jwtService.getTokenData(accessToken.get(), TokenData::isValid)) {
                SecurityContextHolder.getContext().setAuthentication(getAuthentication(accessToken.get(),request));
                RequestContext.setUserId(jwtService.getTokenData(accessToken.get(), TokenData::getUser).getId());
            } else {
                var refreshToken = jwtService.extractToken(request, REFRESH.getValue());
                if(refreshToken.isPresent() && jwtService.getTokenData(refreshToken.get(), TokenData::isValid)) {
                    var user = jwtService.getTokenData(refreshToken.get(), TokenData::getUser);
                    SecurityContextHolder.getContext().setAuthentication(getAuthentication(jwtService.createToken(user, Token::getAccess_token), request));
                    jwtService.addCokie(response,user,ACCESS);
                    RequestContext.setUserId(user.getId());
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            handleErrorResponse(request, response, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        var shouldNotFilter = request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD) || Arrays.asList(PUBLIC_URLS).contains(request.getRequestURI());
        if(shouldNotFilter) {
            RequestContext.setUserId(0L);
        }
        return shouldNotFilter;
    }

    private Authentication getAuthentication(String token, HttpServletRequest request) {
        var authentication = ApiAuthentication.authenticated(jwtService.getTokenData(token, TokenData::getUser), jwtService.getTokenData(token, TokenData::getAuthorities));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}
