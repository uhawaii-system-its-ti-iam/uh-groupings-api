package edu.hawaii.its.api.exception;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.stereotype.Component;

@Component
public class CredentialFailureAnalyzer extends AbstractFailureAnalyzer<CredentialFailureException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, CredentialFailureException cause) {
        String description = "Possible credential error.";
        String action = "Please check the overrides file: "
                + cause.getMessage();
        return new FailureAnalysis(description, action, cause);
    }
}