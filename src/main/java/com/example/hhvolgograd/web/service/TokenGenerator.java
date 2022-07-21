package com.example.hhvolgograd.web.service;

public interface TokenGenerator {

    String generate(TokenParameter parameters);
    String generate(String email, String userId, String scope);
}
