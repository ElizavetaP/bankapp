package com.bankapp.accounts.model;

public enum Currency {
    RUB("Рубль"),
    USD("Доллар США"),
    EUR("Евро"),
    CNY("Китайский юань");

    private final String title;

    Currency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

