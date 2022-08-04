package com.example.hhvolgograd.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "project.hazelcast")
@NoArgsConstructor
@Getter
@Setter
public class ProjectHazelcastProperty {
    private Duration otpStorageDuration;
    private Duration keepUserStorageDuration;
}
