package com.paybus.models;

public class VerifySMSRequest {
    private String phone_number;
    private String code;

    public VerifySMSRequest(String phoneNumber, String code) {
        this.phone_number = phoneNumber;
        this.code = code;
    }
}
