package edu.hawaii.its.api.access;

public interface UserContextService {
    User getCurrentUser();

    String getCurrentUsername();

    String getCurrentUhuuid();

    void setCurrentUhuuid(String uhuuid);
}