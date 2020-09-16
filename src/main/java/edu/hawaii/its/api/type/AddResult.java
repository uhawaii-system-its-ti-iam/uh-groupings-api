package edu.hawaii.its.api.type;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class AddResult {
    private boolean added;
    private boolean moved;
    private String code;
    private String name;
    private String uid;
    private String uhUuid;

    String identifier;

    public AddResult(String identifier) {
        this.identifier = identifier;
        this.code = "ERROR";
    }

    public AddResult(String identifier, WsAddMemberResult[] addMemberResults,
            WsDeleteMemberResult[] deleteMemberResults) {
        this.identifier = identifier;
        setAddData(addMemberResults, deleteMemberResults);
    }

    private void setAddData(WsAddMemberResult[] addMemberResults, WsDeleteMemberResult[] deleteMemberResults) {

        if (null != addMemberResults[0]) {
            WsSubject addSubject = addMemberResults[0].getWsSubject();
            WsResultMeta addMeta = addMemberResults[0].getResultMetadata();
            this.name = addSubject.getName();
            this.uid = addSubject.getId();
            this.uhUuid = addSubject.getIdentifierLookup();
            this.added = "SUCCESS".equals(addMeta.getResultCode());
            this.code = addMeta.getResultCode();
        }
        if (null != deleteMemberResults[0]) {
            WsResultMeta delMeta = deleteMemberResults[0].getResultMetadata();
            this.moved = "SUCCESS".equals(delMeta.getResultCode());
        }
    }

    // Getters

    public String getIdentifier() {
        return identifier;
    }

    public String getUid() {
        return uid;
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public String getName() {
        return name;
    }

    public boolean isAdded() {
        return added;
    }

    public boolean isMoved() {
        return moved;
    }

    // Setters

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid;
    }
}