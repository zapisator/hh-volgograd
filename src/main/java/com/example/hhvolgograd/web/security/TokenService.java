package com.example.hhvolgograd.web.security;

import com.example.hhvolgograd.configuration.JwtProperty;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService implements JwtDecoder, TokenGenerator {

    public static final String SCOPE = "scope";
    public static final String EMAIL = "email";

    private final JwtProperty jwtProperty;
    private final JWSAlgorithm algorithm = JWSAlgorithm.HS256;

    @Override
    public String generate(TokenParameter parameters) {
        return generate(parameters.getEmail(), parameters.getUserId(), parameters.getScope());
    }

    @Override
    @SneakyThrows
    public String generate(String email, String userId, String scope) {
        val issuedAt = new Date();
        val expiresAt = new Date(issuedAt.getTime() + Duration.ofMinutes(30L).toMillis());

        String issuer = "hhVolgograd";
        return SignedJwtBuilder.create()
                .header(algorithm)
                .payload(new JWTClaimsSet.Builder()
                        .issuer(issuer)
                        .issueTime(issuedAt)
                        .expirationTime(expiresAt)
                        .subject(userId)
                        .claim(EMAIL, email)
                        .claim(SCOPE, scope))
                .signature(new MACSigner(jwtProperty.getSecret()))
                .build()
                .serialize();
    }

    @Override
    @SneakyThrows
    public Jwt decode(String token) throws JwtException {
        val tokenObject = JWTParser.parse(token);
        val claims = tokenObject.getJWTClaimsSet();

        return Jwt.withTokenValue(token)
                .header("alg", tokenObject.getHeader().getAlgorithm())
                .header("typ", "JWT")
                .issuer(claims.getIssuer())
                .claim(SCOPE, claims.getClaim(SCOPE))
                .claim(EMAIL, claims.getClaim(EMAIL))
                .expiresAt(claims.getExpirationTime().toInstant())
                .issuedAt(claims.getIssueTime().toInstant())
                .build();
    }

}
