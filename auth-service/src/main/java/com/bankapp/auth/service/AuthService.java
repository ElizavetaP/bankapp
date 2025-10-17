package com.bankapp.auth.service;

import com.bankapp.auth.dto.ChangePasswordRequest;
import com.bankapp.auth.dto.RegisterRequest;
import com.bankapp.auth.entity.User;
import com.bankapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getLogin());

        // Check if user already exists
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("User with login '" + request.getLogin() + "' already exists");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setBirthdate(LocalDate.parse(request.getBirthdate()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getLogin());

        return savedUser;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for user: {}", request.getLogin());

        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getLogin()));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", request.getLogin());
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));
    }

    public boolean userExists(String login) {
        return userRepository.existsByLogin(login);
    }
}

