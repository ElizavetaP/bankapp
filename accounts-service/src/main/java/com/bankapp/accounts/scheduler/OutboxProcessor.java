package com.bankapp.accounts.scheduler;

import com.bankapp.accounts.entity.OutboxEvent;
import com.bankapp.accounts.entity.SagaEventType;
import com.bankapp.accounts.repository.OutboxEventRepository;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик Transactional Outbox Pattern для accounts-service.
 * 
 * Периодически проверяет таблицу outbox_events и отправляет Saga события в NATS.
 * Маршрутизирует события по топикам в зависимости от eventType.
 */
@Service
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final String natsConnectionUrl;
    private final String sagaBalanceUpdatedTopic;
    private final String sagaBalanceUpdateFailedTopic;
    private final int limit;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                          @Value("${nats.url:nats://localhost:4222}") String natsConnectionUrl,
                          @Value("${application.saga.topics.balance-updated:saga.balance.updated}") String sagaBalanceUpdatedTopic,
                          @Value("${application.saga.topics.balance-update-failed:saga.balance.update.failed}") String sagaBalanceUpdateFailedTopic,
                          @Value("${application.outbox.limit:10}") int limit) {
        this.outboxEventRepository = outboxEventRepository;
        this.natsConnectionUrl = natsConnectionUrl;
        this.sagaBalanceUpdatedTopic = sagaBalanceUpdatedTopic;
        this.sagaBalanceUpdateFailedTopic = sagaBalanceUpdateFailedTopic;
        this.limit = limit;
    }

    /**
     * Обрабатываем таблицу Outbox каждую секунду.
     */
    @Scheduled(fixedDelayString = "PT1s")
    public void process() throws IOException, InterruptedException {
        // Получаем события для обработки
        Page<OutboxEvent> outboxEntries = outboxEventRepository
                .findAllByOrderByCreatedAtAsc(Pageable.ofSize(limit));

        if (outboxEntries.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", outboxEntries.getNumberOfElements());

        // Список успешно обработанных событий
        List<Long> processedIds = new ArrayList<>();

        // Подключаемся к NATS только на время отправки сообщений
        try (Connection natsConnection = Nats.connect(natsConnectionUrl)) {
            
            for (OutboxEvent event : outboxEntries) {
                try {
                    // Определить топик по типу события
                    String topic = getTopicForEventType(event.getEventType());

                    byte[] eventData = event.getPayload().getBytes();
                    natsConnection.publish(topic, eventData);
                    
                    // Успешно отправили - добавляем в список для удаления
                    processedIds.add(event.getId());
                    
                    log.debug("Published event to NATS: id={}, type={}, topic={}", 
                            event.getId(), event.getEventType(), topic);
                    
                } catch (Exception e) {
                    // Логируем ошибку, но продолжаем обработку следующих событий
                    log.error("Failed to publish event to NATS: id={}, type={}", 
                            event.getId(), event.getEventType(), e);
                    // Событие НЕ добавляется в processedIds - останется в Outbox для retry
                }
            }
            
        } catch (IOException e) {
            // NATS полностью недоступен - не можем создать connection
            log.error("NATS connection failed: {}", e.getMessage());
        }
        
        // Удаляем успешно обработанные события (после закрытия NATS connection)
        if (!processedIds.isEmpty()) {
            try {
                outboxEventRepository.deleteAllById(processedIds);
                log.info("Deleted {} processed outbox events", processedIds.size());
            } catch (Exception e) {
                // События отправлены, но не удалены - будут переотправлены (безопасно благодаря идемпотентности)
                // Однако это указывает на проблему с БД, которую нужно расследовать
                log.error("Failed to delete processed events from Outbox (database error). " +
                         "Events will be redelivered (safe due to idempotency, but indicates DB problem). " +
                         "Processed IDs: {}", processedIds, e);
            }
        } else {
            log.warn("No events were processed successfully");
        }
    }
    
    /**
     * Определить топик NATS для типа события.
     */
    private String getTopicForEventType(String eventType) {
        // Преобразуем строку в enum для type-safe обработки
        try {
            SagaEventType sagaEventType = SagaEventType.valueOf(eventType);
            return switch (sagaEventType) {
                case SAGA_BALANCE_UPDATED -> sagaBalanceUpdatedTopic;
                case SAGA_BALANCE_UPDATE_FAILED -> sagaBalanceUpdateFailedTopic;
                case SAGA_BALANCE_UPDATE_REQUESTED -> {
                    // Это событие приходит В accounts-service (не отправляется)
                    log.warn("Unexpected outgoing event type: {}", eventType);
                    yield sagaBalanceUpdatedTopic;
                }
            };
        } catch (IllegalArgumentException e) {
            log.error("Unknown event type: {}, using default topic", eventType);
            return sagaBalanceUpdatedTopic;
        }
    }
}


