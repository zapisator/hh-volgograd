package com.example.hhvolgograd.web.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenParameter {

    private String email;
    private String userId;
    private String scope;

    public static TokenParameterBuilder create() {
        return new TokenParameter().new TokenParameterBuilder();
    }

    public class TokenParameterBuilder {

        public TokenParameterBuilder email(String email) {
            TokenParameter.this.email = email;
            return this;
        }

        public TokenParameterBuilder userId(String userId) {
            TokenParameter.this.userId = userId;
            return this;
        }

        public TokenParameterBuilder scope(String... scope) {
            TokenParameter.this.scope = Arrays
                    .stream(scope)
                    .reduce((accumulator, current) -> accumulator + ", " + current)
                    .orElse("");
            return this;
        }

        public TokenParameter build() {
            return TokenParameter.this;
        }

    }
}
