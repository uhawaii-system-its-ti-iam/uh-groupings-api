package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;

import java.util.List;

public interface MemberAttributeService {

    boolean isMember(String groupPath, String uhIdentifier);

    boolean isMember(String groupPath, Person person);

    boolean isMemberUuid(String groupPath, String uhUuid);

    boolean isUhUuid(String uhIdentifier);

    boolean isOwner(String groupingPath, String uhIdentifier);

    boolean isOwner(String uhIdentifier);

    boolean isAdmin(String uhIdentifier);

    boolean isApp(String uhIdentifier);

    Person getMemberAttributes(String ownerUsername, String uid);

    List<GroupingPath> getOwnedGroupings(String currentUser, String user);

    Integer getNumberOfGroupings(String currentUser, String uid);
}