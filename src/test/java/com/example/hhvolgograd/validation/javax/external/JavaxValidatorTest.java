package com.example.hhvolgograd.validation.javax.external;

import com.example.hhvolgograd.TestUtils;
import com.example.hhvolgograd.persistence.db.model.Phone;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaxValidatorTest {

    @Test
    void validateFields_phoneValuePrintable_doesNotThrow() {
        val validator = new JavaxValidator<>();
        val phone = new Phone();

        phone.setValue(TestUtils.randomStringWithNonZeroLength());

        assertDoesNotThrow(
                () -> validator.validateFields(phone, Set.of("value"))
        );
    }

    @Test
    void validateFields_phoneValueNotPrintable_throws() {
        val validator = new JavaxValidator<>();
        val phone = new Phone();

        phone.setValue("\n");

        assertThrows(
                ValidationException.class,
                () -> validator.validateFields(phone, Set.of("value"))
        );
    }
}