package com.bankapp.accounts.dto;

import com.bankapp.accounts.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Currency currency;
    private Double value;
}

