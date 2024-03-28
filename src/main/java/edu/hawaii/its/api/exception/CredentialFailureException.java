package edu.hawaii.its.api.exception;
import javax.security.auth.login.CredentialException;
public class CredentialFailureException extends CredentialException {
    public CredentialFailureException(String message) {
        super(message);
    }
}
