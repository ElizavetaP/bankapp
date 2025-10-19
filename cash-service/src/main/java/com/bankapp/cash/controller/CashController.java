package com.bankapp.cash.controller;

import com.bankapp.cash.dto.CashOperationRequest;
import com.bankapp.cash.entity.CashOperation;
import com.bankapp.cash.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

    private final CashService cashService;

    /**
     * Пополнение счёта (асинхронно через Saga).
     */
    @PostMapping("/deposit")
    public ResponseEntity<CashOperation> deposit(@Valid @RequestBody CashOperationRequest request) {
        try {
            CashOperation operation = cashService.deposit(request);
            log.info("Deposit Saga initiated: sagaId={}, status={}", operation.getSagaId(), operation.getStatus());
            return ResponseEntity.ok(operation);
        } catch (Exception e) {
            log.error("Deposit failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Снятие денег (асинхронно через Saga).
     */
    @PostMapping("/withdraw")
    public ResponseEntity<CashOperation> withdraw(@Valid @RequestBody CashOperationRequest request) {
        try {
            CashOperation operation = cashService.withdraw(request);
            log.info("Withdraw Saga initiated: sagaId={}, status={}", operation.getSagaId(), operation.getStatus());
            return ResponseEntity.ok(operation);
        } catch (Exception e) {
            log.error("Withdrawal failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}

