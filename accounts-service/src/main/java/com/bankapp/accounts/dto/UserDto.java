package com.bankapp.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    @NotBlank(message = "Login is required")
    private String login;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Birthdate is required")
    private String birthdate;
}

