package com.bankapp.accounts.listener;

import com.bankapp.accounts.entity.OutboxEvent;
import com.bankapp.accounts.entity.SagaEventType;
import com.bankapp.accounts.event.BalanceUpdateFailedEvent;
import com.bankapp.accounts.event.BalanceUpdateRequestedEvent;
import com.bankapp.accounts.event.BalanceUpdatedEvent;
import com.bankapp.accounts.exception.AccountNotFoundException;
import com.bankapp.accounts.exception.InsufficientFundsException;
import com.bankapp.accounts.exception.UserNotFoundException;
import com.bankapp.accounts.repository.OutboxEventRepository;
import com.bankapp.accounts.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.math.BigDecimal;

/**
 * Слушатель Saga событий из cash-service через NATS.
 * Обрабатывает запросы на обновление баланса и отправляет ответы через Outbox.
 */
@Service
@Slf4j
public class SagaEventListener {

    private final Connection natsConnection;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final OutboxEventRepository outboxEventRepository;

    public SagaEventListener(
            @Value("${nats.url:nats://localhost:4222}") String natsConnectionUrl,
            @Value("${application.saga.topics.balance-update-requested:saga.balance.update.requested}") String requestTopic,
            ObjectMapper objectMapper,
            AccountService accountService,
            OutboxEventRepository outboxEventRepository) throws IOException, InterruptedException {

        this.objectMapper = objectMapper;
        this.accountService = accountService;
        this.outboxEventRepository = outboxEventRepository;

        // Подключаемся к NATS и создаем Dispatcher
        this.natsConnection = Nats.connect(natsConnectionUrl);
        Dispatcher dispatcher = natsConnection.createDispatcher(this::handleMessage);
        dispatcher.subscribe(requestTopic);

        log.info("NATS Saga Event Listener started. Subscribed to: {}", requestTopic);
    }

    /**
     * Обработка входящего Saga события.
     */
    private void handleMessage(Message message) {
        String messageData = new String(message.getData());
        log.info("Received Saga event: {}", messageData);

        try {
            BalanceUpdateRequestedEvent event = objectMapper.readValue(messageData, BalanceUpdateRequestedEvent.class);
            processBalanceUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process Saga event: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработка запроса на обновление баланса.
     * Логика выполняется в транзакции вместе с сохранением ответа в Outbox.
     */
    @Transactional
    public void processBalanceUpdate(BalanceUpdateRequestedEvent event) {
        log.info("Processing balance update: sagaId={}, login={}, amount={}",
                event.getSagaId(), event.getLogin(), event.getAmount());

        try {
            // Выполняем обновление баланса
            BigDecimal newBalance = accountService.updateBalanceAndReturn(
                    event.getLogin(),
                    event.getCurrency(),
                    event.getAmount()
            );

            // Успех - сохраняем событие успеха в Outbox
            BalanceUpdatedEvent successEvent = BalanceUpdatedEvent.builder()
                    .sagaId(event.getSagaId())
                    .operationId(event.getOperationId())
                    .login(event.getLogin())
                    .currency(event.getCurrency())
                    .newBalance(newBalance)
                    .build();

            saveEventToOutbox(SagaEventType.SAGA_BALANCE_UPDATED, successEvent);
            log.info("Balance updated successfully: sagaId={}, newBalance={}", event.getSagaId(), newBalance);

        } catch (Exception e) {
            // Ошибка - сохраняем событие ошибки в Outbox
            log.error("Balance update failed: sagaId={}, error={}", event.getSagaId(), e.getMessage());

            BalanceUpdateFailedEvent failedEvent = BalanceUpdateFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .operationId(event.getOperationId())
                    .login(event.getLogin())
                    .errorMessage(e.getMessage())
                    .errorCode(getErrorType(e))
                    .build();

            saveEventToOutbox(SagaEventType.SAGA_BALANCE_UPDATE_FAILED, failedEvent);
        }
    }

    /**
     * Сохранить Saga событие в Outbox для последующей отправки в NATS.
     */
    private void saveEventToOutbox(SagaEventType eventType, Object eventPayload) {
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

    /**
     * Определить тип ошибки по типу исключения (type-safe).
     */
    private String getErrorType(Exception e) {
        if (e instanceof InsufficientFundsException) {
            return "INSUFFICIENT_FUNDS";
        } else if (e instanceof AccountNotFoundException) {
            return "ACCOUNT_NOT_FOUND";
        } else if (e instanceof UserNotFoundException) {
            return "USER_NOT_FOUND";
        }
        return "UNKNOWN_ERROR";
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        natsConnection.close();
    }
}


