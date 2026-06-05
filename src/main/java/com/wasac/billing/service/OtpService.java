package com.wasac.billing.service;

import com.wasac.billing.domain.entity.EmailOtp;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.UserStatus;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.EmailOtpRepository;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Transactional
    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessRuleException("Email is already verified");
        }

        otpRepository.invalidateAllForEmail(email);

        String otp = generateOtpCode();
        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();

        otpRepository.save(emailOtp);

        // Send email after DB commit so registration is not rolled back if SMTP is slow
        String otpToSend = otp;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendOtpEmail(email, otpToSend);
                }
            });
        } else {
            emailService.sendOtpEmail(email, otp);
        }
    }

    @Transactional
    public User verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessRuleException("Email is already verified");
        }

        EmailOtp emailOtp = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessRuleException("No active OTP found. Please request a new one."));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("OTP has expired. Please request a new one.");
        }

        if (!emailOtp.getOtpCode().equals(otp)) {
            throw new BusinessRuleException("Invalid OTP code");
        }

        emailOtp.setUsed(true);
        otpRepository.save(emailOtp);

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, otpLength);
        int code = secureRandom.nextInt(bound / 10, bound);
        return String.valueOf(code);
    }
}
