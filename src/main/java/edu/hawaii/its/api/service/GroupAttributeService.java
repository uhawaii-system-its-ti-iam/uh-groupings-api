package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;

import java.util.List;

public interface GroupAttributeService {

    List<SyncDestination> getAllSyncDestinations(String currentUsername, String path);

    List<SyncDestination> getSyncDestinations(Grouping grouping);

    List<GroupingsServiceResult> changeOptStatus(OptRequest optInRequest, OptRequest optOutRequest);

    GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUsername,
            String attributeName, boolean turnAttributeOn);

    boolean isGroupAttribute(String groupPath, String attribute);

    GroupingsServiceResult assignGrouperPrivilege(String privilegeName, String groupPath, boolean isSet);

    GroupingsServiceResult updateDescription(String groupPath, String ownerUsername, String description);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action,
                                                      Person person);

    GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action);

}
