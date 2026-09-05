package com.izabi.service;

import org.springframework.stereotype.Service;

@Service
public interface TokenBlacklistService {
    void blacklistToken(String token);
    void blacklistAllUserTokens(String userId);
    boolean isTokenBlacklisted(String token);
}
