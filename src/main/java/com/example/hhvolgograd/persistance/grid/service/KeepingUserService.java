package com.example.hhvolgograd.persistance.grid.service;

import java.util.Optional;

public interface KeepingUserService {

    String mapName = "keptUser";

    void save(String email, String userJson);

    Optional<String> findUserByEmail(String email);


}
