package edu.hawaii.its.api.wrapper;

import java.util.Arrays;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 * A wrapper for WsSubject.
 */
public class Subject extends Results {

    private final WsSubject wsSubject;

    public Subject(WsSubject wsSubject) {
        if (wsSubject == null) {
            this.wsSubject = new WsSubject();
        } else {
            this.wsSubject = wsSubject;
        }
    }

    public Subject(String uid, String name, String uhUuid) {
        this.wsSubject = new WsSubject();
        this.wsSubject.setIdentifierLookup(uid);
        this.wsSubject.setName(name);
        this.wsSubject.setId(uhUuid);
    }

    public Subject() {
        this.wsSubject = new WsSubject();
    }

    public String getUhUuid() {
        String uhUuid = wsSubject.getId();
        return uhUuid != null ? uhUuid : "";
    }

    public void setUhUuid(String uhUuid) {
        wsSubject.setId(uhUuid);
    }

    public String getUid() {
        if (wsSubject.getIdentifierLookup() != null) {
            return wsSubject.getIdentifierLookup();
        }
        return getAttributeValue(0);
    }

    public void setUid(String uid) {
        wsSubject.setIdentifierLookup(uid);
    }

    public String getName() {
        String name = wsSubject.getName();
        return name != null ? name : "";
    }

    public void setName(String name) {
        wsSubject.setName(name);
    }

    public String getFirstName() {
        return getAttributeValue(3);
    }

    public void setFirstName(String firstName) {
        String[] attributeValues = wsSubject.getAttributeValues();
        if (attributeValues == null) {
            attributeValues = new String[5];
        }
        attributeValues[3] = firstName;
        wsSubject.setAttributeValues(attributeValues);
    }

    public String getLastName() {
        return getAttributeValue(2);
    }

    public void setLastName(String lastName) {
        String[] attributeValues = wsSubject.getAttributeValues();
        if (attributeValues == null) {
            attributeValues = new String[5];
        }
        attributeValues[2] = lastName;
        wsSubject.setAttributeValues(attributeValues);
    }

    public String getAttributeValue(int index) {
        String[] attributeValues = wsSubject.getAttributeValues();
        if (isEmpty(attributeValues)) {
            return "";
        }
        String attributeValue = wsSubject.getAttributeValue(index);
        return attributeValue != null ? attributeValue : "";
    }

    public void setAttributeValue(int index, String value) {
        String[] attributeValues = wsSubject.getAttributeValues();
        if (attributeValues == null) {
            attributeValues = new String[5];
        }
        attributeValues[index] = value;
        wsSubject.setAttributeValues(attributeValues);
    }

    @Override
    public String getResultCode() {
        String resultCode = wsSubject.getResultCode();
        return resultCode != null ? resultCode : "";
    }

    public void setResultCode(String resultCode) {
        wsSubject.setResultCode(resultCode);
    }

    public String getSourceId() {
        String sourceId = wsSubject.getSourceId();
        return sourceId != null ? sourceId : "";
    }

    public void setSourceId(String sourceId) {
        wsSubject.setSourceId(sourceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Subject other = (Subject) obj;
        return getName().equals(other.getName()) &&
                getUid().equals(other.getUid()) &&
                getUhUuid().equals(other.getUhUuid());
    }

    @Override
    public String toString() {
        return "Subject [name=" + getName() + ", uhUuid=" + getUhUuid() + ", uid=" + getUid() + "]";
    }

    /**
     * A WsSubject containing empty string for all attribute values returns false.
     */
    public boolean hasUHAttributes() {
        String[] attributeValues = this.wsSubject.getAttributeValues();
        if (attributeValues == null) {
            return false;
        }
        return !Arrays.stream(attributeValues).allMatch(value -> value.equals(""));
    }
}
