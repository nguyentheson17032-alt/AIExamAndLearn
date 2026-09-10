package com.aiexam.warehouse.auth;

import com.aiexam.warehouse.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthService.AuthResponse>> register(
            @Valid @RequestBody AuthService.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.AuthResponse> login(@Valid @RequestBody AuthService.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthService.AuthResponse> refresh(@Valid @RequestBody AuthService.RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }
}
