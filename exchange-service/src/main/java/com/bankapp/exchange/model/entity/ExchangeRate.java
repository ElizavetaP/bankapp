package com.bankapp.exchange.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates", schema = "exchange")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "currency_code", nullable = false, unique = true, length = 3)
    private String currencyCode;
    
    @Column(name = "buy_rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal buyRate;
    
    @Column(name = "sell_rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal sellRate;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


