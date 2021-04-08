package edu.hawaii.its.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsMailServiceTest {
    @Autowired
    private GroupingsMailService groupingsMailService;
    JavaMailSender javaMailSender;

    @Test
    public void construction() {
        assertNotNull(groupingsMailService);
        assertNotNull(groupingsMailService.setJavaMailSender(javaMailSender));
        assertNotNull(groupingsMailService.setFrom("uid"));
    }

    @Test
    public void getUserEmailTest() {
        String userEmail = "userEmail";
        assertEquals(userEmail + "@hawaii.edu", groupingsMailService.getUserEmail(userEmail));
    }

}
