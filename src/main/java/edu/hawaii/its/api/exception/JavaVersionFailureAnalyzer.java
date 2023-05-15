package edu.hawaii.its.api.exception;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.stereotype.Component;

@Component
public class JavaVersionFailureAnalyzer extends AbstractFailureAnalyzer<JavaVersionException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, JavaVersionException cause) {
        String javaVersion = System.getProperty("java.version");
        String description = String.format("You are currently running Java %s.", javaVersion);
        String action =
                String.format("Please make sure you are using Java %s to run this program.", cause.getMessage());
        return new FailureAnalysis(description, action, cause);
    }
}