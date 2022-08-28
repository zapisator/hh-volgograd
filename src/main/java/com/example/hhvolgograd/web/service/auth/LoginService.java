package com.example.hhvolgograd.web.service.auth;

public interface LoginService {
    void login(String email);

    String token(String email, String otp);
}
