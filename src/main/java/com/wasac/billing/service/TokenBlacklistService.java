package com.wasac.billing.service;

import com.wasac.billing.domain.entity.RevokedToken;
import com.wasac.billing.repository.RevokedTokenRepository;
import com.wasac.billing.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        String tokenHash = jwtTokenProvider.hashToken(token);
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationFromToken(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        if (!revokedTokenRepository.existsByTokenHashAndExpiresAtAfter(tokenHash, LocalDateTime.now())) {
            revokedTokenRepository.save(RevokedToken.builder()
                    .tokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String tokenHash = jwtTokenProvider.hashToken(token);
        return revokedTokenRepository.existsByTokenHashAndExpiresAtAfter(tokenHash, LocalDateTime.now());
    }
}
