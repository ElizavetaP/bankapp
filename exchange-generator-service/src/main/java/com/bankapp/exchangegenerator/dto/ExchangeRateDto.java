package com.bankapp.exchangegenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDto {
    private Long id;
    private String currencyCode;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDateTime updatedAt;
}

