package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;

class GroupingsMailService {
  @Autowired
  private JavaMailSender javaMailSender;
  private List<List<GroupingsServiceResult>> res;

  public GroupingsMailService(JavaMailSender javaMailSender, List<List<GroupingsServiceResult>> res) {
    this.javaMailSender = javaMailSender;
    this.res = res;
  }


  /**
   * Send a simple SMTP message from your machine to addr.
   *
   * @param addr
   * @param subject
   * @param text
   */
  public void sendSimpleMessage(String addr, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();

    message.setTo(addr);
    message.setSubject(subject);
    message.setText(text);

    this.javaMailSender.send(message);
  }

  public void sendAttachmentMessage(String addr, String subject, String text, String path) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);

    helper.setTo(addr);
    helper.setSubject(subject);
    helper.setText(text);

    FileSystemResource file = new FileSystemResource(new File(path));

    helper.addAttachment("jimby.png", file);

    javaMailSender.send(message);
  }

  private String parseGroupingsServiceResult(List<List<GroupingsServiceResult>> res) {
    StringBuilder str = new StringBuilder();
    for (List<GroupingsServiceResult> li : res) {
      for (GroupingsServiceResult item : li) {
        str.append(item.toString());
      }
      str.append("\n");
    }
    return str.toString();
  }
}
