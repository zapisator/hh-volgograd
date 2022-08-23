package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
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
    public void update(UserUpdates updates, long id) {
        entityManager.clear();
        val queryBuilder = new QueryBuilderImpl(entityManager);
        val query = queryBuilder.updateByIdQuery(updates, "users", id);

        query.executeUpdate();
    }

}
