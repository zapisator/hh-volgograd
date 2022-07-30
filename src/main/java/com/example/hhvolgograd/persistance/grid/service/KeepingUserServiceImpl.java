package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.example.hhvolgograd.persistance.db.model.User;
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
    public KeepingUserServiceImpl(HazelcastManager factory) {
        this.map = factory.getInstance().getMap(mapName);
    }
    @Override
    public void save(String email, String userJson) {
        checkCallIsInTime(email);
        map.put(email, userJson, storageTime, unit);
    }

    @Override
    public User getUserOrThrow(String email) {
        return Optional
                .ofNullable(map.get(email))
                .map(User::fromJson)
                .orElseThrow(
                        () -> new NotRegisteringUserException(format(
                                "User with email '%s' is not registering, "
                                        + "or too much time spent after register form was sent. Try again to register.",
                                email
                        ))
                );
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
