package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

@Slf4j
public class HazelcastMapServiceImpl implements HazelcastMapService {

    private final IMap<String, String> map;
    private final Duration duration;

    public HazelcastMapServiceImpl(IMap<String, String> map, Duration duration) {
        this.map = map;
        this.duration = duration;
    }

    public void save(String email, String userJson) {
        requireCallIsInTime(email);
        map.put(email, userJson, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public String read(String email) {
        return Optional
                .ofNullable(map.get(email))
                .orElseThrow(
                        () -> new NotRegisteringUserException(format(
                                "User with email '%s' is not registering, "
                                        + "or too much time spent after register form was sent. Try again to register.",
                                email
                        ))
                );
    }

    private void requireCallIsInTime(String email) {
        if (map.containsKey(email)) {
            log.debug("email '{}' is still contained at the map", email);
            throw new TooEarlyToContactServiceException(
                    format(
                            "You try to call the service too early for '%s'. "
                                    + "This action is permitted once every %s. "
                                    + "Please, wait until this time.",
                            email, formatDurationHMS(duration.toMillis())
                    )
            );
        }
        log.debug("email '{}' is NOT contained at the map", email);
    }

}
