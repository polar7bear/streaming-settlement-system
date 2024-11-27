package com.streaming.settlement.system.apigateway.config;

import com.streaming.settlement.system.apigateway.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApiGatewayConfig {

    private final RouteLocatorBuilder builder;
    private final JwtFilter jwtFilter;

    @Bean
    public RouteLocator routeLocator() {
        JwtFilter.Config memberConfig = new JwtFilter.Config();
        memberConfig.setExcludePaths(List.of(
                "/members/auth/sign-up",
                "/members/auth/sign-in",
                "/oauth/**"
        ));

        JwtFilter.Config settlementConfig = new JwtFilter.Config();
        settlementConfig.setExcludePaths(List.of(
                "/statistics/**"
        ));


        return builder.routes()
                .route(p -> p.path("/members/auth/**")
                        .filters(f -> f.filter(jwtFilter.apply(memberConfig)))
                        .uri("lb://member-service"))

                .route(p -> p.path("/streams/**")
                        .uri("lb://streaming-ad-service"))

                .route(p -> p.path("/statistics/**", "/settlements/**")
                        .filters(f -> f.filter(jwtFilter.apply(settlementConfig)))
                        .uri("lb://settlement-service"))

                .build();
    }
}
