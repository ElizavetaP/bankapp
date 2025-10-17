package com.bankapp.accounts.controller;

import com.bankapp.accounts.dto.UserDto;
import com.bankapp.accounts.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto request) {
        try {
            UserDto user = userService.createUser(request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        try {
            UserDto user = userService.getUserByLogin(login);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{login}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String login,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String birthdate) {
        try {
            UserDto user = userService.updateUser(login, name, birthdate);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

