package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.persistance.db.model.User;

import java.util.Optional;

public interface CashService {

    void requireNoSuchEmailIsRegistered(String email);

    void requireEmailIsRegistered(String email);

    Optional<User> findUserByEmail(String email);

    User save(User user);
}
