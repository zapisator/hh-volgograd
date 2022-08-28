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
public class PhoneUpdates implements EntityUpdates {

    private final Map<String, Consumer<String>> setMethods = Map.of(
            "value", value -> this.value = new UpdateProperties<>("value", value)
    );
    private final Map<String, Supplier<UpdateProperties<?>>> getMethods = Map.of(
            "value", this::getValue
    );
    private UpdateProperties<String> value = new UpdateProperties<>();

    public static PhoneUpdates create(Map<String, String> updates) {
        val phone = new PhoneUpdates();

        updates.forEach(phone::set);
        return phone;
    }

    public void set(String fieldName, String value) {
        getSetMethods().get(fieldName).accept(value);
    }

    @Override
    public Collection<String> fieldNames() {
        return List.of(
                "value"
        );
    }
}
