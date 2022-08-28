package com.example.hhvolgograd.validation.patch;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface OperationValidator {

    boolean validate(JsonNode node);
}
