package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DbCashService implements CashService {

    private final UserRepository repository;

    @Override
    public boolean existsUserByEmail(String email) {
        return repository.existsUserByEmail(email);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }
}
