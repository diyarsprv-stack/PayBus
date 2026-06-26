package com.paybus.models;

public class TokenResponse {
    public String access_token;
    public String token_type;
    public UserResponse user;

    public static class UserResponse {
        public String id;
        public String phone_number;
        public String full_name;
        public boolean is_verified;
    }
}
