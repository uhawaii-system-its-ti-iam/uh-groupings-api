package edu.hawaii.its.api.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import edu.hawaii.its.api.type.GroupingsServiceResult;

import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

/**
 * Create and send SMTP messages using the JavaMailSender.
 */
public interface GroupingsMailService {
    void setJavaMailSender(JavaMailSender javaMailSender);

    /**
     * Send a SMTP message with no attachment
     */
    void sendSimpleMessage(String from, String to, String subject, String text);

    /**
     * Send an SMTP message with a CSV file attachment.
     */
    @Ignore
    void sendCSVMessage(String from, String to, String subject, String text, String path,
            List<GroupingsServiceResult> res);

    /**
     * Concat UH email suffix onto username
     */
    String getUserEmail(String username);
}
