package com.bankapp.cash.listener;

import com.bankapp.cash.entity.CashOperation;
import com.bankapp.cash.entity.SagaStatus;
import com.bankapp.cash.event.BalanceUpdateFailedEvent;
import com.bankapp.cash.event.BalanceUpdatedEvent;
import com.bankapp.cash.repository.CashOperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Слушатель ответов Saga от accounts-service через NATS.
 * Обновляет статус CashOperation на основе результата обновления баланса.
 */
@Service
@Slf4j
public class SagaResponseListener {

    private final Connection natsConnection;
    private final ObjectMapper objectMapper;
    private final CashOperationRepository cashOperationRepository;

    public SagaResponseListener(
            @Value("${nats.url:nats://localhost:4222}") String natsConnectionUrl,
            @Value("${application.saga.topics.balance-updated:saga.balance.updated}") String successTopic,
            @Value("${application.saga.topics.balance-update-failed:saga.balance.update.failed}") String failedTopic,
            ObjectMapper objectMapper,
            CashOperationRepository cashOperationRepository) throws IOException, InterruptedException {
        
        this.objectMapper = objectMapper;
        this.cashOperationRepository = cashOperationRepository;
        
        // Подключаемся к NATS и создаем Dispatcher
        this.natsConnection = Nats.connect(natsConnectionUrl);
        Dispatcher dispatcher = natsConnection.createDispatcher();
        
        // Подписываемся на оба топика (success и failed)
        dispatcher.subscribe(successTopic, this::handleSuccessMessage);
        dispatcher.subscribe(failedTopic, this::handleFailedMessage);
        
        log.info("NATS Saga Response Listener started. Subscribed to: {} and {}", successTopic, failedTopic);
    }

    /**
     * Обработка успешного ответа (BalanceUpdatedEvent).
     */
    private void handleSuccessMessage(Message message) {
        String messageData = new String(message.getData());
        log.info("Received success Saga response: {}", messageData);

        try {
            BalanceUpdatedEvent event = objectMapper.readValue(messageData, BalanceUpdatedEvent.class);
            updateOperationStatus(event.getSagaId(), SagaStatus.COMPLETED, null);
        } catch (Exception e) {
            log.error("Failed to process success Saga response: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка ответа об ошибке (BalanceUpdateFailedEvent).
     */
    private void handleFailedMessage(Message message) {
        String messageData = new String(message.getData());
        log.info("Received failed Saga response: {}", messageData);

        try {
            BalanceUpdateFailedEvent event = objectMapper.readValue(messageData, BalanceUpdateFailedEvent.class);
            updateOperationStatus(event.getSagaId(), SagaStatus.FAILED, event.getErrorMessage());
        } catch (Exception e) {
            log.error("Failed to process failed Saga response: {}", e.getMessage(), e);
        }
    }

    /**
     * Обновить статус CashOperation.
     */
    @Transactional
    public void updateOperationStatus(String sagaId, SagaStatus status, String errorMessage) {
        log.info("Updating operation status: sagaId={}, status={}", sagaId, status);

        CashOperation operation = cashOperationRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found for sagaId: " + sagaId));

        operation.setStatus(status);
        operation.setErrorMessage(errorMessage);
        
        cashOperationRepository.save(operation);
        log.info("Operation status updated: sagaId={}, status={}", sagaId, status);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        natsConnection.close();
    }
}


