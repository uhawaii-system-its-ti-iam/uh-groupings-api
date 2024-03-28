package edu.hawaii.its.api.exception;

import org.junit.jupiter.api.Test;

import javax.security.auth.login.CredentialException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CredentialFailureAnalyzerTest {

    @Test
    public void analyze() {
        CredentialFailureAnalyzer credentialFailureAnalyzer = new CredentialFailureAnalyzer();
        Throwable throwable = new Throwable();
        CredentialFailureException exception = new CredentialFailureException("");
        assertNotNull(credentialFailureAnalyzer.analyze(throwable, exception));
    }

}
