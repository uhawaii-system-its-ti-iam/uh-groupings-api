package edu.hawaii.its.api.service;

import com.opencsv.CSVWriter;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilize JavaMailSender class
 */
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

  public void sendAttachmentMessage(String addr, String subject, String text, String path) throws MessagingException, IOException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    File f = new File("CSV_TEST.csv");

    helper.setTo(addr);
    helper.setSubject(subject);
    helper.setText(text);


    FileSystemResource file = this.toCsv(this.toCsvString(this.res), f);

    helper.addAttachment(f.getPath(), file);

    javaMailSender.send(message);
    f.delete();
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

  private List<String[]> toCsvString(List<List<GroupingsServiceResult>> res) {
    List<String[]> lines = new ArrayList<>();

    lines.add(new String[]{"username", "uuid", "firstName", "lastName", "name"});

    for (List<GroupingsServiceResult> li : res) {
      for (GroupingsServiceResult item : li) {
        lines.add(item.getPerson().toCsv());
      }
    }
    return lines;
  }

  private FileSystemResource toCsv(List<String[]> data, File file) throws IOException {
    FileWriter out = new FileWriter(file);
    CSVWriter writer = new CSVWriter(out);

    writer.writeAll(data);
    writer.close();

    return new FileSystemResource(file);
  }
}

