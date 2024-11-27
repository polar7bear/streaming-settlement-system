package com.streaming.settlement.system.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class TokenProvider {

    private final String secretKey;
    private final String issuer;
    private final Key key;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.issuer}") String issuer) {
        this.secretKey = secretKey;
        this.issuer = issuer;
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public Authentication getAuthentication(String token) {
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(getRole(token)));
        return new UsernamePasswordAuthenticationToken(getUsername(token), null, authorities);
    }

    public boolean validationToken(String token) {
        try {
            String tokenType = commonValidation(token);
            return tokenType.equals("ACCESS_TOKEN");
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException exception) {
            return false;
        }
    }

    public String commonValidation(String token) {
        String tokenType = getTokenType(token);
        String targetIssuer = getIssuer(token);

        if (!targetIssuer.equals(issuer)) return null;

        return tokenType;
    }


    public Claims getClaims(String token) {
        log.info("[TokenProvider getClaims] Start");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info("[TokenProvider getClaims] claims: {}", claims);
            return claims;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getMemberId(String token) {
        return getClaims(token).get("memberId", Long.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getIssuer(String token) {
        return getClaims(token).getIssuer();
    }
}
