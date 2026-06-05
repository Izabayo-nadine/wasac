package com.wasac.billing.controller;

import com.wasac.billing.config.OpenApiConfig;
import com.wasac.billing.dto.request.LoginRequest;
import com.wasac.billing.dto.request.RegisterRequest;
import com.wasac.billing.dto.request.ResendOtpRequest;
import com.wasac.billing.dto.request.VerifyOtpRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.JwtResponse;
import com.wasac.billing.dto.response.UserResponse;
import com.wasac.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Register, OTP verification, login, logout — start here in Swagger")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register customer (public) or staff (admin)",
            description = """
                    **Public (no Authorize):** omit `roles` → ROLE_CUSTOMER assigned, OTP sent to email.
                    
                    **Admin (with Bearer token):** include `roles`: ROLE_ADMIN, ROLE_OPERATOR, or ROLE_FINANCE.
                    """,
            security = {}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User created. Customers must verify OTP before login."
    )
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        String message = user.isEmailVerified()
                ? "Staff user registered successfully"
                : "Registration successful. Please verify your email with the OTP sent.";
        return ApiResponse.ok(message, user);
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify email with OTP",
            description = "Activates account after registration. Check email or server console for OTP when mail is disabled.",
            security = {}
    )
    public ApiResponse<UserResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        UserResponse user = authService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponse.ok("Email verified successfully. You can now log in.", user);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP to email", security = {})
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.getEmail());
        return ApiResponse.ok("OTP sent to your email", null);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login and get JWT token",
            description = "Copy `data.token` from response → click Authorize → paste `Bearer <token>`",
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                                        "type": "Bearer",
                                        "email": "admin@wasac.com",
                                        "roles": ["ROLE_ADMIN"]
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Logout and invalidate JWT token")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        authService.logout(token);
        return ApiResponse.ok("Logged out successfully", null);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
