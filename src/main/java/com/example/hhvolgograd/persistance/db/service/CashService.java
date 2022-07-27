package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.persistance.db.model.User;

public interface CashService {

    boolean existsUserByEmail(String email);

    User save(User user);
}
