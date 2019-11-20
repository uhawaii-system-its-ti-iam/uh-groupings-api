package edu.hawaii.its.api.service;

import com.opencsv.CSVWriter;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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

  GroupingsMailService(JavaMailSender javaMailSender, List<List<GroupingsServiceResult>> res) {
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
  void sendSimpleMessage(String addr, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();

    message.setTo(addr);
    message.setSubject(subject);
    message.setText(text);

    this.javaMailSender.send(message);
  }

  void sendAttachmentMessage(String addr, String subject, String text, String path) throws MessagingException, IOException {
    File f = new File(path);
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(addr);
      helper.setSubject(subject);
      helper.setText(text);


      FileSystemResource file = this.toCsv(this.toCsvObj(this.res), f);

      helper.addAttachment(f.getPath(), file);

      javaMailSender.send(message);
      f.delete();

    } catch (MessagingException me) {
      throw new MessagingException();
    } catch (IOException ioe) {
      throw new IOException(ioe);
    }
  }

  /**
   * Build a CSV object using a List of String arrays.
   * Each index of the list represents a row in the CSV file.
   * Each String in a row represents a field or cell in the CSV file.
   *
   * @param res Response data
   * @return List of string Arrays
   */
  private List<String[]> toCsvObj(List<List<GroupingsServiceResult>> res) {
    List<String[]> lines = new ArrayList<>();

    lines.add(new String[]{"username", "uuid", "firstName", "lastName", "name"});

    for (List<GroupingsServiceResult> li : res) {
      for (GroupingsServiceResult item : li) {
        lines.add(item.getPerson().toCsv());
      }
    }
    return lines;
  }

  /**
   * Write Csv data to a file.
   *
   * @param data Csv data returned from toCsvObj()
   * @param file Descriptor ro be written too.
   * @return FileSystemResource to be sent via SMTP.
   * @throws IOException
   */
  private FileSystemResource toCsv(List<String[]> data, File file) throws IOException {
    try {
      FileWriter out = new FileWriter(file);
      CSVWriter writer = new CSVWriter(out);

      writer.writeAll(data);
      writer.close();

    } catch (FileNotFoundException e) {
      throw new IOException(e);
    }

    return new FileSystemResource(file);
  }

  String getOwnerAddress(String ownerUsername) {
    return ownerUsername + "@hawaii.edu";
  }
}

