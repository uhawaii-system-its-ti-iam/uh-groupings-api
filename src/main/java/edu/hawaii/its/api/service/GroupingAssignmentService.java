package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.List;
import java.util.Map;

public interface GroupingAssignmentService {

    List<Grouping> groupingsIn(List<String> groupPaths);

    List<Grouping> groupingsOwned(List<String> groupPaths);

    List<String> groupingsOwnedPaths(List<String> groupPaths);

    List<Grouping> groupingsOptedInto(String username, List<String> groupPaths);

    List<Grouping> groupingsOptedOutOf(String username, List<String> groupPaths);

    Grouping getGrouping(String groupingPath, String ownerUsername);

    Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size,
            String sortString,
            Boolean isAscending);

    AdminListsHolder adminLists(String adminUsername);

    //not to be included in the REST controller
    Person makePerson(WsSubject subject, String[] attributeNames);

    List<Grouping> groupingsOpted(String includeOrrExclude, String username, List<String> groupPaths);

    List<String> extractGroupPaths(List<WsGroup> groups);

    Map<String, Group> makeGroups(WsGetMembersResults membersResults);

    List<String> getGroupPaths(String ownerUsername, String username);

    Map<String, Group> getMembers(String ownerUsername, List<String> groupPaths);

    Map<String, Group> getPaginatedMembers(String ownerUsername, List<String> groupPaths, Integer page, Integer size,
            String sortString, Boolean isAscending);

    List<Grouping> restGroupingsExclude(String actingUsername, String ownerUsername);

    List<Grouping> excludeGroups(List<String> groupPaths);

    List<Grouping> groupingsToOptInto(String optInUsername, List<String> groupPaths);

    List<String> getOptInGroups(String owner, String optInUid);

    List<String> getOptOutGroups(String owner, String optOutUid);
}
