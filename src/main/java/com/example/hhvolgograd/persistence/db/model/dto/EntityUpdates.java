package com.example.hhvolgograd.persistence.db.model.dto;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface EntityUpdates {

    Map<String, Supplier<UpdateProperties<?>>> getGetMethods();
    Map<String, Consumer<String>> getSetMethods();
    Collection<String> fieldNames();
}
