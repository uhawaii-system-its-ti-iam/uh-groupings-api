package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public interface GroupingFactoryService {

    List<GroupingsServiceResult> addGrouping(
            String username,
            String groupingPath);


    List<GroupingsServiceResult> deleteGrouping(String adminUsername, String groupingPath);

    List<GroupingsServiceResult> markGroupForPurge(String adminUsername, String groupingPath);

    boolean isPathEmpty(String adminUsername, String groupingPath);

}
