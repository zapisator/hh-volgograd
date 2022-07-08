package com.example.hhvolgograd.web.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignedJwtBuilder {

    private JWSHeader header;
    private JWTClaimsSet jwtClaimsSet;
    private JWSSigner signer;

    public static SignedJwtBuilder create() {
        return new SignedJwtBuilder();
    }

    public SignedJwtBuilder header(JWSAlgorithm algorithm) {
        this.header = new JWSHeader(algorithm);
        return this;
    }

    public SignedJwtBuilder payload(JWTClaimsSet.Builder builder) {
        this.jwtClaimsSet = builder
                .build();
        return this;
    }

    public SignedJwtBuilder payload(JWTClaimsSet jwtClaimsSet) {
        this.jwtClaimsSet = jwtClaimsSet;
        return this;
    }

    public SignedJwtBuilder signature(JWSSigner signer) {
        this.signer = signer;
        return this;
    }


    public SignedJWT build() throws JOSEException {
        val jwt = new SignedJWT(header, jwtClaimsSet);
        jwt.sign(signer);
        return jwt;
    }
}