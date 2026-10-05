package com.sohel.cloudstorage.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.enums.AccountStatus;
import com.sohel.cloudstorage.enums.Role;
import com.sohel.cloudstorage.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUsernameFromJwtToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    setAuthentication(userDetails, request);
                } else {
                    authenticateSupabaseUser(jwt, request);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateSupabaseUser(String jwt, HttpServletRequest request) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return;

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode claims = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));

            if (!claims.has("sub") || !claims.has("exp")) return;

            long exp = claims.get("exp").asLong();
            if (Instant.now().getEpochSecond() > exp) {
                log.warn("Supabase JWT is expired");
                return;
            }

            String iss = claims.has("iss") ? claims.get("iss").asText() : "";
            if (!iss.contains("supabase.co") && !iss.contains("/auth/v1")) {
                return;
            }

            UUID userId = UUID.fromString(claims.get("sub").asText());
            String email = claims.has("email") ? claims.get("email").asText() : null;

            UserEntity user = userRepository.findById(userId)
                    .or(() -> email != null ? userRepository.findByEmail(email) : Optional.empty())
                    .orElse(null);

            if (user == null) {
                String fullName = "";
                if (claims.has("user_metadata") && claims.get("user_metadata").has("full_name")) {
                    fullName = claims.get("user_metadata").get("full_name").asText();
                }
                String username = (email != null ? email.split("@")[0] : "user") + "_" + userId.toString().substring(0, 6);
                user = UserEntity.builder()
                        .id(userId)
                        .username(username)
                        .email(email != null ? email : userId + "@cloudstorage.local")
                        .password("SUPABASE_AUTH")
                        .firstName(fullName.isEmpty() ? "User" : fullName)
                        .lastName("")
                        .role(Role.ROLE_USER)
                        .status(AccountStatus.ACTIVE)
                        .emailVerified(true)
                        .storageUsed(0L)
                        .storageLimit(16106127360L)
                        .build();
            }

            UserDetails userDetails = new CustomUserDetails(user);
            setAuthentication(userDetails, request);
        } catch (Exception e) {
            log.error("Failed to parse/authenticate Supabase JWT: {}", e.getMessage());
        }
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
