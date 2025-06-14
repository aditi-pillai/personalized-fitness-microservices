package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse response = new UserResponse();
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPassword(user.getPassword());
        response.setKeycloakId(user.getKeycloakId());
        return response;
    }

    public UserResponse register(@Valid RegisterRequest request) {
        // Check if user exists by Keycloak ID
        if (userRepository.existsByKeycloakId(request.getKeycloakId())) {
            User existingUser = (User) userRepository.findByKeycloakId(request.getKeycloakId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UserResponse response = new UserResponse();
            response.setEmail(existingUser.getEmail());
            response.setCreatedAt(existingUser.getCreatedAt());
            response.setKeycloakId(existingUser.getKeycloakId());
            response.setUpdatedAt(existingUser.getUpdatedAt());
            response.setId(existingUser.getId());
            response.setFirstName(existingUser.getFirstName());
            response.setLastName(existingUser.getLastName());
            response.setPassword(existingUser.getPassword());
            return response;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setKeycloakId(request.getKeycloakId());
        user.setLastName(request.getLastName());

        User savedUser = userRepository.save(user);
        UserResponse response = new UserResponse();
        response.setEmail(savedUser.getEmail());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setKeycloakId(savedUser.getKeycloakId());
        response.setUpdatedAt(savedUser.getUpdatedAt());
        response.setId(savedUser.getId());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setPassword(savedUser.getPassword());

        return response;
    }

    public Boolean existsByUserId(String userId) {
        log.info("calling user validation API for checking if user exists with userId: {}", userId);
//        return userRepository.existsByKeycloakId(userId);
//        return userRepository.existsById(userId);
        return userRepository.existsByKeycloakId(userId);
    }
}
