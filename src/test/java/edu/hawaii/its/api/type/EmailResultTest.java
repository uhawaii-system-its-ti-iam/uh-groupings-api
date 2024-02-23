package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

class EmailResultTest {

    private SimpleMailMessage msg;

    @BeforeEach
    public void setup() {
        msg = new SimpleMailMessage();
        msg.setTo("to");
        msg.setFrom("from");
        msg.setSubject("subject");
        msg.setText("text");
    }

    @Test
    public void constructors() {
        EmailResult emailResult = new EmailResult();
        assertNotNull(emailResult);
        assertEquals("FAILURE", emailResult.getResultCode());
        assertEquals("", emailResult.getRecipient());
        assertEquals("", emailResult.getFrom());
        assertEquals("", emailResult.getSubject());
        assertEquals("", emailResult.getText());

        emailResult = new EmailResult(msg);
        assertNotNull(emailResult);
        assertEquals("SUCCESS", emailResult.getResultCode());
        assertEquals("to", emailResult.getRecipient());
        assertEquals("from", emailResult.getFrom());
        assertEquals("subject", emailResult.getSubject());
        assertEquals("text", emailResult.getText());
    }
}
