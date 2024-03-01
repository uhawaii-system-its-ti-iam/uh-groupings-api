package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FeedbackTest {

    @Test
    public void accessors() {
        Feedback feedback = new Feedback();
        feedback.setName("name");
        feedback.setEmail("email");
        feedback.setType("type");
        feedback.setMessage("message");
        assertNotNull(feedback.toString());

        assertEquals("name", feedback.getName());
        assertEquals("email", feedback.getEmail());
        assertEquals("type", feedback.getType());
        assertEquals("message", feedback.getMessage());
        assertEquals("", feedback.getExceptionMessage());

        feedback.setExceptionMessage("exceptionMessage");
        assertEquals("exceptionMessage", feedback.getExceptionMessage());
    }
}
