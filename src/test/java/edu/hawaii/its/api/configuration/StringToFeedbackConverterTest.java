package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.Feedback;
import edu.hawaii.its.api.util.JsonUtil;

public class StringToFeedbackConverterTest {

    StringToFeedbackConverter stringToFeedbackConverter = new StringToFeedbackConverter();

    @Test
    public void convertInvalidFeedbackTest() {
        Feedback feedback = stringToFeedbackConverter.convert("invalid");
        assertNull(feedback);
    }

    @Test
    public void convertFeedbackTest() {
        Feedback expectedFeedback = new Feedback();
        expectedFeedback.setName("name");
        expectedFeedback.setEmail("email");
        expectedFeedback.setType("type");
        expectedFeedback.setMessage("message");
        expectedFeedback.setExceptionMessage("exceptionMessage");

        Feedback feedback = stringToFeedbackConverter.convert(JsonUtil.asJson(expectedFeedback));
        assertNotNull(feedback);
        assertEquals(expectedFeedback.getName(), feedback.getName());
        assertEquals(expectedFeedback.getEmail(), feedback.getEmail());
        assertEquals(expectedFeedback.getType(), feedback.getType());
        assertEquals(expectedFeedback.getMessage(), feedback.getMessage());
        assertEquals(expectedFeedback.getExceptionMessage(), feedback.getExceptionMessage());
    }
}
