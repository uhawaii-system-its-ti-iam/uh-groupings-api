package edu.hawaii.its.api.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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

    @Value("${email.send.default-recipient}")
    private String defaultRecipient;

    @Value("${email.retirement.iam-team-recipient}")
    private String iamTeamRecipient;

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

        if (!subjectService.isValidIdentifier(currentUser, currentUser)) {
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
        if (!recipient.equals(defaultRecipient)) {
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

        if (!subjectService.isValidIdentifier(currentUser, currentUser)) {
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
        if (!recipient.equals(defaultRecipient)) {
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

    public void sendWithStack(Exception e, String exceptionType, String path) {
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
        text += "Endpoint Path: " + path + "\n";
        if (!recipient.equals(defaultRecipient)) {
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

    public void sendRetireGroupingEmails(String groupingPath, String requestorUid, String requestorEmail,
            String groupingName, String description, List<String> ownerEmails, String requestorName) {
        logger.info("Starting retire grouping email notifications for: " + groupingPath);

        if (!isEnabled) {
            logger.warn("Email service is not enabled. Skipping email notifications.");
            return;
        }

        String iamSubject = "[groupings] Owner request to retire " + groupingName;
        String iamBody = buildIamTeamEmailBody(groupingPath, requestorEmail);
        sendEmail(iamTeamRecipient, iamSubject, iamBody);

        if (ownerEmails.isEmpty()) {
            logger.warn("No owner emails found for grouping: " + groupingPath);
            return;
        }

        String ownersSubject = "Request sent to IAM to retire grouping " + groupingName;
        String ownersBody = buildOwnersEmailBody(groupingName, description, groupingPath, requestorName);
        sendEmail(ownerEmails.toArray(new String[0]), ownersSubject, ownersBody);
    }

    private void sendEmail(String recipient, String subject, String body) {
        sendEmail(new String[] { recipient }, subject, body);
    }

    private void sendEmail(String[] recipients, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipients);
        msg.setFrom(from);
        msg.setText(body);
        msg.setSubject(subject);

        try {
            javaMailSender.send(msg);
            logger.info("Email sent successfully with subject: " + subject);
        } catch (MailException ex) {
            logger.error("Error sending email with subject: " + subject, ex);
        }
    }

    private String buildIamTeamEmailBody(String groupingPath, String requestorEmail) {
        StringBuilder body = new StringBuilder();
        body.append("Grouping to retire: ").append(groupingPath).append("\n\n");
        body.append("Requesting by owner: ").append(requestorEmail).append("\n\n");
        body.append("The IAM team will follow up with the requestor in order to determine the disposition ")
                .append("of any sync destinations such as LISTSERV lists or Google groups before the grouping is ")
                .append("retired.\n\n");
        body.append("The requestor and the grouping owners have received an email notification that this request has ")
                .append("been made.");
        return body.toString();
    }

    private String buildOwnersEmailBody(String groupingName, String description, String groupingPath,
            String requestorName) {
        StringBuilder body = new StringBuilder();
        body.append("This is an automated notification to inform you that the IAM team has received a request from ")
                .append(requestorName)
                .append(" to retire the following grouping:\n\n");
        body.append("  o Name: ").append(groupingName).append(" - ").append(description).append("\n");
        body.append("  o Path: ").append(groupingPath).append("\n\n");
        body.append("The IAM team will follow up with the requestor in order to determine the disposition ")
                .append("of any sync destinations such as LISTSERV lists or Google groups before the grouping is ")
                .append("retired.\n\n");
        body.append("If you have any questions please contact the requestor, or the IAM team at ")
                .append("<its-iam-help@lists.hawaii.edu>.");
        return body.toString();
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
