package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.PreferenceStatus;

class StringToPreferenceStatusConverterTest {

    StringToPreferenceStatusConverter stringToPreferenceStatusConverter = new StringToPreferenceStatusConverter();

    @Test
    public void convertInvalidPreferenceStatusTest() {
        PreferenceStatus preferenceStatus = stringToPreferenceStatusConverter.convert("invalid");
        assertNull(preferenceStatus);
    }

    @Test
    public void convertPreferenceStatusEnableTest() {
        PreferenceStatus preferenceStatus = stringToPreferenceStatusConverter.convert(PreferenceStatus.ENABLE.value());
        assertNotNull(preferenceStatus);
        assertEquals(PreferenceStatus.ENABLE, preferenceStatus);
    }

    @Test
    public void convertPreferenceStatusDisableTest() {
        PreferenceStatus preferenceStatus = stringToPreferenceStatusConverter.convert(PreferenceStatus.DISABLE.value());
        assertNotNull(preferenceStatus);
        assertEquals(PreferenceStatus.DISABLE, preferenceStatus);
    }
}
