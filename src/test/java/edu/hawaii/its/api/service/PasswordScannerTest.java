package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.PasswordFoundException;


@SpringBootTest(classes = { SpringBootWebApplication.class })
public class PasswordScannerTest {

    @Autowired
    private PasswordScanner passwordScanner;

    @Test
    public void construction() {
        Assertions.assertNotNull(passwordScanner);
    }

    @Test
    public void testTwoPatternDiffFile() {
        String dirname = "src/test/resources/pattern-property-checker/test2";
        passwordScanner.setDirname(dirname);
        assertThrows(PasswordFoundException.class,
                () -> passwordScanner.init());
    }

}
