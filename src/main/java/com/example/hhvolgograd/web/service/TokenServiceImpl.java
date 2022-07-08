package com.example.hhvolgograd.web.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class TokenServiceImpl implements JwtDecoder, TokenGenerator {

    public static final String SCOPE = "scope";
    public static final String EMAIL = "email";

    @Value("jwt.secret")
    private final String secret;
    private final JWSAlgorithm algorithm = JWSAlgorithm.HS256;
    private final String issuer = "hhVolgograd";

    @Override
    @SneakyThrows
    public String generate(String email) {
        val issuedAt = new Date();
        val validityPeriodMillis = 30 * 60 * 1000;
        val expiresAt = new Date(issuedAt.getTime() + validityPeriodMillis);

        return SignedJwtBuilder.create()
                .header(algorithm)
                .payload(new JWTClaimsSet.Builder()
                        .issuer(issuer)
                        .issueTime(issuedAt)
                        .expirationTime(expiresAt)
                        .subject(email)
                        .claim(EMAIL, email)
                        .claim(SCOPE, email))
                .signature(new MACSigner(secret))
                .build()
                .serialize();
    }

    @Override
    @SneakyThrows
    public Jwt decode(String token) throws JwtException {
        val tokenObject = JWTParser.parse(token);

        return Jwt.withTokenValue(token)
                .header("alg", tokenObject.getHeader().getAlgorithm())
                .header("typ", "JWT")
                .issuer(tokenObject
                        .getJWTClaimsSet()
                        .getIssuer())
                .claim(SCOPE, tokenObject
                        .getJWTClaimsSet()
                        .getClaim(SCOPE))
                .claim(EMAIL, tokenObject
                        .getJWTClaimsSet()
                        .getClaim(EMAIL))
                .expiresAt(tokenObject
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .toInstant())
                .issuedAt(tokenObject
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant())
                .build();
    }
}
