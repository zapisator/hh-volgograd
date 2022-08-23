package com.example.hhvolgograd.persistance.db.model.dto;

import lombok.Getter;

@Getter
public class DtoProperties <T> {
    private final String fieldName;
    private final T value;
    private final boolean isUpdated;

    public DtoProperties(String fieldName, T value) {
        this.fieldName = fieldName;
        this.value = value;
        isUpdated = true;
    }

    public DtoProperties() {
        fieldName = null;
        value = null;
        isUpdated = false;
    }
}
