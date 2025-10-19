package com.bankapp.cash.entity;

/**
 * Статус Saga для отслеживания состояния распределенной транзакции.
 */
public enum SagaStatus {
    /**
     * Операция создана, ожидается обработка в accounts-service.
     */
    PENDING,
    
    /**
     * Saga успешно завершена.
     */
    COMPLETED,

    FAILED,
    
    /**
     * Выполнена компенсирующая транзакция.
     */
    COMPENSATED
}

