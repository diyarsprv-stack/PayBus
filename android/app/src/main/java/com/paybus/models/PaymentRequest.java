package com.paybus.models;

public class PaymentRequest {
    private double amount;
    private String provider;
    private String card_id;
    private String bus_route;

    public PaymentRequest(double amount, String provider, String cardId, String busRoute) {
        this.amount = amount;
        this.provider = provider;
        this.card_id = cardId;
        this.bus_route = busRoute;
    }
}
