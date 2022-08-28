package com.example.hhvolgograd.persistence.db.repository.custom.query;

import com.example.hhvolgograd.persistence.db.model.dto.EntityUpdates;
import com.example.hhvolgograd.persistence.db.model.dto.Entry;
import com.example.hhvolgograd.persistence.db.model.dto.UpdateProperties;
import lombok.AllArgsConstructor;
import lombok.val;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.IntegerType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.hhvolgograd.persistence.db.model.dto.UserUpdates.type;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class QueryBuilderImpl implements QueryBuilder {

    private EntityManager entityManager;

    @Override
    public Query updateObjectByIdQuery(EntityUpdates updates, String tableName, long id) {
        val preparedStatement = preparedUpdateStatement(updates, tableName, id);
        val query = entityManager.createNativeQuery(preparedStatement);

        updates
                .fieldNames()
                .stream()
                .map(fieldName -> updates.getGetMethods().get(fieldName).get())
                .filter(UpdateProperties::isUpdated)
                .forEach(
                        dtoProperties -> {
                            val fieldName = dtoProperties.getFieldName();
                            val fieldValue = dtoProperties.getValue();

                            if (isNull(fieldValue) && type(fieldName).equals(Integer.class)) {
                                query
                                        .unwrap(NativeQuery.class)
                                        .setParameter(dtoProperties.getFieldName(), null, IntegerType.INSTANCE);
                            } else{
                                query.setParameter(dtoProperties.getFieldName(), dtoProperties.getValue());
                            }

                        }
                );
        return query;
    }

    @Override
    public <T, FK> Query createAllByForeignKey(List<Entry<T>> creates, String tableName, Entry<FK> foreignKey) {
        final Supplier<String> preparedStatement = () -> preparedCreateAllStatement(creates, tableName, foreignKey);

        return collectionQuery(preparedStatement, creates, foreignKey);
    }

    @Override
    public Query deleteAll(String tableName) {
        return deleteAllByForeignKey(tableName, new Entry<String>(null, null));
    }

    @Override
    public <FK> Query deleteAllByForeignKey(String tableName, Entry<FK> foreignKey) {
        return deleteAllByForeignKeyAndValues(Collections.emptyList(), tableName, foreignKey);
    }

    @Override
    public <T, FK> Query deleteAllByForeignKeyAndValues(
            List<Entry<T>> deletes, String tableName, Entry<FK> foreignKey
    ) {
        final Supplier<String> preparedStatement = () -> preparedDeleteAllStatement(deletes, tableName, foreignKey);

        return collectionQuery(preparedStatement, deletes, foreignKey);
    }

    private <T, FK> Query collectionQuery(Supplier<String> supplier, List<Entry<T>> collectionToProcess, Entry<FK> foreignKey) {
        val query = entityManager.createNativeQuery(supplier.get());

        IntStream
                .range(0, collectionToProcess.size())
                .forEach(i -> {
                    val entry = collectionToProcess.get(i);

                    query.setParameter(entry.getName() + i, entry.getValue());
                });
        setForeignKeyIfPresent(foreignKey, query);
        return query;
    }

    private static <FK> void setForeignKeyIfPresent(Entry<FK> foreignKey, Query query) {
        if (Objects.nonNull(foreignKey.getName())) {
            query.setParameter(foreignKey.getName(), foreignKey.getValue());
        }
    }

    private <T, FK> String preparedCreateAllStatement(List<Entry<T>> creates, String tableName, Entry<FK> foreignKey) {
        val start = "\ninsert into " + tableName
                + " (" + creates.get(0).getName() + ", " + foreignKey.getName() + ")" + " values\n\t";
        val middle = IntStream.range(0, creates.size())
                .mapToObj(i -> "(:" + creates.get(i).getName() + i + ", :" + foreignKey.getName() + ")" )
                .collect(Collectors.joining(",\n\t"));
        return start + middle;
    }

    private <T, FK> String preparedDeleteAllStatement(
            List<Entry<T>> deletes,
            String tableName,
            Entry<FK> foreignKey
    ) {
        val start = "\ndelete from " + tableName;
        val middle = Objects.nonNull(foreignKey.getName())
                ? "\n\twhere " + foreignKey.getName() + " = :" + foreignKey.getName()
                : "";
        final String end;

        if (deletes.size() > 0) {
            end = IntStream
                    .range(0, deletes.size())
                    .mapToObj(i -> {
                        val entry = deletes.get(i);

                        return "\n\t\t" + ":" + entry.getName() + i;
                    })
                    .collect(Collectors.joining(", ", " and " + deletes.get(0).getName() + " in (", "\n\t)"));
        } else {
            end = "";
        }
        return start + middle + end;
    }

    private String preparedUpdateStatement(EntityUpdates updates, String tableName, long id) {
        val start = "\nupdate " + tableName + " \n\tset \n\t\t";
        val end = "\n\twhere id = " + id;

        val middle = updates
                .fieldNames()
                .stream()
                .filter(fieldName -> updates.getGetMethods().get(fieldName).get().isUpdated())
                .map(key -> key + " = :" + key + " ")
                .collect(Collectors.joining(",\n\t\t"));
        return start + middle + end;
    }


}
