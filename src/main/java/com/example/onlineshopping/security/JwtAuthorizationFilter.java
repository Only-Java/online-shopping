package com.example.onlineshopping.security;

import com.example.onlineshopping.user.entity.User;
import com.example.onlineshopping.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = bearer.substring(7);

        Claims claims = jwtService.claims(token);
        String phoneNumber = claims.getSubject();
        User user = userRepository
                .findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with phone number %s not found", phoneNumber)));
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user , null, user.getRole().getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response) ;
    }
}
