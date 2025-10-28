package com.bankapp.accounts.entity;

/**
 * Типы событий для Choreography Saga Pattern.
 * Используются как идентификаторы событий в outbox_events.event_type.
 */
public enum SagaEventType {
    
    /**
     * Запрос на обновление баланса (cash-service → accounts-service).
     */
    SAGA_BALANCE_UPDATE_REQUESTED,
    
    /**
     * Баланс успешно обновлен (accounts-service → cash-service).
     */
    SAGA_BALANCE_UPDATED,
    
    /**
     * Ошибка при обновлении баланса (accounts-service → cash-service).
     */
    SAGA_BALANCE_UPDATE_FAILED
}


