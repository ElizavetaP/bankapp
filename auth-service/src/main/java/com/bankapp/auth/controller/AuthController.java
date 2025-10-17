package com.bankapp.auth.controller;

import com.bankapp.auth.dto.ChangePasswordRequest;
import com.bankapp.auth.dto.RegisterRequest;
import com.bankapp.auth.entity.User;
import com.bankapp.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok("User registered successfully: " + user.getLogin());
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{login}")
    public ResponseEntity<User> getUser(@PathVariable String login) {
        try {
            User user = authService.findByLogin(login);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{login}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable String login) {
        boolean exists = authService.userExists(login);
        return ResponseEntity.ok(exists);
    }
}

