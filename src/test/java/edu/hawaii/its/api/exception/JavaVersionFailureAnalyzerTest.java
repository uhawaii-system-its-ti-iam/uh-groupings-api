package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class JavaVersionFailureAnalyzerTest {

    @Test
    public void analyze() {
        JavaVersionFailureAnalyzer javaVersionFailureAnalyzer = new JavaVersionFailureAnalyzer();
        Throwable throwable = new Throwable();
        JavaVersionException exception = new JavaVersionException("");
        assertNotNull(javaVersionFailureAnalyzer.analyze(throwable, exception));
    }

}
