package com.example.hhvolgograd.persistence.db.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.model.dto.Entry;
import com.example.hhvolgograd.persistence.db.model.dto.UserUpdates;
import com.example.hhvolgograd.persistence.db.repository.PhoneRepository;
import com.example.hhvolgograd.persistence.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
@AllArgsConstructor
public class DbCashService implements CashService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;

    @Override
    public void requireNoSuchEmailIsRegistered(String email) {
        if (userRepository.existsUserByEmail(email)) {
            throw new DuplicateKeyException(format("User with '%s' email has already registered.", email));
        }
    }

    @Override
    public void requireEmailIsRegistered(String email) {
        if (!userRepository.existsUserByEmail(email)) {
            throw new NotRegisteringUserException(format("User with '%s' email has no been registered.", email));
        }
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<User> findAll(Specification<User> specification, Pageable pageable) {
        return userRepository.findAll(specification, pageable);
    }

    @Override
    public int updateUser(UserUpdates updates, long id) {
        return userRepository.update(updates, id);
    }

    @Override
    public void updatePhones(List<Entry<String>> creates, List<Entry<String>> deletes, long userId) {
       val updatesCount = phoneRepository.deletePhonesByUserIdAndValues(userId, deletes);
        phoneRepository.addPhonesByUserId(userId, creates);
    }

    @Override
    public void deletePhonesBy(long userId) {
//        phoneRepository.deleteAllByUserId(userId);
    }

    @Override
    public void updateProfile() {

    }

}
