package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for WsGetMembersResult. WsGetMembersResult contains a list of members of a group. WsGetMembersResult is a
 * singular result, which is returned in WsGetMembersResults when GcGetMembers.execute(wrapped by GetMembersCommand) is
 * called. A single WsGetMembersResult is returned in WsGetMembersResults for each group path queried with GcGetMembers.
 */
public class GetMembersResult extends Results {

    private final WsGetMembersResult wsGetMembersResult;

    public GetMembersResult(WsGetMembersResult wsGetMembersResult) {
        if (wsGetMembersResult == null) {
            this.wsGetMembersResult = new WsGetMembersResult();
        } else {
            this.wsGetMembersResult = wsGetMembersResult;
        }
    }

    @Override public String getResultCode() {
        return getGroup().getResultCode();
    }

    public Group getGroup() {
        WsGroup wsGroup = wsGetMembersResult.getWsGroup();
        if (wsGroup == null) {
            return new Group();
        }
        return new Group(wsGroup);
    }

    public List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<>();
        WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
        if (isEmpty(wsSubjects)) {
            return subjects;
        }
        for (WsSubject wsSubject : wsSubjects) {
            subjects.add(new Subject(wsSubject));
        }
        return subjects;
    }
}
