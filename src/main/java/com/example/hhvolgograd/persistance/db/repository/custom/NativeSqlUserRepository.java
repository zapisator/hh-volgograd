package com.example.hhvolgograd.persistance.db.repository.custom;

import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;

public interface NativeSqlUserRepository {

    int update(UserUpdates updates, long id);
}
