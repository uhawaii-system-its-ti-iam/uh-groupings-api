package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.Strings;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@ActiveProfiles("default")
public class MoodHappyServiceTest {

    @Autowired
    private MoodService moodService;

    @Test
    public void state() {
        System.out.println(Strings.fill('h', 88));
        System.out.println(">>>>>> MOOD: " + moodService.state());
        System.out.println(Strings.fill('H', 88));
    }
}
