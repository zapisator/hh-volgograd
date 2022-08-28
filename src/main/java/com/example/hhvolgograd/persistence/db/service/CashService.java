package com.example.hhvolgograd.persistence.db.service;

import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.model.dto.Entry;
import com.example.hhvolgograd.persistence.db.model.dto.UserUpdates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface CashService {

    void requireNoSuchEmailIsRegistered(String email);

    void requireEmailIsRegistered(String email);

    Optional<User> findUserByEmail(String email);

    User save(User user);

    Page<User> findAll(Specification<User> specification, Pageable pageable);

    int updateUser(UserUpdates updates, long id);

    int updatePhones(List<Entry<String>> creates, List<Entry<String>> deletes, long userId);

    int deletePhonesBy(long userId);

    void updateProfile();

}
