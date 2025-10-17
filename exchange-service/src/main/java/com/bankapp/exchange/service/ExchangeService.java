package com.bankapp.exchange.service;

import com.bankapp.exchange.model.entity.ExchangeRate;
import com.bankapp.exchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {
    
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional(readOnly = true)
    public List<ExchangeRate> getAllRates() {
        return exchangeRateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ExchangeRate getRate(String currencyCode) {
        return exchangeRateRepository.findByCurrencyCode(currencyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + currencyCode));
    }

    @Transactional
    public void updateRates(List<ExchangeRate> newRates) {
        log.info("Updating {} exchange rates", newRates.size());
        
        for (ExchangeRate newRate : newRates) {
            ExchangeRate existingRate = exchangeRateRepository.findByCurrencyCode(newRate.getCurrencyCode())
                    .orElse(new ExchangeRate());
            
            existingRate.setCurrencyCode(newRate.getCurrencyCode());
            existingRate.setBuyRate(newRate.getBuyRate());
            existingRate.setSellRate(newRate.getSellRate());
            
            exchangeRateRepository.save(existingRate);
            log.debug("Updated rate for {}: buy={}, sell={}", 
                    newRate.getCurrencyCode(), newRate.getBuyRate(), newRate.getSellRate());
        }
    }
}


