package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

import java.util.List;

public interface MemberAttributeService extends BatchIsMember {

    boolean isUhUuid(String username);

    boolean isGroupCanOptIn(String username, String groupPath);

    boolean isGroupCanOptOut(String username, String groupPath);

    boolean isMember(String groupPath, String username);

    boolean isMember(String groupPath, Person person);

    boolean isMemberUuid(String groupPath, String idnum);

    boolean isOwner(String groupingPath, String username);

    boolean isOwner(String username);

    boolean isAdmin(String username);

    boolean isApp(String username);

    boolean isSelfOpted(String groupPath, String username);

    WsAttributeAssign[] getMembershipAttributes(String assignType, String attributeUuid, String membershipID);

    Person getMemberAttributes(String ownerUsername, String uid);

    List<Person> searchMembers(String groupPath, String username);

    String getSpecificUserAttribute(String adminUser, String username, int attribute);

    List<GroupingPath> getOwnedGroupings(String currentUser, String user);

    List<Membership> getMembershipResults(String owner, String uid);

    Integer getNumberOfGroupings(String currentUser, String uid);

    Integer getNumberOfMemberships(String currentUser, String uid);

}