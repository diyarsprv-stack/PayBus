package com.paybus.models;

public class AddCardRequest {
    private String card_number;
    private String card_holder;
    private String expire_date;
    private String provider;

    public AddCardRequest(String cardNumber, String cardHolder, String expireDate, String provider) {
        this.card_number = cardNumber;
        this.card_holder = cardHolder;
        this.expire_date = expireDate;
        this.provider = provider;
    }
}
