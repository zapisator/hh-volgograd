package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.map.IMap;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Service
public class OtpServiceImpl implements OtpService {

    public static final int storageTime = 30;
    public static final TimeUnit unit = TimeUnit.SECONDS;
    private final IMap<String, String> map;

    @Autowired
    public OtpServiceImpl(HazelcastManager factory) {
        this.map = factory.getInstance().getMap(mapName);
    }
    @Override
    public String save(String email) {
        val otp = UUID.randomUUID().toString();

        checkCallIsInTime(email);
        map.put(email, otp, storageTime, unit);
        return otp;
    }

    @Override
    public Optional<String> findOtpByEmail(String email) {
        return Optional.ofNullable(map.get(email));
    }

    private void checkCallIsInTime(String email) {
        if (map.containsKey(email)) {
            throw new TooEarlyToContactServiceException(
                    format(
                            "You try to call one time password (OTP) to early. "
                                    + "Otp is issued for %s %s. Please, wait until this time.",
                            storageTime, unit
                    )
            );
        }
    }
}
