package edu.hawaii.its.api.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.EmailResult;
import edu.hawaii.its.api.type.Feedback;

@Service
public class EmailService {

    @Value("${email.send.recipient}")
    private String recipient;

    @Value("${email.send.from}")
    private String from;

    @Value("${email.is.enabled}")
    private boolean isEnabled;

    @Value("${app.environment}")
    private String environment;

    private static final Log logger = LogFactory.getLog(EmailService.class);

    private final JavaMailSender javaMailSender;

    private final SubjectService subjectService;

    public EmailService(JavaMailSender javaMailSender, SubjectService subjectService) {
        this.javaMailSender = javaMailSender;
        this.subjectService = subjectService;
    }

    public EmailResult sendFeedback(String currentUser, Feedback feedback) {
        logger.info("Feedback received in EmailService: " + feedback);

        if (!subjectService.isValidIdentifier(currentUser)) {
            throw new AccessDeniedException();
        }

        if (!isEnabled) {
            logger.warn("Email service is not enabled. Set email.is.enabled property to true to enable");
            return new EmailResult();
        }

        String hostname = "Unknown Host";

        try {
            InetAddress ip = this.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException f) {
            logger.error("Error", f);
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipient);
        msg.setFrom(from);
        String text = "";
        String header = "UH Groupings service feedback [" + feedback.getType() + "]";
        text += "Host Name: " + hostname + ".\n";
        if (!recipient.equals("its-iam-web-app-dev-help-l@lists.hawaii.edu")) {
            text += "Recipient overridden to: " + recipient + "\n";
        }
        text += "----------------------------------------------------" + "\n\n";
        text += "Submitted name: " + feedback.getName() + "\n\n";
        text += "Submitted email: <" + feedback.getEmail() + ">\n\n";
        text += "Feedback type: " + feedback.getType() + "\n\n";
        text += "--------------------------" + "\n\n";
        text += "Feedback: " + feedback.getMessage() + "\n\n";
        if (!feedback.getExceptionMessage().isEmpty()) {
            text += "Stack Trace: " + feedback.getExceptionMessage();
        }
        msg.setText(text);
        msg.setSubject(header);
        try {
            javaMailSender.send(msg);
        } catch (MailException ex) {
            logger.error("Error", ex);
        }
        return new EmailResult(msg);
    }

    public EmailResult sendStackTrace(String currentUser, String stackTrace) {
        logger.info("Feedback Error email has been triggered.");

        if (!subjectService.isValidIdentifier(currentUser)) {
            throw new AccessDeniedException();
        }

        if (!isEnabled) {
            logger.warn("Email service is not enabled. Set email.is.enabled property to true");
            return new EmailResult();
        }

        String hostname = "Unknown Host";

        try {
            InetAddress ip = this.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException f) {
            logger.error("Error", f);
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipient);
        msg.setFrom(from);
        String text = "";
        String header =  "(" + environment + ") UH Groupings UI Error Response";
        text += "Cause of Response: The UI threw an exception while making a request to the API. \n\n";
        text += "Host Name: " + hostname + ".\n";
        if (!recipient.equals("its-iam-web-app-dev-help-l@lists.hawaii.edu")) {
            text += "Recipient overridden to: " + recipient + "\n";
        }
        text += "----------------------------------------------------" + "\n\n";
        text += "UI Stack Trace: \n\n" + stackTrace;
        msg.setText(text);
        msg.setSubject(header);
        try {
            javaMailSender.send(msg);
        } catch (MailException ex) {
            logger.error("Error", ex);
        }
        return new EmailResult(msg);
    }

    public void sendWithStack(Exception e, String exceptionType) {
        logger.info("Feedback Error email has been triggered.");
        if (!isEnabled) {
            logger.warn("Email service is not enabled. Set email.is.enabled property to true");
            return;
        }

        InetAddress ip;
        String hostname = "Unknown Host";

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        try {
            ip = this.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException f) {
            logger.error("Error", f);
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipient);
        msg.setFrom(from);
        String text = "";
        String header =  "(" + environment + ") UH Groupings API Error Response";
        text += "Cause of Response: The API threw an exception that has triggered the ErrorControllerAdvice. \n\n";
        text += "Exception Thrown: ErrorControllerAdvice threw the " + exceptionType + ".\n\n";
        text += "Host Name: " + hostname + ".\n";
        if (!recipient.equals("its-iam-web-app-dev-help-l@lists.hawaii.edu")) {
            text += "Recipient overridden to: " + recipient + "\n";
        }
        text += "----------------------------------------------------" + "\n\n";
        text += "API Stack Trace: \n\n" + exceptionAsString;
        msg.setText(text);
        msg.setSubject(header);
        try {
            javaMailSender.send(msg);
        } catch (MailException ex) {
            logger.error("Error", ex);
        }
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setEnvironment(String environment) { this.environment = environment; }

    public String getEnvironment() { return environment; }

    public InetAddress getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

}
