package com.bankapp.accounts.service;

import com.bankapp.accounts.dto.PasswordChangeRequest;
import com.bankapp.accounts.dto.UserDto;
import com.bankapp.accounts.dto.UserRegistrationRequest;
import com.bankapp.accounts.entity.User;
import com.bankapp.accounts.exception.UserNotFoundException;
import com.bankapp.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Регистрация нового пользователя.
     */
    @Transactional
    public UserDto registerUser(UserRegistrationRequest request) {
        log.info("Registering user: {}", request.getLogin());

        // Проверка существования пользователя
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException("User already exists: " + request.getLogin());
        }

        // Валидация возраста (18+)
        LocalDate birthDate = LocalDate.parse(request.getBirthDate());
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        // Создание пользователя
        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setBirthDate(birthDate);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getLogin());

        return toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Обновление информации о пользователе.
     */
    @Transactional
    public UserDto updateUser(String login, UserDto request) {
        log.info("Updating user: {}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));

        // Валидация возраста (18+)
        LocalDate birthDate = LocalDate.parse(request.getBirthDate());
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setBirthDate(birthDate);
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getLogin());

        return toDto(updatedUser);
    }

    /**
     * Смена пароля.
     */
    @Transactional
    public void changePassword(String login, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));

        // Проверка старого пароля
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Установка нового пароля
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", login);
    }

    /**
     * Удаление пользователя (можно удалить только если нет счетов с балансом > 0).
     */
    @Transactional
    public void deleteUser(String login) {
        log.info("Deleting user: {}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));

        // Проверка, есть ли счета с положительным балансом
        boolean hasPositiveBalance = user.getAccounts().stream()
                .anyMatch(account -> account.getBalance().signum() > 0);

        if (hasPositiveBalance) {
            throw new IllegalArgumentException("Cannot delete user with non-zero account balance");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", login);
    }

    /**
     * Получить User entity по логину (для внутреннего использования).
     */
    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + login));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate().toString()
        );
    }
}

