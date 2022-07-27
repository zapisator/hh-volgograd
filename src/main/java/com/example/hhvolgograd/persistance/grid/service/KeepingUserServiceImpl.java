package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Service
public class KeepingUserServiceImpl implements KeepingUserService {

    public static final int storageTime = 10;
    public static final TimeUnit unit = TimeUnit.MINUTES;
    private final IMap<String, String> map;

    @Autowired
    private KeepingUserServiceImpl(HazelcastManager factory) {
        this.map = factory.getInstance().getMap(mapName);
    }
    @Override
    public void save(String email, String userJson) {
        checkCallIsInTime(email);
        map.put(email, userJson, storageTime, unit);
    }

    @Override
    public Optional<String> findUserByEmail(String email) {
        return Optional.ofNullable(map.get(email));
    }

    private void checkCallIsInTime(String email) {
        if (map.containsKey(email)) {
            throw new TooEarlyToContactServiceException(
                    format(
                            "You try to register an email '%s' too soon. "
                                    + "This action is permitted once every %s %s. Please, wait until this time.",
                            email, storageTime, unit
                    )
            );
        }
    }

}
