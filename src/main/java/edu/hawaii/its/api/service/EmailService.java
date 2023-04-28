package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class EmailService {

    @Value("#{'${email.send.recipient.override:}' == '' ? '${email.send.recipient}' : '${email.send.recipient.override:}'}")
    private String recipient;

    @Value("${email.send.from}")
    private String from;

    @Value("${email.is.enabled}")
    private boolean isEnabled;

    private static final Log logger = LogFactory.getLog(EmailService.class);

    private JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendWithStack(Exception e, String exceptionType) {
        logger.info("Feedback Error email has been triggered.");

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

        if (isEnabled) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(recipient);
            msg.setFrom(from);
            String text = "";
            String header = "UH Groupings API Error Response";
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

    public InetAddress getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

}
