package com.bankapp.accounts.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Событие: Запрос на обновление баланса.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateRequestedEvent {
    
    /**
     * Уникальный идентификатор Saga транзакции.
     */
    private String sagaId;
    
    /**
     * ID операции в cash_operations.
     */
    private Long operationId;
    
    /**
     * Логин пользователя.
     */
    private String login;
    
    /**
     * Валюта.
     */
    private String currency;
    
    /**
     * Сумма (положительная для deposit, отрицательная для withdraw).
     */
    private BigDecimal amount;
    
    /**
     * Тип операции (DEPOSIT или WITHDRAW).
     */
    private String operationType;
}


