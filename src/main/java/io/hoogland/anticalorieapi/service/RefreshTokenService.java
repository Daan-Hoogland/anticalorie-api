package io.hoogland.anticalorieapi.service;

import io.hoogland.anticalorieapi.exception.RefreshTokenException;
import io.hoogland.anticalorieapi.model.RefreshToken;
import io.hoogland.anticalorieapi.model.User;
import io.hoogland.anticalorieapi.model.request.RefreshTokenRequest;
import io.hoogland.anticalorieapi.repository.RefreshTokenRepository;
import io.hoogland.anticalorieapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${anticalorie.jwt.refreshExpirationInMs}")
    private Long refreshExpirationInMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public List<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findAllByUser(user);
    }

    public Optional<RefreshToken> findByToken(RefreshTokenRequest token) {
        return refreshTokenRepository.findByToken(token.getRefreshToken());
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationInMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(), "Refresh token was expired.");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
