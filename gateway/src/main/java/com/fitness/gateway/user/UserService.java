package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUserId(String userId) {
        log.info("Calling User Validation API for userId: {}", userId);
        return userServiceWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.info("User not found in database: {}", userId);
                        return Mono.just(false); // Return false instead of throwing error
                    } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        log.error("Bad request for userId validation: {}", userId);
                        return Mono.just(false); // Return false for bad requests too
                    }
                    log.error("Unexpected error during user validation: {}", e.getMessage());
                    return Mono.just(false); // Return false for any other errors
                })
                .doOnSuccess(exists -> log.info("User validation result for {}: {}", userId, exists))
                .doOnError(e -> log.error("Error validating user {}: {}", userId, e.getMessage()));
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {
        log.info("Calling User Registration API for email: {}", request.getEmail());
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        log.error("Bad Request during user registration: {}", e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Bad Request: " + e.getResponseBodyAsString()));
                    } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        log.error("Internal Server Error during user registration: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Internal Server Error: " + e.getMessage()));
                    } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        log.info("User already exists, returning existing user info");
                        // Handle user already exists case - you might want to fetch and return existing user
                        return Mono.error(new RuntimeException("User already exists: " + request.getEmail()));
                    }
                    log.error("Unexpected error during user registration: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Unexpected error: " + e.getMessage()));
                })
                .doOnSuccess(user -> log.info("Successfully registered user: {}", user.getEmail()))
                .doOnError(e -> log.error("Failed to register user {}: {}", request.getEmail(), e.getMessage()));
    }
}
