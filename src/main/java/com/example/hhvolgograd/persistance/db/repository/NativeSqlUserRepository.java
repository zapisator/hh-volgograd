package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;

public interface NativeSqlUserRepository {

    void update(UserUpdates updates, long id);
}
