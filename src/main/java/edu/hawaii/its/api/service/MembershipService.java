package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface MembershipService {

    List<GroupingsServiceResult> addGroupingMember(String ownerUsername, String groupingPath, String userIdentifier);

    List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupingPath, String userToAdd);

    List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupingPath, List<String> usersToAdd)
            throws IOException, MessagingException;

    List<GroupingsServiceResult> deleteGroupingMember(String ownerUsername, String groupingPath,
                                                                String userIdentifier);

    GroupingsServiceResult deleteGroupMember(String ownerUsername, String groupPath,
                                             String userToDelete);

    List<String> listOwned(String adminUsername, String username);

    GroupingsServiceResult addAdmin(String adminUsername, String adminToAddUsername);

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
}
