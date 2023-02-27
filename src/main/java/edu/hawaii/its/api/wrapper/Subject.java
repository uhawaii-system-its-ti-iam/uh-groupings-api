package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class Subject extends Results {

    private final WsSubject wsSubject;

    public Subject(WsSubject wsSubject) {
        if (wsSubject == null) {
            this.wsSubject = new WsSubject();
        } else {
            this.wsSubject = wsSubject;
        }
    }

    public Subject() {
        this.wsSubject = new WsSubject();
    }

    public String getUhUuid() {
        String uhUuid = wsSubject.getId();
        return uhUuid != null ? uhUuid : "";
    }

    public String getUid() {
        if (wsSubject.getIdentifierLookup() != null) {
            return wsSubject.getIdentifierLookup();
        }
        return getAttributeValue(0);
    }

    public String getName() {
        String name = wsSubject.getName();
        return name != null ? name : "";
    }

    public String getFirstName() {
        return getAttributeValue(3);
    }

    public String getLastName() {
        return getAttributeValue(2);
    }

    private String getAttributeValue(int index) {
        String[] attributeValues = wsSubject.getAttributeValues();
        if (isEmpty(attributeValues)) {
            return "";
        }
        String attributeValue = wsSubject.getAttributeValue(index);
        return attributeValue != null ? attributeValue : "";
    }

    @Override
    public String getResultCode() {
        String resultCode = wsSubject.getResultCode();
        return resultCode != null ? resultCode : "";
    }
}
