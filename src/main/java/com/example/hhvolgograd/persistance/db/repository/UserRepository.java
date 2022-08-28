package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.custom.NativeSqlUserRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepositoryImplementation<User, Long>, NativeSqlUserRepository {

    boolean existsUserByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
