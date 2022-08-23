package com.example.hhvolgograd.persistance.db.repository;


import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;

import javax.persistence.Query;

public interface QueryBuilder {
    Query updateByIdQuery(UserUpdates updates, String tableName, long id);
}
