package com.wasac.billing.dto.request;

import com.wasac.billing.domain.enums.CustomerStatus;
import com.wasac.billing.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank
    @Size(max = 150)
    private String fullNames;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.NATIONAL_ID, message = ValidationPatterns.NATIONAL_ID_MESSAGE)
    private String nationalId;

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MESSAGE)
    private String phoneNumber;

    @NotBlank
    private String address;

    private CustomerStatus status;

    private Long userId;
}
