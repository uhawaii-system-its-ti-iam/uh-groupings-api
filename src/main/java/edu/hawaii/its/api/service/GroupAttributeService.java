package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import java.util.List;

public interface GroupAttributeService {

    public GroupingsServiceResult changeListservStatus(String groupingPath, String ownerUsername, boolean isListservOn);

    public GroupingsServiceResult changeReleasedGroupingStatus(String groupingPath, String ownerUsername, boolean isReleasedGroupingOn);

    public List<GroupingsServiceResult> changeOptInStatus(String groupingPath, String ownerUsername, boolean isOptInOn);

    public List<GroupingsServiceResult> changeOptOutStatus(String groupingPath, String ownerUsername, boolean isOptOutOn);

    public boolean isContainingListserv(String groupingPath);

    public boolean isContainingReleasedGrouping(String groupingPath);

    public boolean isOptOutPossible(String groupingPath);

    public boolean isOptInPossible(String groupingPath);

    public boolean groupHasAttribute(String groupPath, String attribute);

    //do not include in REST controller
    public WsGetAttributeAssignmentsResults attributeAssignmentsResults(String assignType, String groupPath,
            String attributeName);
}
