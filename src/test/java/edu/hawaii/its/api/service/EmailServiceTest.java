package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
public class EmailServiceTest {

    private static boolean wasSent;

    private static SimpleMailMessage messageSent;

    public EmailService emailService;

    public EmailService mockEmailService;

    @BeforeEach
    public void setUp() {
        JavaMailSender sender = new MockJavaMailSender() {
            @Override
            public void send(SimpleMailMessage mailMessage) throws MailException {
                wasSent = true;
                messageSent = mailMessage;
            }
        };

        emailService = new EmailService(sender);
        emailService.setEnabled(true);
        emailService.setRecipient("address");
        mockEmailService = spy(new EmailService(sender));

        wasSent = false;
    }


    @Test
    public void enabled() {
        emailService.setEnabled(false);
        assertFalse(emailService.isEnabled());
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(wasSent);

        emailService.setEnabled(true);
        assertTrue(emailService.isEnabled());
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getText().contains("NullPointerException"));
        assertTrue(wasSent);

    }

    @Test
    public void overrideRecipient() {
        emailService.setRecipient("its-iam-web-app-dev-help-l@lists.hawaii.edu");
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(messageSent.getText().contains("Recipient overridden"));

        emailService.setEnabled(true);
        emailService.setRecipient("override@email.com");
        emailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getText().contains("Recipient overridden"));
    }

    @Test
    public void sendFeedbackWithMailExceptionThrown() {
        JavaMailSender senderWithException = new MockJavaMailSender() {
            @Override
            public void send(SimpleMailMessage mailMessage) throws MailException {
                wasSent = false;
                throw new MailSendException("Exception");
            }
        };

        EmailService emailServiceWithException = new EmailService(senderWithException);
        emailServiceWithException.setEnabled(true);
        emailServiceWithException.setRecipient("override@email");
        emailServiceWithException.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertFalse(wasSent);
    }
    @Test
    public void unknownHost() throws UnknownHostException {
        mockEmailService.setEnabled(true);
        mockEmailService.setRecipient("address");
        doThrow(UnknownHostException.class).when(mockEmailService).getLocalHost();

        mockEmailService.sendWithStack(new NullPointerException(), "Null Pointer Exception");
        assertTrue(messageSent.getText().contains("Unknown Host"));
    }

}
