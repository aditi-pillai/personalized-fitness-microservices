package com.fitness.gateway.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String keycloakId;

    @NotBlank(message = "Required field")
    @Size(min=6, message = "Password must have at least 6 characters")
    private String password;
    private String firstName;
    private String lastName;
}
