package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.OptType;

class StringToOptTypeConverterTest {

    StringToOptTypeConverter stringToOptTypeConverter = new StringToOptTypeConverter();

    @Test
    public void convertInvalidOptTypeTest() {
        OptType optType = stringToOptTypeConverter.convert("invalid");
        assertNull(optType);
    }

    @Test
    public void convertOptTypeInTest() {
        OptType optType = stringToOptTypeConverter.convert(OptType.IN.value());
        assertNotNull(optType);
        assertEquals(OptType.IN, optType);
    }

    @Test
    public void convertOptTypeOutTest() {
        OptType optType = stringToOptTypeConverter.convert(OptType.OUT.value());
        assertNotNull(optType);
        assertEquals(OptType.OUT, optType);
    }
}
