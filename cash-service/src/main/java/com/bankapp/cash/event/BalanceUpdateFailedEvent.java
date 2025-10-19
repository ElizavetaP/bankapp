package com.bankapp.cash.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие: Ошибка при обновлении баланса.
 * Отправляется из accounts-service в cash-service через NATS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateFailedEvent {
    
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
     * Сообщение об ошибке.
     */
    private String errorMessage;
    
    /**
     * Код ошибки (например, "INSUFFICIENT_FUNDS").
     */
    private String errorCode;
}
