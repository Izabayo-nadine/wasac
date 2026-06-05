package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.Customer;
import com.wasac.billing.domain.enums.CustomerStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CustomerResponse {

    Long id;
    String fullNames;
    String nationalId;
    String email;
    String phoneNumber;
    String address;
    CustomerStatus status;
    Long userId;
    UserSummaryResponse user;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CustomerResponse from(Customer customer) {
        Long userId = customer.getUser() != null ? customer.getUser().getId() : null;
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullNames(customer.getFullNames())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .userId(userId)
                .user(UserSummaryResponse.from(customer.getUser()))
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
