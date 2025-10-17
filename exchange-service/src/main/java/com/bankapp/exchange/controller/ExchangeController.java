package com.bankapp.exchange.controller;

import com.bankapp.exchange.model.entity.ExchangeRate;
import com.bankapp.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {
    
    private final ExchangeService exchangeService;

    //Получить все курсы
    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRate>> getAllRates() {
        List<ExchangeRate> rates = exchangeService.getAllRates();
        return ResponseEntity.ok(rates);
    }
    
    //Получить курс конкретной валюты
    @GetMapping("/rates/{currency}")
    public ResponseEntity<ExchangeRate> getRate(@PathVariable String currency) {
        ExchangeRate rate = exchangeService.getRate(currency);
        return ResponseEntity.ok(rate);
    }
    
    //Обновить курсы (для exchange-generator-service)
    @PostMapping("/rates")
    public ResponseEntity<Void> updateRates(@RequestBody List<ExchangeRate> newRates) {
        exchangeService.updateRates(newRates);
        return ResponseEntity.ok().build();
    }
}


