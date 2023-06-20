package com.example.applicationgateway.dto;

import lombok.Data;

@Data
public class LoginRequest {

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest() {
    }

    private String username;
    private String password;
}
