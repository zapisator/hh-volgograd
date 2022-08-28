package com.example.hhvolgograd.persistence.db.repository.custom;

import com.example.hhvolgograd.persistence.db.model.Phone;
import com.example.hhvolgograd.persistence.db.model.dto.Entry;
import com.example.hhvolgograd.persistence.db.repository.custom.query.QueryBuilder;
import com.example.hhvolgograd.persistence.db.repository.custom.query.QueryBuilderImpl;
import com.example.hhvolgograd.validation.javax.external.JavaxValidator;
import lombok.AllArgsConstructor;
import lombok.val;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class NativeSqlPhoneRepositoryImpl implements NativeSqlPhoneRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int deletePhonesByUserIdAndValues(long UserId, List<Entry<String>> deletes) {
        if (deletes.isEmpty()) {
            return 0;
        }
        validate(deletes);
        entityManager.clear();

        final QueryBuilder queryBuilder = new QueryBuilderImpl(entityManager);
        val query = queryBuilder.deleteAllByForeignKeyAndValues(
                deletes, "phones", new Entry<>("user_id", UserId)
        );

        return query.executeUpdate();
    }

    @Override
    public int addPhonesByUserId(long userId, List<Entry<String>> creates) {
        if (creates.isEmpty()) {
            return 0;
        }
        validate(creates);
        entityManager.clear();

        final QueryBuilder queryBuilder = new QueryBuilderImpl(entityManager);
        val query = queryBuilder.createAllByForeignKey(
                creates, "phones", new Entry<>("user_id", userId)
        );

        return query.executeUpdate();
    }

    private void validate(List<Entry<String>> entries) {
        val validator = new JavaxValidator<Phone>();
        val fieldNames = Set.of(entries.get(0).getName());

        entries
                .stream()
                .map(entry -> {
                    val phone = new Phone();

                    phone.setValue(entry.getValue());
                    return phone;
                })
                .forEach(phone -> validator.validateFields(phone, fieldNames));
    }
}
