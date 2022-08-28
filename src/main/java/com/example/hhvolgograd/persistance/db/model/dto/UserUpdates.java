package com.example.hhvolgograd.persistance.db.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@NoArgsConstructor
public class UserUpdates implements EntityUpdates {
    private static final Map<String, Class<?>> CLASS = Map.of(
            "name", String.class,
            "age", Integer.class,
            "email", String.class
    );
    private final Map<String, Consumer<String>> setMethods = Map.of(
            "name", value -> name = new UpdateProperties<>("name", value),
            "age", value -> age = new UpdateProperties<>("age", (value.equals("null") ? null : Integer.valueOf(value))),
            "email", value -> email = new UpdateProperties<>("email", value)
    );
    private final Map<String, Supplier<UpdateProperties<?>>> getMethods = Map.of(
            "name", this::getName,
            "age", this::getAge,
            "email", this::getEmail
    );
    private UpdateProperties<String> name = new UpdateProperties<>();
    private UpdateProperties<Integer> age = new UpdateProperties<>();
    private UpdateProperties<String> email = new UpdateProperties<>();

    public static UserUpdates create(Map<String, String> updates) {
        val user = new UserUpdates();

        updates.forEach(user::set);
        return user;
    }

    public static Class<?> type(String fieldName) {
        return CLASS.get(fieldName);
    }

    public void set(String fieldName, String value) {
        getSetMethods().get(fieldName).accept(value);
    }

    @Override
    public Collection<String> fieldNames() {
        return List.of(
                "name",
                "age",
                "email"
        );
    }


}
