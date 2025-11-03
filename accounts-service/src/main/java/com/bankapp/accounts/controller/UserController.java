package com.bankapp.accounts.controller;

import com.bankapp.accounts.dto.PasswordChangeRequest;
import com.bankapp.accounts.dto.UserDto;
import com.bankapp.accounts.dto.UserRegistrationRequest;
import com.bankapp.accounts.exception.UserNotFoundException;
import com.bankapp.accounts.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserDto user = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to register user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить пользователя по логину.
     */
    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        try {
            UserDto user = userService.getUserByLogin(login);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить всех пользователей.
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Обновить информацию о пользователе.
     */
    @PutMapping("/{login}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String login,
            @Valid @RequestBody UserDto request) {
        try {
            UserDto user = userService.updateUser(login, request);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Сменить пароль пользователя.
     */
    @PostMapping("/{login}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String login,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(login, request);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to change password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удалить пользователя.
     */
    @DeleteMapping("/{login}")
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        try {
            userService.deleteUser(login);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

