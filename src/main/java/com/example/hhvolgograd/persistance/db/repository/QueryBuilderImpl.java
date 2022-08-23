package com.example.hhvolgograd.persistance.db.repository;

import com.example.hhvolgograd.persistance.db.model.dto.DtoProperties;
import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
import lombok.AllArgsConstructor;
import lombok.val;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.stream.Collectors;

@AllArgsConstructor
public class QueryBuilderImpl implements QueryBuilder {

    private EntityManager entityManager;

    @Override
    public Query updateByIdQuery(UserUpdates updates, String tableName, long id) {
        val preparedStatement = preparedStatement(updates, tableName, id);
        val query = entityManager.createNativeQuery(preparedStatement);

        updates
                .fieldNames()
                .stream()
                .map(fieldName -> updates.getGetMethods().get(fieldName).get())
                .filter(DtoProperties::isUpdated)
                .forEach(dtoProperties -> query.setParameter(dtoProperties.getFieldName(), dtoProperties.getValue()));
        return query;
    }

    private String preparedStatement(UserUpdates updates, String tableName, long id) {
        val start = "\nupdate " + tableName + " \n\tset \n\t\t";
        val end = "\n\twhere id = " + id + ";";

        val middle = updates.fieldNames()
                .stream()
                .filter(fieldName -> updates.getGetMethods().get(fieldName).get().isUpdated())
                .map(key -> key + " = :" + key + " ")
                .collect(Collectors.joining(",\n\t\t"));
        return start + middle + end;
    }


}
