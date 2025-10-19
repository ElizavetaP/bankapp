package com.bankapp.cash.service;

import com.bankapp.cash.dto.CashOperationRequest;
import com.bankapp.cash.entity.CashOperation;
import com.bankapp.cash.entity.OperationType;
import com.bankapp.cash.entity.OutboxEvent;
import com.bankapp.cash.entity.SagaEventType;
import com.bankapp.cash.entity.SagaStatus;
import com.bankapp.cash.event.BalanceUpdateRequestedEvent;
import com.bankapp.cash.repository.CashOperationRepository;
import com.bankapp.cash.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сервис кассовых операций.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {

    private final CashOperationRepository cashOperationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Пополнение счёта (Choreography Saga Pattern с Transactional Outbox).
     */
    @Transactional
    public CashOperation deposit(CashOperationRequest request) {
        return processCashOperation(request, OperationType.DEPOSIT);
    }

    /**
     * Снятие денег со счёта (Choreography Saga Pattern с Transactional Outbox).
     */
    @Transactional
    public CashOperation withdraw(CashOperationRequest request) {
        return processCashOperation(request, OperationType.WITHDRAW);
    }

    /**
     * Общая логика обработки кассовых операций (Choreography Saga Pattern с Transactional Outbox).
     * 
     * 1. Сохраняет операцию в БД (status=PENDING)
     * 2. Сохраняет Saga событие в Outbox (в той же транзакции)
     * 3. OutboxProcessor отправит событие в NATS асинхронно с retry
     * 
     * @param request данные операции
     * @param operationType тип операции (DEPOSIT или WITHDRAW)
     * @return созданная операция со статусом PENDING
     */
    private CashOperation processCashOperation(CashOperationRequest request, OperationType operationType) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting {} Saga: sagaId={}, user={}, amount={}", 
                operationType, sagaId, request.getLogin(), request.getValue());

        // 1. Создать операцию со статусом PENDING
        CashOperation operation = new CashOperation();
        operation.setUserLogin(request.getLogin());
        operation.setCurrency(request.getCurrency());
        operation.setAmount(request.getValue());
        operation.setOperationType(operationType);
        operation.setSagaId(sagaId);
        operation.setStatus(SagaStatus.PENDING);
        
        CashOperation savedOperation = cashOperationRepository.save(operation);
        log.debug("Cash operation created: id={}, sagaId={}", savedOperation.getId(), sagaId);

        // 2. Сохранить Saga событие в Outbox (в той же транзакции)
        // Для WITHDRAW сумма отрицательная, для DEPOSIT - положительная
        BigDecimal eventAmount = operationType == OperationType.WITHDRAW 
                ? request.getValue().negate() 
                : request.getValue();
        
        BalanceUpdateRequestedEvent event = BalanceUpdateRequestedEvent.builder()
                .sagaId(sagaId)
                .operationId(savedOperation.getId())
                .login(request.getLogin())
                .currency(request.getCurrency())
                .amount(eventAmount)
                .operationType(operationType.name())
                .build();
        
        saveSagaEventToOutbox(SagaEventType.SAGA_BALANCE_UPDATE_REQUESTED, event);
        log.info("Saga event saved to Outbox: sagaId={}", sagaId);

        return savedOperation;
    }

    /**
     * Сохранить Saga событие в Outbox для последующей отправки в NATS.
     */
    private void saveSagaEventToOutbox(SagaEventType eventType, Object eventPayload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(eventType.name())
                    .payload(payloadJson)
                    .build();
            
            outboxEventRepository.save(outboxEvent);
            log.debug("Saga event saved to Outbox: type={}", eventType);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Saga event: {}", e.getMessage());
            throw new RuntimeException("Failed to save Saga event to Outbox", e);
        }
    }
}

