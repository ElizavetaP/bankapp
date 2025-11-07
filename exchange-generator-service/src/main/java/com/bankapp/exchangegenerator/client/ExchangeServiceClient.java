package com.bankapp.exchangegenerator.client;

import com.bankapp.exchangegenerator.dto.ExchangeRateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "exchange-service")
public interface ExchangeServiceClient {
    
    @PostMapping("/api/exchange/rates")
    void updateRates(@RequestBody List<ExchangeRateDto> rates);
}


