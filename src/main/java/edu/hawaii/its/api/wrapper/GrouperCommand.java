package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

public abstract class GrouperCommand<T> {

    protected static final String GROUP_SUBJECT_SOURCE_ID = "g:gsa";

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

    /**
     * A lookup of uhIdentifier in the subject source sourceId only, or in every source when sourceId is null or
     * empty. Grouper resolves a lookup that names its source about five times faster, because it otherwise searches
     * every subject source for the identifier.
     */
    protected WsSubjectLookup subjectLookup(String uhIdentifier, String sourceId) {
        WsSubjectLookup wsSubjectLookup = subjectLookup(uhIdentifier);
        if (sourceId != null && !sourceId.isEmpty()) {
            wsSubjectLookup.setSubjectSourceId(sourceId);
        }
        return wsSubjectLookup;
    }

    /**
     * A subject lookup for a group rather than a person. Grouper identifies a group used as a subject by the
     * g:gsa source id.
     */
    protected WsSubjectLookup groupSubjectLookup(String groupPath) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        wsSubjectLookup.setSubjectSourceId(GROUP_SUBJECT_SOURCE_ID);
        wsSubjectLookup.setSubjectIdentifier(groupPath);
        return wsSubjectLookup;
    }

    /**
     * Whether naming is a UH number (8 digits), which Grouper looks up as a subject id rather than an identifier.
     */
    public static boolean isUhUuid(String naming) {
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
