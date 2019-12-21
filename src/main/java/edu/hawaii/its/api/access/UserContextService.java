package edu.hawaii.its.api.access;

public interface UserContextService {
    User getCurrentUser();

    String getCurrentUsername();

    String getCurrentUhUuid();

    void setCurrentUhUuid(String uhUuid);
}
