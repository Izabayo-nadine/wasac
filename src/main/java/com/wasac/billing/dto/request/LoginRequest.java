package com.wasac.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "admin@wasac.com", description = "Admin: admin@wasac.com / admin123")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "admin123")
    private String password;
}
