package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;

import java.util.List;

public interface MemberAttributeService {

    boolean isMember(String groupPath, String username);

    boolean isMember(String groupPath, Person person);

    boolean isMemberUuid(String groupPath, String idnum);

    boolean isUhUuid(String username);

    boolean isOwner(String groupingPath, String username);

    boolean isOwner(String username);

    boolean isAdmin(String username);

    boolean isApp(String username);

    Person getMemberAttributes(String ownerUsername, String uid);

    List<GroupingPath> getOwnedGroupings(String currentUser, String user);

    Integer getNumberOfGroupings(String currentUser, String uid);
}