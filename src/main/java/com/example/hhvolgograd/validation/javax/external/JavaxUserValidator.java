package com.example.hhvolgograd.validation.javax.external;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
import lombok.val;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class JavaxUserValidator {

    public void validate(UserUpdates updates) {
        val validator = new JavaxValidator<User>();
        val user = new User();
        val fieldNames = new HashSet<String>();

        appendNameIfIsUpdated(updates, user, fieldNames);
        appendAgeIfIsUpdated(updates, user, fieldNames);
        appendEmailIfIsUpdated(updates, user, fieldNames);
        validator.validateFields(user, fieldNames);
    }

    private void appendNameIfIsUpdated(UserUpdates updates, User user, Set<String> fieldNames) {
        val name = updates.getName();

        if (Objects.nonNull(name) && name.isUpdated()) {
            user.setName(name.getValue());
            fieldNames.add(name.getFieldName());
        }
    }

    private void appendAgeIfIsUpdated(UserUpdates updates, User user, Set<String> fieldNames) {
        val age = updates.getAge();

        if (Objects.nonNull(age) && age.isUpdated()) {
            user.setAge(age.getValue());
            fieldNames.add(age.getFieldName());
        }
    }

    private void appendEmailIfIsUpdated(UserUpdates updates, User user, Set<String> fieldNames) {
        val email = updates.getEmail();

        if (Objects.nonNull(email) && email.isUpdated()) {
            user.setEmail(email.getValue());
            fieldNames.add(email.getFieldName());
        }
    }
}
