package com.example.hhvolgograd.persistence.grid.service;

import lombok.Getter;

@Getter
public enum HazelcastMapServiceType {
    OTP_SERVICE ("otp"),
    KEEPING_USER_SERVICE ("kept-user");

    private final String mapName;
    HazelcastMapServiceType(String mapName) {
        this.mapName = mapName;
    }
}
