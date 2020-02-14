package edu.hawaii.its.api.service;

import com.opencsv.CSVWriter;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("groupingsMailService")
public class GroupingsMailServiceImpl implements GroupingsMailService {
    @Autowired
    JavaMailSender javaMailSender;

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override public void sendSimpleMessage(String address, String subject, String text) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(address);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        this.javaMailSender.send(simpleMailMessage);

    }

    @Override public void sendAttachmentMessage(String address, String subject, String text, String path,
            List<GroupingsServiceResult> res)
            throws MessagingException, IOException {
        File file = new File(path);

        try {
            MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setTo(address);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text);

            FileSystemResource fileSystemResource = this.toCsv(this.toCsvObj(res), file);
            mimeMessageHelper.addAttachment(file.getPath(), fileSystemResource);
            javaMailSender.send(mimeMessage);
            file.delete();

        } catch (MessagingException me) {
            me.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
    private List<String[]> toCsvObj(List<GroupingsServiceResult> res) {
        List<String[]> lines = new ArrayList<>();

        lines.add(new String[] { "username", "uuid", "firstName", "lastName", "name" });

        for (GroupingsServiceResult item : res) {
            lines.add(item.getPerson().toCsv());
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
}
