package com.example.hhvolgograd.persistence.grid.service;

import com.example.hhvolgograd.configuration.ProjectHazelcastProperty;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Getter
@AllArgsConstructor
public class HazelcastFactory {

    private final HazelcastInstance instance;
    private final ProjectHazelcastProperty hazelcastProperty;

    public HazelcastMapService createInstance(HazelcastMapServiceType type) {
        final IMap<String, String> map = instance.getMap(type.getMapName());
        final Duration duration;

        switch (type) {
            case OTP_SERVICE:
                duration = hazelcastProperty.getOtpStorageDuration();
                break;
            case KEEPING_USER_SERVICE:
                duration = hazelcastProperty.getKeepUserStorageDuration();
                break;
            default:
                throw new IllegalArgumentException("Type of Hazelcast service can not be null");
        }
        return new HazelcastMapServiceImpl(map, duration);
    }

}
