package com.streaming.settlement.system.memberservice.infrastructure.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class TokenProvider implements InitializingBean {

    private final String secret;
    private final long tokenValidityTime;
    private Key key;
    private final static String ACCESS_TOKEN = "ACCESS_TOKEN";
    private final static String REFRESH_TOKEN = "REFRESH_TOKEN";

    private final String issuer;

    public TokenProvider(@Value("${jwt.secret}") String secret,
                         @Value("${jwt.token-validity-time}") long tokenValidityTime,
                         @Value("${jwt.issuer}") String issuer) {
        this.secret = secret;
        this.tokenValidityTime = tokenValidityTime;
        this.issuer = issuer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64URL.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Authentication authentication) {
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .toString();
        long now = new Date().getTime();
        Date expiration = new Date(now + this.tokenValidityTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuer(issuer)
                .claim("role", role)
                .claim("type", ACCESS_TOKEN)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(expiration)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        long validityTime = 60480000;
        Date expiration = new Date(new Date().getTime() + validityTime);
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .get()
                .toString();

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuer(issuer)
                .claim("role", role)
                .claim("type", REFRESH_TOKEN)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(expiration)
                .compact();
    }

    // 토큰으로 인증 객체 가져오기
    public Authentication getAuthentication(String token) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(getRole(token)));
        return new UsernamePasswordAuthenticationToken(getUsername(token), null, authorities);
    }

    // 토큰 유효성 검증
    public boolean validationAccessToken(String token) {
        try {
            String tokenType = commonValidation(token);
            if (tokenType == null) return false;

            return tokenType.equals(ACCESS_TOKEN) || tokenType.equals(REFRESH_TOKEN);

        } catch (SecurityException | MalformedJwtException e) {
            throw new MalformedJwtException(e.getMessage());
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException(e.getMessage());
        }
    }

    private String commonValidation(String token) {
        String tokenType = getTokenType(token);
        String targetIssuer = getIssuer(token);
        String targetSubject = getUsername(token);
        Authentication authentication = getAuthentication(token);

        if (!targetIssuer.equals(issuer)) return null;
        if (!targetSubject.equals(authentication.getName())) return null;
        return tokenType;
    }

    // 토큰 유효시간 가져오기
    public Date getAccessTokenExpire(String token) {
        Claims claims = (Claims) Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parse(token)
                .getBody();
        return claims.getExpiration();
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public String getIssuer(String token) {
        return getClaims(token).getIssuer();
    }

}
