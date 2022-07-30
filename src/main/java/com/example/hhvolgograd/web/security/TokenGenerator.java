package com.example.hhvolgograd.web.security;

public interface TokenGenerator {

    String generate(TokenParameter parameters);
    String generate(String email, String userId, String scope);
}
