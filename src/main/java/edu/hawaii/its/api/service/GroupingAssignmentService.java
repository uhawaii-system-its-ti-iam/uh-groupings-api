package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;

import java.util.List;
import java.util.Map;

public interface GroupingAssignmentService {

    Grouping getGrouping(String groupingPath, String ownerUsername);

    AdminListsHolder adminLists(String adminUsername);

    Map<String, Group> getMembers(String ownerUsername, List<String> groupPaths);

    Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size,
            String sortString,
            Boolean isAscending);

    Grouping setGroupingAttributes(Grouping grouping);

    Map<String, Group> getPaginatedMembers(String ownerUsername, List<String> groupPaths, Integer page, Integer size,
            String sortString, Boolean isAscending);

    List<String> getGroupPaths(String ownerUsername, String username);

    List<String> optOutGroupingsPaths(String owner, String optOutUid);

    List<String> optInGroupingsPaths(String owner, String optInUid);

    List<String> optableGroupings(String optAttr);

    List<String> allGroupingsPaths();

    List<String> getGroupingOwners(String currentUser, String groupPath);

    Boolean isSoleOwner(String currentUser, String groupPath, String uidToCheck);
}
