package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import java.util.List;

public interface MembershipService {

    List<GroupingsServiceResult> addGroupingMember(String ownerUsername, String groupingPath, String userToAdd);

    List<GroupingsServiceResult> addGroupingMemberByUsername(String ownerUsername, String groupingPath,
                                                             String userToAddUsername);

    List<GroupingsServiceResult> addGroupingMemberByUuid(String ownerUsername, String groupingPath,
                                                         String userToAddUuid);

    List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupingPath, String userToAdd);

    List<GroupingsServiceResult> addGroupMemberByUsername(String ownerUsername, String groupPath,
                                                          String userToAddUsername);

    List<GroupingsServiceResult> addGroupMemberByUuid(String ownerUsername, String groupPath,
                                                      String userToAddUsername);

    List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupingPath, List<String> usersToAdd);

    List<GroupingsServiceResult> addGroupMembersByUsername(String ownerUsername, String group,
                                                           List<String> usersToAddUsername);

    List<GroupingsServiceResult> addGroupMembersByUuid(String ownerUsername, String group,
                                                       List<String> usersToAddUuid);

    List<GroupingsServiceResult> deleteGroupingMemberByUsername(String ownerUsername, String groupingPath,
                                                                String userToDeleteUsername);

    List<GroupingsServiceResult> deleteGroupingMemberByUuid(String ownerUsername, String groupingPath,
                                                            String userToDeleteUuid);

    GroupingsServiceResult deleteGroupMemberByUsername(String ownerUsername, String groupPath,
                                                       String userToDeleteUsername);

    GroupingsServiceResult deleteGroupMemberByUuid(String ownerUsername, String groupPath,
                                                   String userToDeleteUuid);

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
