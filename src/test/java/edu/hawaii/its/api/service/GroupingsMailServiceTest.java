package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsMailServiceTest {
    @Autowired
    private GroupingsMailService groupingsMailService;

    @Test
    public void construction() {
        assertNotNull(groupingsMailService);
        assertNotNull(groupingsMailService.setJavaMailSender(null));
        assertNotNull(groupingsMailService.setFrom("uid"));
    }

    @Test
    public void getUserEmailTest() {
        String userEmail = "userEmail";
        assertEquals(userEmail + "@hawaii.edu", groupingsMailService.getUserEmail(userEmail));
    }

}
