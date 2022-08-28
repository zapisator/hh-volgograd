package com.example.hhvolgograd.validation.patch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class CommonValidator implements OperationValidator {
    @Override
    public boolean validate(JsonNode node) {
        val operationPathName = "op";
        val operation = node.path(operationPathName);
        val opNames = List.of(
                "add",
                "remove",
                "replace"
        );
        val decision = node.has(operationPathName)
                && opNames.contains(operation.textValue())
                && node.has("path")
                && (operation.textValue().equals("remove") || node.has("value"));

        if (!decision) {
            log.warn("Wrong operation type '{}' in patch member '{}'", operation, node.textValue());
        }
        return decision;
    }
}
