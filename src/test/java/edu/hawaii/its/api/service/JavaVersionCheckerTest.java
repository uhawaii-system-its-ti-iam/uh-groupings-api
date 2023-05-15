package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.JavaVersionException;

@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "localhost")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class JavaVersionCheckerTest {
    @Autowired
    private JavaVersionChecker javaVersionChecker;

    private String currentJavaVersion = System.getProperty("java.version");

    @Test
    public void construction() {
        Assertions.assertNotNull(javaVersionChecker);
    }

    @Test
    public void testIncorrectVersion() {
        System.setProperty("java.version", "xxx");
        assertThrows(JavaVersionException.class,
                () -> javaVersionChecker.init());
        System.setProperty("java.version", currentJavaVersion);
    }

    @Test
    public void testCorrectVersion() {
        assertDoesNotThrow(() -> javaVersionChecker.init());
    }
}
