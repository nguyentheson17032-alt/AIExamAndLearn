package com.aiexam.warehouse.auth;

import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.common.exception.DuplicateResourceException;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("EMAIL_TAKEN", "Email is already registered");
        }
        User user = User.register(email, passwordEncoder.encode(request.password()), request.displayName());
        userRepository.save(user);
        return issueTokens(new UserPrincipal(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BusinessRuleViolationException("LOGIN_FAILED", "Invalid credentials"));
        return issueTokens(new UserPrincipal(user));
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String email = jwtService.validateRefreshTokenAndGetSubject(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BusinessRuleViolationException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        if (!stored.isActive() || !stored.getUser().getEmail().equals(email)) {
            stored.getFamilyId();
            refreshTokenRepository.findByFamilyId(stored.getFamilyId()).forEach(RefreshToken::revokeFamily);
            throw new BusinessRuleViolationException("REFRESH_TOKEN_REUSE", "Refresh token reuse detected");
        }
        UserPrincipal principal = new UserPrincipal(stored.getUser());
        AuthResponse response = issueTokens(principal, stored.getFamilyId());
        stored.revoke(null);
        return response;
    }

    private AuthResponse issueTokens(UserPrincipal principal) {
        return issueTokens(principal, UUID.randomUUID());
    }

    private AuthResponse issueTokens(UserPrincipal principal, UUID familyId) {
        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenExpiration());
        refreshTokenRepository.save(RefreshToken.issue(principal.getUser(), hash(refresh), familyId, expiresAt));
        return new AuthResponse(access, refresh, jwtProperties.accessTokenExpiration().toSeconds());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(min = 2, max = 100) String displayName
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record AuthResponse(String accessToken, String refreshToken, long expiresIn) {}
}
