package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import java.util.List;

public interface GroupAttributeService {

    public GroupingsServiceResult changeListservStatus(String groupingPath, String ownerUsername, boolean listservOn);

    public GroupingsServiceResult changeReleasedGroupingStatus(String groupingPath, String ownerUsername, boolean releasedGroupingOn);

    public List<GroupingsServiceResult> changeOptInStatus(String groupingPath, String ownerUsername, boolean optInOn);

    public List<GroupingsServiceResult> changeOptOutStatus(String groupingPath, String ownerUsername, boolean optOutOn);

    public boolean isHasListserv(String groupingPath);

    public boolean isHasReleasedGrouping(String groupingPath);

    public boolean isOptOutPossible(String groupingPath);

    public boolean isOptInPossible(String groupingPath);

    //do not include in REST controller
    public WsGetAttributeAssignmentsResults attributeAssignmentsResults(String assignType, String groupPath,
            String attributeName);
}
