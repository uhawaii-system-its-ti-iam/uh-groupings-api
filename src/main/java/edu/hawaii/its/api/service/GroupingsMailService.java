package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

/**
 * Create and send SMTP messages using the JavaMailSender.
 */
public interface GroupingsMailService {
    GroupingsMailService setJavaMailSender(JavaMailSender javaMailSender);
    GroupingsMailService setFrom(String from);


    /**
     * Send a SMTP message with no attachment
     */
    void sendSimpleMessage(String from, String to, String subject, String text);

    /**
     * Send an SMTP message with a CSV file attachment.
     */
    void sendCSVMessage(String from, String to, String subject, String text, String path,
            List<GroupingsServiceResult> res);

    /**
     * Concat UH email suffix onto username
     */
    String getUserEmail(String username);
}
