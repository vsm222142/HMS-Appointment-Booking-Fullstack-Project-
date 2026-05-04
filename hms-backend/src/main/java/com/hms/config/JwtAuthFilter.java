package com.hms.config;

import com.hms.repository.UserRepository;
import com.hms.util.CookieUtil;
import com.hms.util.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractHeaderToken(request);
        if (token == null || token.isBlank()) {
            token = extractCookieToken(request.getCookies());
        }
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            if (email == null || role == null || userId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Ensure user still exists
            if (userRepository.findByEmail(email).isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            // Ignore invalid/expired token
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookieToken(Cookie[] cookies) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (CookieUtil.AUTH_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private String extractHeaderToken(HttpServletRequest request) {
        // Required by frontend spec: token header named "token"
        String t = request.getHeader("token");
        if (t != null && !t.isBlank()) return t;
        // Also allow standard Authorization: Bearer <token>
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        return null;
    }
}

