package com.wasac.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private Long id;
    private String email;
    private String fullNames;
    private List<String> roles;
}
