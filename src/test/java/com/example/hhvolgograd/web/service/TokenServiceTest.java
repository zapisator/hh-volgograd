package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.configuration.Configuration;
import com.example.hhvolgograd.configuration.JwtProperty;
import com.example.hhvolgograd.web.security.Scope;
import com.example.hhvolgograd.web.security.TokenParameter;
import com.example.hhvolgograd.web.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Configuration.class})
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class TokenServiceTest {

    private final JwtProperty jwtProperty;

    @Test
    void generate() throws JSONException {
        val email = "a@gmail.com";
        val userId = Long.toString(1L);
        val scope = Arrays.stream(Scope.values())
                .map(Scope::getValue)
                .collect(Collectors.joining(", "));
        val tokenService = new TokenService(jwtProperty);

        val token = tokenService.generate(
                TokenParameter.create()
                        .withEmail(email)
                        .withUserId(userId)
                        .withScope(scope)
                        .build()
        );
        val payload = token.split("\\.")[1];
        val payloadDecoded = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
        val payloadJson = new JSONObject(payloadDecoded);

        assertAll(
                () -> assertEquals(email, payloadJson.getString("email")),
                () -> assertEquals(scope, payloadJson.getString("scope")),
                () -> assertEquals(userId, payloadJson.getString("sub")),
                () -> assertEquals("hhVolgograd", payloadJson.getString("iss")),
                () -> assertDoesNotThrow(() -> Integer.valueOf(payloadJson.getString("iat"))),
                () -> assertDoesNotThrow(() -> Integer.valueOf(payloadJson.getString("exp")))
        );
    }

    @Test
    void decode() {
        val tokenSerialized = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiJhQGdtYWlsLmNvbSIsInNjb3BlIjoiYUBnbWFpbC5jb20iLCJpc3MiOiJoaFZvbGdvZ3J"
                + "hZCIsImV4cCI6MTk3Mjc2Mjg4NCwiaWF0IjoxNjU3NDAyODg0LCJlbWFpbCI6ImFAZ21haWwuY29tIn0"
                + ".dX7sfWuwDtEzvNMxMQt33xmG57Yc-0CriV-tNv8pwTY";
        val referenceEmail = "a@gmail.com";
        val token = new TokenService(jwtProperty).decode(tokenSerialized);

        val email = token.getClaim(TokenService.EMAIL);

        assertEquals(referenceEmail, email);
    }
}