package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PasswordFoundAnalyzerTest {

    @Test
    public void analyze() {
        PasswordFoundAnalyzer passwordFoundAnalyzer = new PasswordFoundAnalyzer();
        Throwable throwable = new Throwable();
        PasswordFoundException exception = new PasswordFoundException("");
        assertNotNull(passwordFoundAnalyzer.analyze(throwable, exception));
    }

}
