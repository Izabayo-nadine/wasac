package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Role;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.RoleName;
import com.wasac.billing.domain.enums.UserStatus;
import com.wasac.billing.dto.request.LoginRequest;
import com.wasac.billing.dto.request.RegisterRequest;
import com.wasac.billing.dto.response.JwtResponse;
import com.wasac.billing.dto.response.UserResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.RoleRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<RoleName> STAFF_ROLES = Set.of(
            RoleName.ROLE_ADMIN,
            RoleName.ROLE_OPERATOR,
            RoleName.ROLE_FINANCE
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Public registration: ROLE_CUSTOMER, INACTIVE until OTP verified.
     * Admin registration: staff roles, immediately ACTIVE and verified.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        boolean adminCreatingStaff = isAuthenticatedAdmin();
        Set<Role> roles = adminCreatingStaff
                ? resolveAdminStaffRoles(request)
                : resolvePublicCustomerRole(request);

        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(adminCreatingStaff ? UserStatus.ACTIVE : UserStatus.INACTIVE)
                .emailVerified(adminCreatingStaff)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        if (!adminCreatingStaff) {
            otpService.generateAndSendOtp(user.getEmail());
        }

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse verifyOtp(String email, String otp) {
        return UserResponse.from(otpService.verifyOtp(email, otp));
    }

    @Transactional
    public void resendOtp(String email) {
        otpService.generateAndSendOtp(email);
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessRuleException("Please verify your email with OTP before logging in");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessRuleException("Account is inactive");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .fullNames(user.getFullNames())
                .roles(roles)
                .build();
    }

    public void logout(String token) {
        tokenBlacklistService.revokeToken(token);
        SecurityContextHolder.clearContext();
    }

    private Set<Role> resolvePublicCustomerRole(RegisterRequest request) {
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            boolean hasStaffRole = request.getRoles().stream().anyMatch(STAFF_ROLES::contains);
            if (hasStaffRole) {
                throw new BusinessRuleException(
                        "Public users cannot register staff roles. Only ROLE_CUSTOMER is allowed.");
            }
        }
        return new HashSet<>(Set.of(getRole(RoleName.ROLE_CUSTOMER)));
    }

    private Set<Role> resolveAdminStaffRoles(RegisterRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new BusinessRuleException(
                    "Admin must specify at least one staff role: ROLE_ADMIN, ROLE_OPERATOR, or ROLE_FINANCE");
        }

        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : request.getRoles()) {
            if (!STAFF_ROLES.contains(roleName)) {
                throw new BusinessRuleException(
                        "Admins can only assign staff roles: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE");
            }
            roles.add(getRole(roleName));
        }
        return roles;
    }

    boolean isAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }
}
