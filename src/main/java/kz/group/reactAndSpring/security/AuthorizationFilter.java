package kz.group.reactAndSpring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.group.reactAndSpring.domain.ApiAuthentication;
import kz.group.reactAndSpring.domain.RequestContext;
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

import static kz.group.reactAndSpring.utils.RequestUtils.handleErrorResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader(HEADER_NAME);
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                String token = authorizationHeader.substring(BEARER_PREFIX.length());
                if (jwtService.getTokenData(token, TokenData::isValid)) {
                    Authentication authentication = getAuthentication(token, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    RequestContext.setUserId(jwtService.getTokenData(token, TokenData::getUser).getId());
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
        return false;
    }

    private Authentication getAuthentication(String token, HttpServletRequest request) {
        TokenData tokenData = TokenData.builder()
                .user(jwtService.getTokenData(token, TokenData::getUser))
                .claims(jwtService.getTokenData(token, TokenData::getClaims))
                .valid(true)
                .authorities(jwtService.getTokenData(token, TokenData::getAuthorities))
                .build();
        Authentication authentication = ApiAuthentication.authenticated(tokenData.getUser(), tokenData.getAuthorities());
        ((ApiAuthentication) authentication).setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}
