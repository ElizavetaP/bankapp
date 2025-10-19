package com.bankapp.cash.repository;

import com.bankapp.cash.entity.OutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    /**
     * Получить события для обработки.
     * Обрабатываются в порядке создания (FIFO).
     */
    Page<OutboxEvent> findAllByOrderByCreatedAtAsc(Pageable pageable);
}

