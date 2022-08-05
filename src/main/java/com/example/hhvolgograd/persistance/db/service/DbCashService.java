package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class DbCashService implements CashService {

    private final UserRepository repository;

    @Override
    public void requireNoSuchEmailIsRegistered(String email) {
        if (repository.existsUserByEmail(email)) {
            throw new DuplicateKeyException(format("User with '%s' email has already registered.", email));
        }
    }

    @Override
    public void requireEmailIsRegistered(String email) {
        if (!repository.existsUserByEmail(email)) {
            throw new NotRegisteringUserException(format("User with '%s' email has no been registered.", email));
        }
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return repository.findUserByEmail(email);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }
}
