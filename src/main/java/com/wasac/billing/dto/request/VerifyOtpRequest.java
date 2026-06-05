package com.wasac.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Verify email with 6-digit OTP received after registration")
public class VerifyOtpRequest {

    @NotBlank
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    @Schema(example = "482913", description = "6-digit code from email or server console log")
    private String otp;
}
