package com.bankapp.accounts.repository;

import com.bankapp.accounts.entity.Account;
import com.bankapp.accounts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUser(User user);
    
    Optional<Account> findByUserAndCurrency(User user, String currency);
    
    boolean existsByUserAndCurrency(User user, String currency);

}

