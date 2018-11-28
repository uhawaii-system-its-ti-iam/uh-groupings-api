package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.MembershipAssignment;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.security.Principal;
import java.util.List;

public interface GroupingAssignmentService {

    public List<Grouping> groupingsIn(List<String> groupPaths);

    public List<Grouping> groupingsOwned(List<String> groupPaths);

    public List<Grouping> restGroupingsOwned(String actingUsername, String ownerUsername);

    public List<Grouping> groupingsOptedInto(String username, List<String> groupPaths);

    public List<Grouping> groupingsOptedOutOf(String username, List<String> groupPaths);

    public Grouping getGrouping(String groupingPath, String ownerUsername);

    public Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size);

    public Grouping getPaginatedGroupingHelper(String ownerUsername, String groupingPath, Integer page, Integer size);

    public GroupingAssignment getGroupingAssignment(String username);

    //get a MembershipAssignment object containing the groups that a user is in and can opt into
    MembershipAssignment getMembershipAssignment(String username, String uid);

    public AdminListsHolder adminLists(String adminUsername);

    //not to be included in the REST controller
    public Person makePerson(WsSubject subject, String[] attributeNames);

    public List<String> extractGroupPaths(List<WsGroup> groups);

    public Group makeGroup(WsGetMembersResults membersResults);

    public Group makeBasisGroup(WsGetMembersResults membersResults);

    public List<String> getGroupPaths(String ownerUsername, String username);

    public List<String> getGroupPaths(Principal principal, String username);

    public Group getMembers(String ownerUsername, String groupPath);

    public Group getPaginatedMembers( String ownerUsername, String groupPath, Integer page, Integer size);
}
