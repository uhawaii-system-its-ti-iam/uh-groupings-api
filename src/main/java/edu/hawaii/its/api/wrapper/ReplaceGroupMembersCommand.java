package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.ArrayList;
import java.util.List;

public class ReplaceGroupMembersCommand extends AddMembersCommand {

    /**
     * Calling execute on this object replaces the members in the group at groupPath with members from the list of uhIdentifiers.
     */
    public ReplaceGroupMembersCommand(String groupPath, List<String> uhIdentifiers) {
        super(groupPath, uhIdentifiers);
        replaceGroupMembers(true);
    }

    /**
     * Calling execute on this object replaces the members in the group at groupPath with an empty list of members.
     */
    public ReplaceGroupMembersCommand(String groupPath) {
        this(groupPath, new ArrayList<>());
    }

    @Override
    public ReplaceGroupMembersResult execute() {
        WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
        return new ReplaceGroupMembersResult(wsAddMemberResults);
    }

}
