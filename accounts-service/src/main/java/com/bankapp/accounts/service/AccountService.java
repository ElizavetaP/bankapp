package com.bankapp.accounts.service;

import com.bankapp.accounts.dto.AccountDto;
import com.bankapp.accounts.entity.Account;
import com.bankapp.accounts.entity.User;
import com.bankapp.accounts.model.Currency;
import com.bankapp.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(String login) {
        log.debug("Getting accounts for user: {}", login);
        
        User user = userService.findUserByLogin(login);
        List<Account> existingAccounts = accountRepository.findByUser(user);

        List<AccountDto> result = new ArrayList<>();

        for (Currency currency : Currency.values()) {
            Optional<Account> account = existingAccounts.stream()
                    .filter(a -> a.getCurrency().equals(currency.name()))
                    .findFirst();

            account.ifPresent(acc -> result.add(new AccountDto(
                    currency,
                    acc.getBalance().doubleValue()
            )));
        }

        return result;
    }

    @Transactional
    public AccountDto createAccount(String login, Currency currency) {
        log.info("Creating account for user: {}, currency: {}", login, currency);

        User user = userService.findUserByLogin(login);
        
        String currencyCode = currency.name();

        // Проверка существования счета
        if (accountRepository.existsByUserAndCurrency(user, currencyCode)) {
            throw new IllegalArgumentException("Account already exists for currency: " + currencyCode);
        }

        Account account = new Account();
        account.setUser(user);
        account.setCurrency(currencyCode);
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getId());

        return new AccountDto(currency, 0.0);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(String login, Currency currency) {
        User user = userService.findUserByLogin(login);
        Account account = accountRepository.findByUserAndCurrency(user, currency.name())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return account.getBalance();
    }

    @Transactional
    public void updateBalance(String login, Currency currency, BigDecimal amount) {
        log.info("Updating balance for user: {}, currency: {}, amount: {}", login, currency, amount);

        User user = userService.findUserByLogin(login);
        String currencyCode = currency.name();
        Account account = accountRepository.findByUserAndCurrency(user, currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(amount);
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Balance updated successfully. New balance: {}", newBalance);
    }
}

