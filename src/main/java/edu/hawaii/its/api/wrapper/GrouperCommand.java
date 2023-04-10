package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

public abstract class GrouperCommand<T> {

    protected WsSubjectLookup subjectLookup(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();

        if (isUhUuid(uhIdentifier)) {
            wsSubjectLookup.setSubjectId(uhIdentifier);
        } else {
            wsSubjectLookup.setSubjectIdentifier(uhIdentifier);
        }
        return wsSubjectLookup;
    }

    protected boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }
}
