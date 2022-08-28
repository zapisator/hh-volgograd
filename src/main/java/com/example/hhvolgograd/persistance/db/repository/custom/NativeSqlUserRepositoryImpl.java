package com.example.hhvolgograd.persistance.db.repository.custom;

import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
import com.example.hhvolgograd.persistance.db.repository.custom.query.QueryBuilder;
import com.example.hhvolgograd.persistance.db.repository.custom.query.QueryBuilderImpl;
import com.example.hhvolgograd.validation.javax.external.JavaxUserValidator;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@AllArgsConstructor
public class NativeSqlUserRepositoryImpl implements NativeSqlUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int update(UserUpdates updates, long id) {
        entityManager.clear();
        new JavaxUserValidator().validate(updates);

        final QueryBuilder queryBuilder = new QueryBuilderImpl(entityManager);
        val query = queryBuilder.updateObjectByIdQuery(updates, "users", id);

        return query.executeUpdate();
    }

}
