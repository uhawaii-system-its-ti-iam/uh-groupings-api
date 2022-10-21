package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import java.util.List;

public class HasMembersCommand extends GrouperCommand implements Command<HasMembersResults> {

    private GcHasMember gcHasMember;

    public HasMembersCommand(String groupPath, List<String> uhIdentifiers) {
        gcHasMember = new GcHasMember();
        gcHasMember.assignIncludeSubjectDetail(true);
        for (String uhIdentifier : uhIdentifiers) {
            this.assignGroupPath(groupPath)
                    .addUhIdentifier(uhIdentifier);
        }
    }

    @Override public HasMembersResults execute() {
        HasMembersResults hasMembersResult;
        try {
            WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
            hasMembersResult = new HasMembersResults(wsHasMemberResults);
        } catch (RuntimeException e) {
            hasMembersResult = new HasMembersResults();
        }
        return hasMembersResult;
    }

    private HasMembersCommand assignGroupPath(String groupPath) {
        gcHasMember.assignGroupName(groupPath);
        return this;
    }

    private HasMembersCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private HasMembersCommand addUhUuid(String uhUuid) {
        gcHasMember.addSubjectId(uhUuid);
        return this;
    }

    private HasMembersCommand addUid(String uid) {
        gcHasMember.addSubjectIdentifier(uid);
        return this;
    }
}
