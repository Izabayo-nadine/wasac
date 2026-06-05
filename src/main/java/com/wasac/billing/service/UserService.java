package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Role;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.RoleName;
import com.wasac.billing.domain.enums.UserStatus;
import com.wasac.billing.dto.request.UserRequest;
import com.wasac.billing.dto.response.UserResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.RoleRepository;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<RoleName> STAFF_ROLES = Set.of(
            RoleName.ROLE_ADMIN,
            RoleName.ROLE_OPERATOR,
            RoleName.ROLE_FINANCE
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessRuleException("Password is required when creating a user");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(resolveStaffRoles(request.getRoles()))
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = getEntityById(id);

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (!user.getPhoneNumber().equals(request.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already in use");
        }

        user.setFullNames(request.getFullNames());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveStaffRoles(request.getRoles()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = getEntityById(id);
        customerRepository.findByUserId(id).ifPresent(customer -> {
            customer.setUser(null);
            customerRepository.save(customer);
        });
        userRepository.delete(user);
    }

    private Set<Role> resolveStaffRoles(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BusinessRuleException("At least one staff role is required");
        }

        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : roleNames) {
            if (!STAFF_ROLES.contains(roleName)) {
                throw new BusinessRuleException(
                        "Only staff roles allowed: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE");
            }
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }
}
