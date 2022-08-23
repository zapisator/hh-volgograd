package com.example.hhvolgograd.web.service.patch.validation;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface OperationValidator {

    boolean validate(JsonNode node);
}
