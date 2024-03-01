package edu.hawaii.its.api.type;

import java.util.Objects;

import org.springframework.mail.SimpleMailMessage;

public class EmailResult {
    private String resultCode;
    private String recipient;
    private String from;
    private String subject;
    private String text;

    public EmailResult() {
        this.resultCode = "FAILURE";
        this.recipient = "";
        this.from = "";
        this.subject = "";
        this.text = "";
    }

    public EmailResult(SimpleMailMessage msg) {
        this.resultCode = "SUCCESS";
        this.recipient = Objects.requireNonNull(msg.getTo())[0];
        this.from = msg.getFrom();
        this.subject = msg.getSubject();
        this.text = msg.getText();
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
}
