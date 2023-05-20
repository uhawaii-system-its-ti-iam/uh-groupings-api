package edu.hawaii.its.api.configuration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AppConfigTest {

    @Test
    public void construction() {
        AppConfig appConfig = new AppConfig();
        assertNotNull(appConfig);
    }

}
