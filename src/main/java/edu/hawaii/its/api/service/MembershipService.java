package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface MembershipService {

    List<Membership> getMembershipResults(String owner, String uid);

    List<GroupingsServiceResult> addGroupingMember(String ownerUsername, String groupingPath, String userIdentifier);

    GenericServiceResult addGroupMemberr(String ownerUsername, String groupPath, List<String> uids);

    List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupingPath, String userToAdd);

    List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupingPath, List<String> usersToAdd)
            throws IOException, MessagingException;

    List<GroupingsServiceResult> deleteGroupingMember(String ownerUsername, String groupingPath,
            String userIdentifier);

    GroupingsServiceResult deleteGroupMember(String ownerUsername, String groupPath,
            String userToDelete);

    GenericServiceResult deleteGroupMembers(String currentUser, String groupingPath,
            List<String> usersToDelete);

    List<String> listOwned(String adminUsername, String username);

    GroupingsServiceResult addAdmin(String adminUsername, String adminToAddUsername);

    List<GroupingsServiceResult> removeFromGroups(String adminUsername, String userToRemove, List<String> GroupPaths);

    List<GroupingsServiceResult> resetGroup(String ownerUsername, String path,
            List<String> includeIdentifier, List<String> excludeIdentifier);

    GroupingsServiceResult deleteAdmin(String adminUsername, String adminToDeleteUsername);

    List<GroupingsServiceResult> optIn(String username, String groupingPath);

    List<GroupingsServiceResult> optOut(String username, String groupingPath);

    List<GroupingsServiceResult> optIn(String username, String groupingPath, String uid);

    List<GroupingsServiceResult> optOut(String username, String groupingPath, String uid);

    boolean isGroupCanOptIn(String username, String groupPath);

    boolean isGroupCanOptOut(String username, String groupPath);

    //do not include in REST controller
    GroupingsServiceResult updateLastModified(String groupPath);

    GroupingsServiceResult addSelfOpted(String groupPath, String username);

    GroupingsServiceResult removeSelfOpted(String groupPath, String username);

    GenericServiceResult generic();

    boolean isUhUuid(String username);

    boolean canOpt(String path);
}
