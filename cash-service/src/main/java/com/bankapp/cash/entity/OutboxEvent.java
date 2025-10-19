package com.bankapp.cash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity для реализации Transactional Outbox Pattern.
 * Обеспечивает гарантированную доставку уведомлений в notifications-service.
 */
@Entity
@Table(name = "outbox_events", schema = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;  // JSON строка
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

