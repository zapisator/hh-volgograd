package com.example.hhvolgograd.web.service;

public interface LoginService {
    void login(String email);

    String token(String email, String otp);
}
