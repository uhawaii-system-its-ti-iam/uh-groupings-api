package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.internet.MimeMessage;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Feedback;

import static org.mockito.Mockito.doReturn;

/**
 * Integration test verifying EmailService sends real SMTP messages
 * end-to-end through the JavaMailSender bean (no mocking of
 * JavaMailSender itself), using GreenMail as a fake SMTP server.
 *
 * This complements EmailServiceTest.java, which mocks JavaMailSender
 * to test business logic in isolation. This test instead confirms the
 * real send path (config -> JavaMailSenderImpl -> SMTP -> delivery)
 * works.
 */
@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestEmailService {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private SubjectService subjectService;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    private Feedback feedback;

    @BeforeEach
    public void setUp() {
        greenMail.reset();

        doReturn(true).when(subjectService).isValidIdentifier(TEST_UIDS.get(0), TEST_UIDS.get(0));

        emailService.setEnabled(true);
        emailService.setRecipient("test@example.com");

        feedback = new Feedback();
        feedback.setName("Testf-iwt-a TestIAM-staff");
        feedback.setEmail("testiwta@hawaii.edu");
        feedback.setType("problem");
        feedback.setMessage("Some problem happened.");
        feedback.setExceptionMessage("");
    }

    @Test
    public void feedbackEmailTransmitsOverSmtp() throws Exception {
        emailService.sendFeedback(TEST_UIDS.get(0), feedback);

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received.length, equalTo(1));

        String subject = received[0].getSubject();
        String body = GreenMailUtil.getBody(received[0]);

        assertThat(subject, containsString("problem"));
        assertThat(body, containsString("Some problem happened."));
    }

    @Test
    public void stackTraceEmailTransmitsOverSmtp() {
        emailService.sendStackTrace(TEST_UIDS.get(0), "stackTrace");

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received.length, equalTo(1));

        String body = GreenMailUtil.getBody(received[0]);
        assertThat(body, containsString("stackTrace"));
    }

    @Test
    public void sendWithStackOverSMTP() {
        emailService.sendWithStack(new NullPointerException("test"), "Null Pointer Exception", "/path/to/test");

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received.length, equalTo(1));

        String body = GreenMailUtil.getBody(received[0]);
        assertThat(body, containsString("Null Pointer Exception"));
    }
}