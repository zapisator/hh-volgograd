package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.persistance.db.model.User;

public interface CashService {

    void checkIfNoSuchEmailIsRegistered(String email);

    void checkIfEmailIsRegistered(String email);

    User findUserByEmail(String email);

    User save(User user);
}
