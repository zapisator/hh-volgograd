package com.example.hhvolgograd.persistance.grid.service;

import java.util.Optional;

public interface OtpService {
    String mapName = "otp";

    String save(String email);

    Optional<String> findOtpByEmail(String email);
}
