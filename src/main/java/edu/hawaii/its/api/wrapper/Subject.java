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
        String uid = wsSubject.getIdentifierLookup();
        return uid != null ? uid : "";
    }

    public String getName() {
        String name = wsSubject.getName();
        return name != null ? name : "";
    }

    @Override
    public String getResultCode() {
        String resultCode = wsSubject.getResultCode();
        return resultCode != null ? resultCode : "";
    }
}
