package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.Feedback;
import edu.hawaii.its.api.type.RetireGroupingResult;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class EmailServiceTest {

    private static boolean wasSent;

    private static SimpleMailMessage messageSent;

    private static List<SimpleMailMessage> messagesSent;

    private static Feedback feedback;

    @MockitoSpyBean
    private EmailService emailService;

    @MockitoSpyBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private SubjectService subjectService;

    @Value("${app.environment}")
    private String environment;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.path:/api/groupings/v2.1}")
    private String testPath;

    @BeforeEach
    public void setUp() {
        doReturn(true).when(subjectService).isValidIdentifier(TEST_UIDS.get(0), TEST_UIDS.get(0));

        doAnswer(invocation -> {
            SimpleMailMessage mailMessage = invocation.getArgument(0);
            wasSent = true;
            messageSent = mailMessage;
            messagesSent.add(mailMessage);
            return null;
        }).when(javaMailSender).send(any(SimpleMailMessage.class));
        emailService.setEnabled(true);
        emailService.setRecipient("address");
        emailService.setEnvironment(environment);


        wasSent = false;
        messagesSent = new ArrayList<>();
        

        feedback = new Feedback();
        feedback.setName("Testf-iwt-a TestIAM-staff");
        feedback.setEmail("testiwta@hawaii.edu");
        feedback.setType("problem");
        feedback.setMessage("Some problem happened.");
        feedback.setExceptionMessage("");

    }

    @Test
    public void validUhIdentifier() {
        doReturn(false).when(subjectService).isValidIdentifier(TEST_UIDS.get(0), TEST_UIDS.get(0));

        assertThrows(AccessDeniedException.class, () -> emailService.sendFeedback(TEST_UIDS.get(0), feedback));
        assertThrows(AccessDeniedException.class, () -> emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace"));
    }

    @Test
    public void disabled() {
        emailService.setEnabled(false);
        assertFalse(emailService.isEnabled());
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
        assertFalse(wasSent);
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");
        assertFalse(wasSent);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertFalse(wasSent);
        RetireGroupingResult result = emailService.sendRetireGroupingEmails("path:to:grouping", "owner@hawaii.edu",
                "grouping", "description", List.of("other-owner@hawaii.edu"), "Owner Name");
        assertFalse(wasSent);
        assertEquals("FAILURE", result.getResultCode());
        assertEquals("Email service is not enabled.", result.getResultMessage());
        assertEquals(List.of("other-owner@hawaii.edu"), result.getOwnerRecipients());
    }

    @Test
    public void overrideRecipient() {
        emailService.setRecipient("its-iam-web-app-dev-help-l@lists.hawaii.edu");
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
        assertFalse(messageSent.getText().contains("Recipient overridden"));
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");
        assertFalse(messageSent.getText().contains("Recipient overridden"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertFalse(messageSent.getText().contains("Recipient overridden"));

        emailService.setRecipient("override@email.com");
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
        assertTrue(messageSent.getText().contains("Recipient overridden"));
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");
        assertTrue(messageSent.getText().contains("Recipient overridden"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertTrue(messageSent.getText().contains("Recipient overridden"));
    }

    @Test
    public void sendFeedbackWithNoExceptionMessage() {
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
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
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
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
        doReturn(true).when(subjectService).isValidIdentifier(TEST_UIDS.get(0), TEST_UIDS.get(0));

        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
        assertFalse(wasSent);
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");
        assertFalse(wasSent);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertFalse(wasSent);
    }

    @Test
    public void unknownHost() throws UnknownHostException {
        doThrow(UnknownHostException.class).when(emailService).getLocalHost();

        emailService.sendFeedback(TEST_UIDS.get(0), feedback);
        assertTrue(messageSent.getText().contains("Unknown Host"));
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");
        assertTrue(messageSent.getText().contains("Unknown Host"));
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertTrue(messageSent.getText().contains("Unknown Host"));
    }

    @Test
    public void environmentInSubject() {
        String environment = emailService.getEnvironment();
        assertEquals("dev", environment);
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception", testPath);
        assertTrue(messageSent.getSubject().contains("(dev)"));
    }

    @Test
    public void sendRetireGroupingEmails() {
        RetireGroupingResult result = emailService.sendRetireGroupingEmails(
                "hawaii.edu:custom:test:listserv-tests:JTTEST-L",
                "requestor@hawaii.edu",
                "JTTEST-L",
                "Changing description test",
                List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"),
                "Requestor Name");

        assertEquals(2, messagesSent.size());
        assertEquals("SUCCESS", result.getResultCode());
        assertEquals("Retirement request emails were sent.", result.getResultMessage());
        assertEquals(List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"), result.getOwnerRecipients());

        SimpleMailMessage iamMessage = messagesSent.get(0);
        assertTrue(Arrays.asList(iamMessage.getTo()).contains("iam-team-test@hawaii.edu"));
        assertEquals("[groupings] Owner request to retire JTTEST-L", iamMessage.getSubject());
        assertTrue(iamMessage.getText().contains("Grouping to retire: hawaii.edu:custom:test:listserv-tests:JTTEST-L"));
        assertTrue(iamMessage.getText().contains("Requesting by owner: requestor@hawaii.edu"));
        assertTrue(iamMessage.getText().contains("LISTSERV lists or Google groups"));
        assertTrue(iamMessage.getText().contains("An owners notification will be sent"));

        SimpleMailMessage ownersMessage = messagesSent.get(1);
        assertEquals(List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"),
                Arrays.asList(ownersMessage.getTo()));
        assertEquals("Request sent to IAM to retire grouping JTTEST-L", ownersMessage.getSubject());
        assertTrue(ownersMessage.getText().contains("request from Requestor Name"));
        assertTrue(ownersMessage.getText().contains("  o Name: JTTEST-L - Changing description test"));
        assertTrue(ownersMessage.getText().contains("  o Path: hawaii.edu:custom:test:listserv-tests:JTTEST-L"));
        assertTrue(ownersMessage.getText().contains("<its-iam-help@lists.hawaii.edu>"));
    }

    @Test
    public void sendRetireGroupingEmailsOwnerFailure() {
        doAnswer(invocation -> {
            SimpleMailMessage mailMessage = invocation.getArgument(0);
            if (mailMessage.getTo().length > 1) {
                throw new MailSendException("Owners notification rejected");
            }
            messagesSent.add(mailMessage);
            return null;
        }).when(javaMailSender).send(any(SimpleMailMessage.class));

        RetireGroupingResult result = emailService.sendRetireGroupingEmails(
                "hawaii.edu:custom:test:listserv-tests:JTTEST-L",
                "requestor@hawaii.edu",
                "JTTEST-L",
                "Changing description test",
                List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"),
                "Requestor Name");

        assertEquals("FAILURE", result.getResultCode());
        assertEquals("Failed to send the grouping owners notification email.", result.getResultMessage());
        assertEquals(List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"), result.getOwnerRecipients());
        assertEquals(1, messagesSent.size());
    }

    @Test
    public void sendRetireGroupingEmailsIamFailureDoesNotNotifyOwners() {
        doThrow(new MailSendException("IAM recipient rejected"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        RetireGroupingResult result = emailService.sendRetireGroupingEmails(
                "hawaii.edu:custom:test:listserv-tests:JTTEST-L",
                "requestor@hawaii.edu",
                "JTTEST-L",
                "Changing description test",
                List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"),
                "Requestor Name");

        assertEquals("FAILURE", result.getResultCode());
        assertEquals("Failed to send the IAM retirement request email.", result.getResultMessage());
        assertEquals(List.of("owner-one@hawaii.edu", "owner-two@hawaii.edu"), result.getOwnerRecipients());
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
