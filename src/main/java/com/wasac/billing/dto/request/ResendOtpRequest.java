package com.wasac.billing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Resend OTP to registered email")
public class ResendOtpRequest {

    @NotBlank
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;
}
