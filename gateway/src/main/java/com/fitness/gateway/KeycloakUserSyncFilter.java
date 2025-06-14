package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        String userId = exchange.getRequest().getHeaders().getFirst("X-user-ID");
        RegisterRequest registerRequest = getUserDetails(token);

        if (userId == null && registerRequest != null) {
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null && token != null && registerRequest != null) {
            String finalUserId = userId;
            return userService.validateUserId(userId)
                    .flatMap(exists -> {
                        if (!exists) {
                            return userService.registerUser(registerRequest)
                                    .then(updateRequestAndChain(exchange, chain, finalUserId));
                        } else {
                            log.info("User already exists. Skipping sync.");
                            return updateRequestAndChain(exchange, chain, finalUserId);
                        }
                    });
        }
        return chain.filter(exchange);
    }

    private Mono<Void> updateRequestAndChain(ServerWebExchange exchange, WebFilterChain chain, String userId) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-user-ID", userId)
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("Invalid/missing Authorization header");
                return null;
            }
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            return RegisterRequest.builder()
                    .email(claims.getStringClaim("email"))
                    .keycloakId(claims.getStringClaim("sub"))
                    .password("dummy@123123")
                    .firstName(claims.getStringClaim("given_name"))
                    .lastName(claims.getStringClaim("family_name"))
                    .build();
        } catch (Exception e) {
            log.error("JWT Parsing failed: {}", e.getMessage());
            return null;
        }
    }
}
