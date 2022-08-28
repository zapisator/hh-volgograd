package com.example.hhvolgograd.persistence.db.repository;

import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.repository.custom.NativeSqlUserRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepositoryImplementation<User, Long>, NativeSqlUserRepository {

    boolean existsUserByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
