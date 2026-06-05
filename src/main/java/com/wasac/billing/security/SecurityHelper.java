package com.wasac.billing.security;

import com.wasac.billing.domain.entity.User;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces that ROLE_CUSTOMER users can only access their own customer-linked data.
 */
@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserRepository userRepository;

    public boolean isStaff() {
        return hasAnyRole("ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FINANCE");
    }

    public void assertCustomerOwnsData(Long customerId) {
        if (isStaff()) {
            return;
        }
        Long ownCustomerId = getAuthenticatedCustomerId();
        if (ownCustomerId == null || !ownCustomerId.equals(customerId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public Long getAuthenticatedCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || user.getCustomer() == null) {
            return null;
        }
        return user.getCustomer().getId();
    }

    private boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return Arrays.stream(roles).anyMatch(authorities::contains);
    }
}
