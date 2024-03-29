package com.example.hhvolgograd.persistence.db.repository.custom.query;


import com.example.hhvolgograd.persistence.db.model.dto.EntityUpdates;
import com.example.hhvolgograd.persistence.db.model.dto.Entry;

import javax.persistence.Query;
import java.util.List;

public interface QueryBuilder {
    Query updateObjectByIdQuery(EntityUpdates updates, String tableName, long id);

    <T, FK> Query createAllByForeignKey(List<Entry<T>> creates, String tableName, Entry<FK> foreignKey);

    Query deleteAll(String tableName);

    <FK> Query deleteAllByForeignKey(String tableName, Entry<FK> foreignKey);

    <T, FK> Query deleteAllByForeignKeyAndValues(
            List<Entry<T>> deletes,
            String tableName,
            Entry<FK> foreignKey
    );
}
