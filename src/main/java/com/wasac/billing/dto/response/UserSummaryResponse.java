package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserSummaryResponse {

    Long id;
    String fullNames;
    String email;
    String phoneNumber;
    UserStatus status;
    boolean emailVerified;
    List<String> roles;

    public static UserSummaryResponse from(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullNames(user.getFullNames())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();
    }
}
