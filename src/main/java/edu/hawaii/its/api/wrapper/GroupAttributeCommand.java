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

    public GroupAttributeCommand(String attribute) {
        this();
        addAttribute(attribute);
    }

    public GroupAttributeCommand(String attribute, String groupPath) {
        this();
        addAttribute(attribute);
        addGroup(groupPath);
    }

    public GroupAttributeCommand(String attribute, List<String> groupPaths) {
        this(attribute);
        for (String path : groupPaths) {
            addGroup(path);
        }
    }

    public GroupAttributeCommand(List<String> attributes, String groupPath) {
        this();
        addGroup(groupPath);
        for (String attribute : attributes) {
            addAttribute(attribute);
        }
    }

    public GroupAttributeCommand(List<String> attributes, List<String> groupPaths) {
        this();
        for (String path : groupPaths) {
            addGroup(path);
        }
        for (String attribute : attributes) {
            addAttribute(attribute);
        }
    }

    @Override
    public GroupAttributeResults execute() {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = gcGetAttributeAssignments.execute();
        return new GroupAttributeResults(wsGetAttributeAssignmentsResults);
    }

    private GroupAttributeCommand addAttribute(String attribute) {
        gcGetAttributeAssignments.addAttributeDefNameName(attribute);
        return this;
    }

    private GroupAttributeCommand addGroup(String groupPath) {
        gcGetAttributeAssignments.addOwnerGroupName(groupPath);
        return this;
    }

}
