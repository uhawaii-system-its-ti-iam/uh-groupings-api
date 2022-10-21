package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import java.util.Objects;

public class HasMemberCommand extends GrouperCommand implements Command<HasMemberResults> {
    private GcHasMember gcHasMember;

    public HasMemberCommand(String groupPath, String uhIdentifier) {
        Objects.requireNonNull(groupPath, "groupPath cannot be null");
        Objects.requireNonNull(uhIdentifier, "uhIdentifier cannot be null");
        gcHasMember = new GcHasMember();
        gcHasMember.assignIncludeSubjectDetail(true);
        this.assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier);
    }

    @Override public HasMemberResults execute() {
        WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
        return new HasMemberResults(wsHasMemberResults);
    }

    private HasMemberCommand assignGroupPath(String groupPath) {
        gcHasMember.assignGroupName(groupPath);
        return this;
    }

    private HasMemberCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private HasMemberCommand addUhUuid(String uhUuid) {
        gcHasMember.addSubjectId(uhUuid);
        return this;
    }

    private HasMemberCommand addUid(String uid) {
        gcHasMember.addSubjectIdentifier(uid);
        return this;
    }
}
