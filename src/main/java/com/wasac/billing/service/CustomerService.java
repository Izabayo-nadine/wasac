package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Customer;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.CustomerStatus;
import com.wasac.billing.dto.request.CustomerRequest;
import com.wasac.billing.dto.response.CustomerResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("Customer with National ID already exists: " + request.getNationalId());
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email already exists: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .fullNames(request.getFullNames())
                .nationalId(request.getNationalId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
                .build();

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
            customer.setUser(user);
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        securityHelper.assertCustomerOwnsData(id);
        return CustomerResponse.from(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public Customer getEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getEntityById(id);

        if (!customer.getNationalId().equals(request.getNationalId())
                && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("National ID already in use");
        }

        customer.setFullNames(request.getFullNames());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }

    /** Validates customer is active before billing */
    public void validateActiveForBilling(Customer customer) {
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive bills");
        }
    }
}
