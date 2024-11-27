package com.streaming.settlement.system.memberservice.service;

import com.streaming.settlement.system.memberservice.config.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;
    private final TokenProvider tokenProvider;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";

    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        long expiration = tokenProvider.getTokenExpire(refreshToken).getTime() - new Date().getTime();

        redisTemplate.opsForValue()
                .set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
    }

    public void addAccessTokenToBlackList(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        long expiration = tokenProvider.getTokenExpire(accessToken).getTime() - new Date().getTime();

        redisTemplate.opsForValue()
                .set(key, "sign-out", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        String foundData = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(foundData);
    }

    public void signOut(String email, String accessToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);

        addAccessTokenToBlackList(accessToken);
    }
}
