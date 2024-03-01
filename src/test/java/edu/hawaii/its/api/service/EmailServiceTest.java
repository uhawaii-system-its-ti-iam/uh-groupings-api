package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.net.UnknownHostException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.Feedback;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class EmailServiceTest {

    private static boolean wasSent;

    private static SimpleMailMessage messageSent;

    private static Feedback feedback;

    @SpyBean
    private EmailService emailService;

    @SpyBean
    private JavaMailSender javaMailSender;

    @MockBean
    private SubjectService subjectService;

    @Value("${app.environment}")
    private String environment;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;

    @TestConfiguration
    public static class EmailServiceTestConfiguration {
        @Bean
        public JavaMailSender javaMailSender() {
            return new MockJavaMailSender() {
                @Override
                public void send(SimpleMailMessage mailMessage) throws MailException {
                    wasSent = true;
                    messageSent = mailMessage;
                }
            };
        }
    }

    @BeforeEach
    public void setUp() {
        doReturn(true).when(subjectService).isValidIdentifier(TEST_USERNAMES.get(0));

        emailService.setEnabled(true);
        emailService.setRecipient("address");
        emailService.setEnvironment(environment);

        wasSent = false;

        feedback = new Feedback();
        feedback.setName("Testf-iwt-a TestIAM-staff");
        feedback.setEmail("testiwta@hawaii.edu");
        feedback.setType("problem");
        feedback.setMessage("Some problem happened.");
        feedback.setExceptionMessage("");
    }

    @Test
    public void validUhIdentifier() {
        doReturn(false).when(subjectService).isValidIdentifier(TEST_USERNAMES.get(0));

        assertThrows(AccessDeniedException.class, () -> emailService.sendFeedback(TEST_USERNAMES.get(0), feedback));
        assertThrows(AccessDeniedException.class, () -> emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace"));
    }

    @Test
    public void disabled() {
        emailService.setEnabled(false);
        assertFalse(emailService.isEnabled());
        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertFalse(wasSent);
        emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace");
        assertFalse(wasSent);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(wasSent);
    }

    @Test
    public void overrideRecipient() {
        emailService.setRecipient("its-iam-web-app-dev-help-l@lists.hawaii.edu");
        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertFalse(messageSent.getText().contains("Recipient overridden"));
        emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace");
        assertFalse(messageSent.getText().contains("Recipient overridden"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(messageSent.getText().contains("Recipient overridden"));

        emailService.setRecipient("override@email.com");
        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertTrue(messageSent.getText().contains("Recipient overridden"));
        emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace");
        assertTrue(messageSent.getText().contains("Recipient overridden"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getText().contains("Recipient overridden"));
    }

    @Test
    public void sendFeedbackWithNoExceptionMessage() {
        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertTrue(wasSent);

        assertTrue(messageSent.getSubject().contains("problem"));
        assertTrue(messageSent.getText().contains("Testf-iwt-a TestIAM-staff"));
        assertTrue(messageSent.getText().contains("testiwta@hawaii.edu"));
        assertTrue(messageSent.getText().contains("Some problem happened."));
        assertFalse(messageSent.getText().contains("Stack Trace:"));
    }

    @Test
    public void sendFeedbackWithExceptionMessage() {
        feedback.setExceptionMessage("ArrayIndexOutOfBoundsException");
        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertTrue(wasSent);

        assertTrue(messageSent.getSubject().contains("problem"));
        assertTrue(messageSent.getText().contains("Testf-iwt-a TestIAM-staff"));
        assertTrue(messageSent.getText().contains("testiwta@hawaii.edu"));
        assertTrue(messageSent.getText().contains("Some problem happened."));
        assertTrue(messageSent.getText().contains("Stack Trace:"));
        assertTrue(messageSent.getText().contains("ArrayIndexOutOfBoundsException"));
    }

    @Test
    public void sendFeedbackWithMailExceptionThrown() {
        doThrow(MailSendException.class).when(javaMailSender).send((SimpleMailMessage) any());
        doReturn(true).when(subjectService).isValidIdentifier(TEST_USERNAMES.get(0));

        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertFalse(wasSent);
        emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace");
        assertFalse(wasSent);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(wasSent);
    }

    @Test
    public void unknownHost() throws UnknownHostException {
        doThrow(UnknownHostException.class).when(emailService).getLocalHost();

        emailService.sendFeedback(TEST_USERNAMES.get(0), feedback);
        assertTrue(messageSent.getText().contains("Unknown Host"));
        emailService.sendStackTrace(TEST_USERNAMES.get(0), "stackTrace");
        assertTrue(messageSent.getText().contains("Unknown Host"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getText().contains("Unknown Host"));
    }

    @Test
    public void environmentInSubject() {
        String environment = emailService.getEnvironment();
        assertEquals("dev", environment);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getSubject().contains("(dev)"));
    }
}
