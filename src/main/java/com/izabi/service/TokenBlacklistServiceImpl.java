package com.izabi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService{

    private final RedisTemplate<String , String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";


    @Override
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blackListed", Duration.ofHours(24));
    }

    @Override
    public void blacklistAllUserTokens(String userId) {
        String key = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), Duration.ofDays(30));
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
