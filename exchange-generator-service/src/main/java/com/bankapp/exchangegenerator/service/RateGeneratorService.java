package com.bankapp.exchangegenerator.service;

import com.bankapp.exchangegenerator.dto.ExchangeRateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class RateGeneratorService {

    @Value("${exchange.generator.volatility:0.02}")
    private double volatility;

    private final Map<String, BigDecimal> currentRates = new HashMap<>();
    private final Random random = new Random();

    public RateGeneratorService() {
        // Инициализация базовых курсов
        currentRates.put("USD", new BigDecimal("94.50"));
        currentRates.put("EUR", new BigDecimal("101.50"));
        currentRates.put("CNY", new BigDecimal("12.90"));
    }

    public List<ExchangeRateDto> generateRates() {
        log.debug("Generating new exchange rates");
        
        List<ExchangeRateDto> rates = new ArrayList<>();
        
        // Рубль не генерируем - он уже в БД (init-db.sql) и не меняется (1:1)

        for (Map.Entry<String, BigDecimal> entry : currentRates.entrySet()) {
            String currency = entry.getKey();
            BigDecimal currentRate = entry.getValue();
            
            // Генерируем изменение курса (± volatility)
            double change = (random.nextDouble() * 2 - 1) * volatility;
            BigDecimal newRate = currentRate.multiply(
                BigDecimal.valueOf(1 + change)
            ).setScale(4, RoundingMode.HALF_UP);
            
            // Обновляем текущий курс
            currentRates.put(currency, newRate);
            
            // Курс покупки немного выше курса продажи (спред ~1%)
            BigDecimal buyRate = newRate.multiply(new BigDecimal("0.995"))
                .setScale(4, RoundingMode.HALF_UP);
            BigDecimal sellRate = newRate.multiply(new BigDecimal("1.005"))
                .setScale(4, RoundingMode.HALF_UP);
            
            rates.add(ExchangeRateDto.builder()
                    .currencyCode(currency)
                    .buyRate(buyRate)
                    .sellRate(sellRate)
                    .build());
            
            log.debug("Generated rate for {}: buy={}, sell={}", currency, buyRate, sellRate);
        }
        
        return rates;
    }
}

