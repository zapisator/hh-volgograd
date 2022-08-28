package com.example.hhvolgograd.persistence.db.repository.custom;

import com.example.hhvolgograd.persistence.db.model.dto.UserUpdates;

public interface NativeSqlUserRepository {

    int update(UserUpdates updates, long id);
}
