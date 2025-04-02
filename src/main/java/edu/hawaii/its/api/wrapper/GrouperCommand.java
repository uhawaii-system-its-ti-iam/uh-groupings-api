package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

public abstract class GrouperCommand<T> {

    private boolean retry = false;

    public boolean isRetry(){
        return this.retry;
    }

    public T setRetry(boolean retry) {
        this.retry = retry;
        return self();
    }

    protected abstract T self();

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
        return naming != null && naming.matches("^\\d{8}$");
    }

    public WsAttributeAssignValue assignAttributeValue(String value) {

        WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
        wsAttributeAssignValue.setValueSystem(value);

        return wsAttributeAssignValue;
    }

    public WsStemLookup stemLookup(String stemName) {
        return stemLookup(stemName, null);
    }

    public WsStemLookup stemLookup(String stemName, String stemUuid) {
        return new WsStemLookup(stemName, stemUuid);
    }

    protected WsGroupLookup groupLookup(String groupPath) {
        String grouperUuid = new FindGroupsCommand().addPath(groupPath).execute().getGroup().getGrouperUuid();
        return new WsGroupLookup(groupPath, grouperUuid);
    }
}
