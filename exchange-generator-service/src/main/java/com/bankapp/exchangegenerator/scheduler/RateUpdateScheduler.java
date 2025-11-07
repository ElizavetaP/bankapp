package com.bankapp.exchangegenerator.scheduler;

import com.bankapp.exchangegenerator.client.ExchangeServiceClient;
import com.bankapp.exchangegenerator.dto.ExchangeRateDto;
import com.bankapp.exchangegenerator.service.RateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateUpdateScheduler {

    private final RateGeneratorService rateGeneratorService;
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Обновление курсов каждую секунду
     */
    @Scheduled(fixedRate = 1000)
    public void updateRates() {
        try {
            log.info("Starting exchange rates update");
            
            List<ExchangeRateDto> rates = rateGeneratorService.generateRates();
            exchangeServiceClient.updateRates(rates);
            
            log.info("Exchange rates updated successfully: {} rates", rates.size());
        } catch (Exception e) {
            log.error("Failed to update exchange rates: {}", e.getMessage(), e);
        }
    }
}


