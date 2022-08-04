package com.example.hhvolgograd.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties({
        JwtProperty.class,
        ProjectMailProperty.class,
        ProjectHazelcastProperty.class
})
@RequiredArgsConstructor
@Getter
public class Configuration {

    private final JwtProperty jwtProperty;
    private final ProjectMailProperty projectMailProperty;
    private final ProjectHazelcastProperty projectHazelcastProperty;
}
