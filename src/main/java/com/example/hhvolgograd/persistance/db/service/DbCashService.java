package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.lang.String.format;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
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

    @Override
    public Page<User> findAll(Specification<User> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }


}
