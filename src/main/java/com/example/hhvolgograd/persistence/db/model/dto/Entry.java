package com.example.hhvolgograd.persistence.db.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Entry<T> {
    private final String name;
    private final T value;
}
