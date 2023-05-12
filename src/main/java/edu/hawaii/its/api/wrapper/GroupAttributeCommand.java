package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import java.util.List;

/**
 * A wrapper for GcGetAttributeAssignments. When an attribute is passed, GroupAttributeCommand on execute
 * fetches (from grouper) results containing all the groups baring the attribute. Specifying a groupPath(s) with the attribute passed
 * will fetch attribute results pertaining only to that group(s).
 */
public class GroupAttributeCommand extends GrouperCommand implements Command<GroupAttributeResults> {
    protected final GcGetAttributeAssignments gcGetAttributeAssignments;

    public GroupAttributeCommand() {
        this.gcGetAttributeAssignments = new GcGetAttributeAssignments();
        this.gcGetAttributeAssignments.assignAttributeAssignType("group");
    }

    @Override
    public GroupAttributeResults execute() {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = gcGetAttributeAssignments.execute();
        return new GroupAttributeResults(wsGetAttributeAssignmentsResults);
    }

    public GroupAttributeCommand addAttribute(String attribute) {
        gcGetAttributeAssignments.addAttributeDefNameName(attribute);
        return this;
    }

    public GroupAttributeCommand addAttributes(List<String> attributes) {
        for (String attribute : attributes) {
            gcGetAttributeAssignments.addAttributeDefNameName(attribute);
        }
        return this;
    }

    public GroupAttributeCommand addGroup(String groupPath) {
        gcGetAttributeAssignments.addOwnerGroupName(groupPath);
        return this;
    }

    public GroupAttributeCommand addGroups(List<String> groupPaths) {
        for (String groupPath : groupPaths) {
            gcGetAttributeAssignments.addOwnerGroupName(groupPath);
        }
        return this;
    }

    public GroupAttributeCommand assignOwner(String uhIdentifier) {
        gcGetAttributeAssignments.assignActAsSubject(subjectLookup(uhIdentifier));
        return this;
    }
}
