package com.example.hhvolgograd.validation.javax.external;

import lombok.val;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import java.util.Set;

public class JavaxValidator <T> {

    public void validateFields(T object, Set<String> fieldNames) {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();

            for (ConstraintViolation<T> violation : validator.validate(object)) {
                val violatingFieldName = violation.getPropertyPath().toString();

                if (fieldNames.contains(violatingFieldName)) {
                    throw new ValidationException(violation.getMessage());
                }
            }
        }
    }

}
