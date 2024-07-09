package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.service.EmailService;
import edu.hawaii.its.api.type.EmailResult;
import edu.hawaii.its.api.type.Feedback;

@RestController
@RequestMapping("/email")
public class EmailRestController {

    private static final Log logger = LogFactory.getLog(EmailRestController.class);

    private final EmailService emailService;

    final private static String CURRENT_USER_KEY = "current_user";

    public EmailRestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping(value = "/send/feedback")
    @ResponseBody
    public ResponseEntity<EmailResult> sendFeedback(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody Feedback feedback) {
        logger.info("Entered REST sendFeedback...");
        return ResponseEntity
                .ok()
                .body(emailService.sendFeedback(currentUser, feedback));
    }

    @PostMapping(value = "/send/stack-trace", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<EmailResult> sendStackTrace(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody String stackTrace) {
        logger.info("Entered REST sendStackTrace...");
        return ResponseEntity
                .ok()
                .body(emailService.sendStackTrace(currentUser, stackTrace));
    }
}
