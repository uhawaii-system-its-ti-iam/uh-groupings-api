package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.List;
import java.util.Map;

public interface HelperService {

    boolean isUhUuid(String username);

    String extractFirstMembershipID(WsGetMembershipsResults wsGetMembershipsResults);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action,
            Person person);

    GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action);

    List<GroupingPath> makePaths(List<String> groupingPaths);

    String parentGroupingPath(String group);

    String nameGroupingPath(String group);

    Map<String, Group> makeGroups(WsGetMembersResults membersResults);

    Person makePerson(WsSubject subject, String[] attributeNames);

    List<String> extractGroupPaths(List<WsGroup> groups);

}
