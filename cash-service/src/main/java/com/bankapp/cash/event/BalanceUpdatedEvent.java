package com.bankapp.cash.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Событие: Баланс успешно обновлен.
 * Отправляется из accounts-service в cash-service через NATS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdatedEvent {
    
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
     * Новый баланс после операции.
     */
    private BigDecimal newBalance;
}
