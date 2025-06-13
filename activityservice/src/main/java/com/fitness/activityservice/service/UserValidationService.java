package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUserId(String userId){
        log.info("calling user validation api");
        try{
            return Boolean.TRUE.equals(userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        }  catch (WebClientResponseException e){
            System.out.println("Response error: " + e.getResponseBodyAsString());
            System.out.println("Status code: " + e.getStatusCode());
            throw e; // or handle explicitly
        } catch (WebClientException e){
            System.out.println("WebClient exception: " + e.getMessage());
            throw e;
        }

    }

}
