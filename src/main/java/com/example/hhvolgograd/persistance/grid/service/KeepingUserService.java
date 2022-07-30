package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.persistance.db.model.User;

public interface KeepingUserService {

    String mapName = "keptUser";

    void save(String email, String userJson);

    User getUserOrThrow(String email);

}
