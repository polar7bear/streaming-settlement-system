package com.streaming.settlement.system.apigateway.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String BLACKLIST_PREFIX = "BL:";
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final TokenProvider tokenProvider;


    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            boolean skipAuth = config.isSkipAuth(path);
            if (skipAuth) {
                return chain.filter(exchange);
            }

            String token = resolveToken(request);
            if (!StringUtils.hasText(token)) {
                return onError(exchange, "존재하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }

            return checkBlacklist(token)
                    .flatMap(isBlackListed -> {
                        if (isBlackListed) {
                            return onError(exchange, "블랙리스트에 등록된 토큰입니다.", HttpStatus.UNAUTHORIZED);
                        }

                        if (!tokenProvider.validationToken(token)) {
                            return onError(exchange, "유효하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED);
                        }

                        Long memberId = tokenProvider.getMemberId(token);
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-Member-Id", String.valueOf(memberId))
                                .build();

                        return chain.filter(exchange.mutate()
                                .request(mutatedRequest)
                                .build());
                    });

        });
    }

    @Getter
    @Setter
    public static class Config {
        private List<String> excludePaths = new ArrayList<>();
        private boolean enableDebug = false;

        public boolean isSkipAuth(String path) {
            return excludePaths.stream()
                    .anyMatch(pattern -> {
                        AntPathMatcher matcher = new AntPathMatcher();
                        return matcher.match(pattern, path);
                    });
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Boolean> checkBlacklist(String token) {
        return reactiveRedisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    public String resolveToken(ServerHttpRequest request) {
        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isEmpty()) {
            String bearerToken = authHeader.getFirst();
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(BEARER_PREFIX.length());
            }
        }
        return null;
    }
}
