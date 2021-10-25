package edu.hawaii.its.api.service;

import java.util.List;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.SyncDestination;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

public interface GroupAttributeService {

    List<GroupingsServiceResult> changeOptInStatus(String groupingPath, String currentUsername, boolean isOptInOn);

    List<GroupingsServiceResult> changeOptOutStatus(String groupingPath, String currentUsername, boolean isOptOutOn);

    boolean isGroupAttribute(String groupPath, String attribute);

    List<SyncDestination> getAllSyncDestinations(String currentUsername, String path);

    // Do not include in REST controller.
    WsGetAttributeAssignmentsResults attributeAssignmentsResults(String assignType, String groupPath,
            String attributeName);

    GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUsername,
            String attributeName, boolean turnAttributeOn);

    List<SyncDestination> getSyncDestinations(Grouping grouping);

    GroupingsServiceResult updateDescription(String groupPath, String ownerUsername, String description);
}
