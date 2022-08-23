package com.example.hhvolgograd.web.service.patch.validation;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class CommonValidator implements OperationValidator {
    @Override
    public boolean validate(JsonNode node) {
        val operation = node.path("operation");
        val opNames = List.of(
                "add",
                "remove",
                "replace"
        );
        val decision = node.has("operation")
                && opNames.contains(operation.textValue())
                && node.has("path")
                && (operation.textValue().equals("remove") || node.has("value"));

        if (!decision) {
            log.warn("Wrong operation type '{}' in patch member '{}'", operation, node.textValue());
        }
        return decision;
    }
}
