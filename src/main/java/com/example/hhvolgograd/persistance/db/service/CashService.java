package com.example.hhvolgograd.persistance.db.service;

import com.example.hhvolgograd.persistance.db.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface CashService {

    void requireNoSuchEmailIsRegistered(String email);

    void requireEmailIsRegistered(String email);

    Optional<User> findUserByEmail(String email);

    User save(User user);

    Page<User> findAll(Specification<User> specification, Pageable pageable);
}
