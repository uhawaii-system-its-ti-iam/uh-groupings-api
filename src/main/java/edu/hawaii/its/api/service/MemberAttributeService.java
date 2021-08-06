package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

import java.util.List;
import java.util.Map;

public interface MemberAttributeService extends BatchIsMember {
    GroupingsServiceResult assignOwnership(String groupingPath, String ownerUsername, String newOwnerUsername);

    GroupingsServiceResult removeOwnership(String groupingPath, String actor, String ownerToRemoveUsername);

    boolean isMember(String groupPath, String username);

    boolean isMember(String groupPath, Person person);

    boolean isMemberUuid(String groupPath, String idnum);

    boolean isOwner(String groupingPath, String username);

    boolean isOwner(String username);

    boolean isAdmin(String username);

    boolean isApp(String username);

    boolean isSelfOpted(String groupPath, String username);

    WsAttributeAssign[] getMembershipAttributes(String assignType, String attributeUuid, String membershipID);

    Map<String, String> getMemberAttributes(String ownerUsername, String uid);

    boolean isUhUuid(String naming);

    List<Person> searchMembers(String groupPath, String username);

    String getSpecificUserAttribute(String adminUser, String username, int attribute);

    List<GroupingPath> getOwnedGroupings(String currentUser, String user);
}
