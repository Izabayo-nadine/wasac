package com.wasac.billing.dto.request;

import com.wasac.billing.domain.enums.RoleName;
import com.wasac.billing.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Customer registration (public) or staff registration (admin with JWT)")
public class RegisterRequest {

    @NotBlank(message = "Full names are required")
    @Size(max = 150)
    @Schema(example = "John Doe")
    private String fullNames;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MESSAGE)
    @Schema(example = "0788123456")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(example = "password123")
    private String password;

    @Schema(
            description = "Optional. Public users: omit (gets ROLE_CUSTOMER). Admin only: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE",
            example = "[\"ROLE_OPERATOR\"]",
            allowableValues = {"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FINANCE", "ROLE_CUSTOMER"}
    )
    private Set<RoleName> roles;
}
