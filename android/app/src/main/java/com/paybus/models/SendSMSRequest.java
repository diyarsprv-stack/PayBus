package com.paybus.models;

public class SendSMSRequest {
    private String phone_number;

    public SendSMSRequest(String phoneNumber) {
        this.phone_number = phoneNumber;
    }
}
