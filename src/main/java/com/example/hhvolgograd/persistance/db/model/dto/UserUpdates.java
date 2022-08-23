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
public class UserUpdates {
    private final Map<String, Consumer<String>> setMethods = Map.of(
            "name", value -> name = new DtoProperties<>("name", value),
            "age", value -> age = new DtoProperties<>("age", (value == null ? null : Long.valueOf(value))),
            "email", value -> email = new DtoProperties<>("email", value)
    );
    private final Map<String, Supplier<DtoProperties<?>>> getMethods = Map.of(
            "name", this::getName,
            "age", this::getAge,
            "email", this::getEmail
    );
    private DtoProperties<String> name = new DtoProperties<>();
    private DtoProperties<Long> age = new DtoProperties<>();
    private DtoProperties<String> email = new DtoProperties<>();

    public static UserUpdates create(Map<String, String> updates) {
        val user = new UserUpdates();

        updates.forEach(user::set);
        return user;
    }

    public void set(String fieldName, String value) {
        setMethods.get(fieldName).accept(value);
    }

    public Collection<String> fieldNames() {
        return List.of(
                "name",
                "age",
                "email"
        );
    }

}
