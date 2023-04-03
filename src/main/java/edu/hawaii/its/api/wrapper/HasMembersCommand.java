package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import java.util.List;

/**
 * A wrapper for GcHasMember.  When a group path and UH identifier are passed, HasMembersCommand on execute fetches,
 * from grouper, results which confirm whether the UH identifier is listed with the group at group path.
 */
public class HasMembersCommand extends GrouperCommand implements Command<HasMembersResults> {

    private final GcHasMember gcHasMember;

    public HasMembersCommand() {
        this.gcHasMember = new GcHasMember();
        this.gcHasMember.assignIncludeSubjectDetail(true);
    }

    @Override public HasMembersResults execute() {
        WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
        return new HasMembersResults(wsHasMemberResults);
    }

    public HasMembersCommand assignGroupPath(String groupPath) {
        gcHasMember.assignGroupName(groupPath);
        return this;
    }

    public HasMembersCommand addUhIdentifiers(List<String> uhIdentifiers) {
        uhIdentifiers.forEach(this::addUhIdentifier);
        return this;
    }

    public HasMembersCommand addUhIdentifier(String uhIdentifier) {
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
