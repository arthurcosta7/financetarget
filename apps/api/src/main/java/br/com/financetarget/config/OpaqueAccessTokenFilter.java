package br.com.financetarget.config;

import br.com.financetarget.identity.application.IdentityRepository;
import br.com.financetarget.identity.application.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;

@Component
public class OpaqueAccessTokenFilter extends OncePerRequestFilter {
    private final IdentityRepository repository;
    private final TokenService tokenService;
    private final AuthProperties properties;
    private final Clock clock;

    public OpaqueAccessTokenFilter(IdentityRepository repository, TokenService tokenService,
                                   AuthProperties properties, Clock clock) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String rawToken = cookie(request, properties.accessCookieName());
        if (rawToken != null) {
            repository.findByAccessHash(tokenService.hash(rawToken), clock.instant()).ifPresent(account -> {
                var authentication = UsernamePasswordAuthenticationToken.authenticated(account, null, java.util.List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(request, response);
    }

    public static String cookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
