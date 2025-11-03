package com.bankapp.accounts.controller;

import com.bankapp.accounts.dto.AccountDto;
import com.bankapp.accounts.model.Currency;
import com.bankapp.accounts.service.AccountService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{login}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(@PathVariable String login) {
        try {
            List<AccountDto> accounts = accountService.getUserAccounts(login);
            return ResponseEntity.ok(accounts);
        } catch (IllegalArgumentException e) {
            log.error("Failed to get accounts: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @RequestParam String login,
            @RequestParam Currency currency) {
        try {
            AccountDto account = accountService.createAccount(login, currency);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create account: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{login}/{currency}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @PathVariable String login,
            @PathVariable Currency currency) {
        try {
            BigDecimal balance = accountService.getAccountBalance(login, currency);
            return ResponseEntity.ok(balance);
        } catch (IllegalArgumentException e) {
            log.error("Failed to get balance: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{login}/{currency}/update-balance")
    public ResponseEntity<Void> updateBalance(
            @PathVariable String login,
            @PathVariable Currency currency,
            @RequestParam BigDecimal amount) {
        try {
            accountService.updateBalance(login, currency, amount);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to update balance: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{login}/{currency}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable String login,
            @PathVariable Currency currency) {
        try {
            accountService.deleteAccount(login, currency);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete account: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

