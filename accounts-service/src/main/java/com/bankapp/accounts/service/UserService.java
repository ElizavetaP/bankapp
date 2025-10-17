package com.bankapp.accounts.service;

import com.bankapp.accounts.dto.UserDto;
import com.bankapp.accounts.entity.User;
import com.bankapp.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(UserDto request) {
        log.info("Creating user: {}", request.getLogin());

        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("User already exists: " + request.getLogin());
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthdate(LocalDate.parse(request.getBirthdate()));

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getLogin());

        return toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto updateUser(String login, String name, String birthdate) {
        log.info("Updating user: {}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));

        user.setName(name);
        user.setBirthdate(LocalDate.parse(birthdate));
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getLogin());

        return toDto(updatedUser);
    }

    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + login));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getLogin(),
                user.getName(),
                user.getBirthdate().toString()
        );
    }
}

