package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface GroupingsMailService {
    void setJavaMailSender(JavaMailSender javaMailSender);

    void sendSimpleMessage(String address, String subject, String text);

    void sendAttachmentMessage(String address, String subject, String text, String path,
            List<GroupingsServiceResult> res)
            throws MessagingException, IOException;
}
