package com.wasac.billing.dto.request;

import com.wasac.billing.domain.enums.RoleName;
import com.wasac.billing.domain.enums.UserStatus;
import com.wasac.billing.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Create or update staff user (ADMIN only)")
public class UserRequest {

    @NotBlank
    @Size(max = 150)
    @Schema(example = "Finance Officer Two")
    private String fullNames;

    @NotBlank
    @Email(message = "Invalid email format")
    @Schema(example = "finance2@wasac.com")
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MESSAGE)
    @Schema(example = "0788999888")
    private String phoneNumber;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(example = "password123", description = "Required on create, optional on update")
    private String password;

    @Schema(example = "ACTIVE")
    private UserStatus status;

    @NotEmpty(message = "At least one role is required")
    @Schema(example = "[\"ROLE_FINANCE\"]")
    private Set<RoleName> roles;
}
