package com.example.hhvolgograd.persistence.db.model.dto;

import lombok.Getter;

@Getter
public class UpdateProperties<T> {
    private final String fieldName;
    private final T value;
    private final boolean isUpdated;

    public UpdateProperties(String fieldName, T value) {
        this.fieldName = fieldName;
        this.value = value;
        isUpdated = true;
    }

    public UpdateProperties() {
        fieldName = null;
        value = null;
        isUpdated = false;
    }
}
