package com.example.hhvolgograd.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "project.jwt")
@RequiredArgsConstructor
@Getter
@Setter
public class JwtProperty {

    private String secret;
}
