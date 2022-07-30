package com.example.hhvolgograd.persistance.grid.service;

public interface OtpService {
    String mapName = "otp";

    String save(String email);

    String getOtpOrThrow(String email);
}
