package com.example.hhvolgograd.web.service;

import lombok.val;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TokenServiceTest.class})
class TokenServiceTest {

    @Value("${jwt.secret}")
    private String secret;

    @Test
    void generate() throws JSONException {
        val email = "a@gmail.com";
        val tokenService = new TokenServiceImpl(secret);

        val token = tokenService.generate(email);
        val payload = token.split("\\.")[1];
        val payloadDecoded = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
        val payloadJson = new JSONObject(payloadDecoded);

        assertAll(
                () -> assertEquals(email, payloadJson.getString("email")),
                () -> assertEquals(email, payloadJson.getString("scope")),
                () -> assertEquals(email, payloadJson.getString("sub")),
                () -> assertEquals("hhVolgograd", payloadJson.getString("iss")),
                () -> assertDoesNotThrow(() -> Integer.valueOf(payloadJson.getString("iat"))),
                () -> assertDoesNotThrow(() -> Integer.valueOf(payloadJson.getString("exp")))
        );
    }

    @Test
    void decode() {
        val tokenSerialized = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiJhQGdtYWlsLmNvbSIsInNjb3BlIjoiYUBnbWFpbC5jb20iLCJpc3MiOiJoaFZvbGdvZ3J"
                + "hZCIsImV4cCI6MTY1NzI1MTM0OSwiaWF0IjoxNjU3MjQ5NTQ5LCJlbWFpbCI6ImFAZ21haWwuY29tIn0"
                + ".fev00PmRaI90xMNsQQM3VVlRKbot4Y4bMQ9Fbtz8rsI";
        val referenceEmail = "a@gmail.com";
        val token = new TokenServiceImpl(secret).decode(tokenSerialized);

        val email = token.getClaim(TokenServiceImpl.EMAIL);

        assertEquals(referenceEmail, email);
    }
}