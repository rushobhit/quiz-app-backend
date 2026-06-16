package com.quizmicroservice.security;

import com.quizmicroservice.model.User;
import com.quizmicroservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);

            if (!StringUtils.hasText(username) ||
                    SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String normalizedEmail = username.trim().toLowerCase();
            User user = userService.findByEmail(normalizedEmail);

            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.isTokenValid(token, normalizedEmail)) {
                filterChain.doFilter(request, response);
                return;
            }

            String roleFromToken = null;
            try {
                roleFromToken = jwtService.extractRole(token);
            } catch (Exception ignored) {
            }

            String resolvedRole = StringUtils.hasText(roleFromToken)
                    ? roleFromToken.trim().toUpperCase()
                    : (user.getRole() != null ? user.getRole().trim().toUpperCase() : "");

            if (!StringUtils.hasText(resolvedRole)) {
                filterChain.doFilter(request, response);
                return;
            }

            String springRole = resolvedRole.startsWith("ROLE_")
                    ? resolvedRole
                    : "ROLE_" + resolvedRole;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            null,
                            List.of(new SimpleGrantedAuthority(springRole))
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            // invalid token => continue unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}