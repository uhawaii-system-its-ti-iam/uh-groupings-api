package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* Create and send SMTP messages using the JavaMailSender. */
public interface GroupingsMailService {
    void setJavaMailSender(JavaMailSender javaMailSender);

    /**
     * Send a SMTP message with no attachment
     *
     * @param address - email address to be sent to
     * @param subject - email subject
     * @param text    - email text body
     */
    void sendSimpleMessage(String address, String subject, String text);

    /**
     * Send an SMTP message with a CSV file attachment.
     *
     * @param address - email address to be sent to
     * @param subject - email subject
     * @param text    - email text body
     * @param path    - path or name of the temporary CSV file
     * @param res     - data to be converted to CSV
     */
    void sendCSVMessage(String address, String subject, String text, String path,
            List<GroupingsServiceResult> res);

    String getUserEmail(String username);
}
