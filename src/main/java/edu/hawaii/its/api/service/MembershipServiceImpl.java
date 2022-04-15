package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("membershipService")
public class MembershipServiceImpl implements MembershipService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GrouperApiService grouperApiService;

    public static final Log logger = LogFactory.getLog(MembershipServiceImpl.class);

    /**
     * Add am admin.
     */
    @Override
    public AddMemberResult addAdmin(String currentUser, String adminToAdd) {
        logger.info("addAdmin; username: " + currentUser + "; newAdmin: " + adminToAdd + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        WsAddMemberResults addMemberResult = grouperApiService.addMember(
                GROUPING_ADMINS,
                adminToAdd);
        return new AddMemberResult(addMemberResult);
    }

    /**
     * Remove an admin.
     */
    @Override
    public RemoveMemberResult removeAdmin(String currentUser, String adminToRemove) {
        logger.info("removeAdmin; username: " + currentUser + "; adminToRemove: " + adminToRemove + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        WsDeleteMemberResults deleteMemberResult = grouperApiService.removeMember(
                GROUPING_ADMINS,
                adminToRemove);
        return new RemoveMemberResult(deleteMemberResult);
    }

    /**
     * Get a list of memberships pertaining to uid.
     */
    @Override
    public List<Membership> membershipResults(String currentUser, String uid) {
        String action = "getMembershipResults; currentUser: " + currentUser + "; uid: " + uid + ";";
        logger.info(action);

        if (!memberAttributeService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<Membership> memberships = new ArrayList<>();
        List<String> groupPaths;
        List<String> optOutList;
        try {
            groupPaths = groupingAssignmentService.getGroupPaths(currentUser, uid);
            optOutList = groupingAssignmentService.optableGroupings(OPT_OUT);
        } catch (GcWebServiceError e) {
            return memberships;
        }
        Map<String, List<String>> pathMap = new HashMap<>();
        for (String pathToCheck : groupPaths) {
            if (!pathToCheck.endsWith(INCLUDE) && !pathToCheck.endsWith(EXCLUDE) && !pathToCheck.endsWith(BASIS)
                    && !pathToCheck.endsWith(OWNERS)) {
                continue;
            }
            String parentPath = helperService.parentGroupingPath(pathToCheck);
            if (!pathMap.containsKey(parentPath)) {
                pathMap.put(parentPath, new ArrayList<>());
            }
            pathMap.get(parentPath).add(pathToCheck);
        }

        for (Map.Entry<String, List<String>> entry : pathMap.entrySet()) {
            String groupingPath = entry.getKey();
            List<String> paths = entry.getValue();
            Membership membership = new Membership();
            for (String path : paths) {
                if (path.endsWith(BASIS)) {
                    membership.setInBasis(true);
                }
                if (path.endsWith(INCLUDE)) {
                    membership.setInInclude(true);
                }
                if (path.endsWith(EXCLUDE)) {
                    membership.setInExclude(true);
                }
                if (path.endsWith(OWNERS)) {
                    membership.setInOwner(true);
                }
            }
            membership.setPath(groupingPath);
            membership.setOptOutEnabled(optOutList.contains(groupingPath));
            membership.setName(helperService.nameGroupingPath(groupingPath));
            memberships.add(membership);
        }
        return memberships;
    }

    /**
     * Add all uids/uhUuids contained in list usersToAdd to the group at groupPath. When adding to the include group
     * members which already exist in the exclude group will be removed from the exclude group and visa-versa. This
     * method was designed to add new members to the include and exclude groups only. Upon passing group paths other than
     * include or exclude, addGroupMembers will return empty list.
     */
    @Override
    public List<AddMemberResult> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd) {
        logger.info("addGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToAdd: " + usersToAdd + ";");

        List<AddMemberResult> addMemberResults = new ArrayList<>();
        String removalPath = helperService.parentGroupingPath(groupPath);

        if (groupPath.endsWith(INCLUDE)) {
            removalPath += EXCLUDE;
        } else if (groupPath.endsWith(EXCLUDE)) {
            removalPath += INCLUDE;
        } else {
            throw new GcWebServiceError("404: Invalid group path.");
        }

        for (String userToAdd : usersToAdd) {
            AddMemberResult addMemberResult;
            try {
                WsDeleteMemberResults wsDeleteMemberResults = grouperApiService.removeMember(removalPath, userToAdd);
                WsAddMemberResults wsAddMemberResults = grouperApiService.addMember(groupPath, userToAdd);
                addMemberResult = new AddMemberResult(wsAddMemberResults, wsDeleteMemberResults);

                if (addMemberResult.isUserWasAdded()) {
                    membershipService.updateLastModified(groupPath);
                    if (addMemberResult.getUid() == null) {
                        addMemberResult.setUid(memberAttributeService
                                .getMemberAttributes(currentUser, addMemberResult.getUhUuid()).getUsername());
                    }
                }
                addMemberResults.add(addMemberResult);
            } catch (GcWebServiceError | NullPointerException e) {
                addMemberResult = new AddMemberResult(userToAdd, FAILURE);
                addMemberResults.add(addMemberResult);
            }
            logger.info("addGroupMembers; " + addMemberResult);
        }
        /*
        if (usersToAdd.size() > 100) {
            groupingsMailService
                    .setJavaMailSender(javaMailSender)
                    .setFrom("no-reply@its.hawaii.edu");
            groupingsMailService.sendCSVMessage(
                    "no-reply@its.hawaii.edu",
                    groupingsMailService.getUserEmail(currentUser),
                    "Groupings: Add " + groupPath,
                    "",
                    "UH-Groupings-Report-" + LocalDateTime.now().toString() + ".csv", addMemberResults);
        }
         */
        return addMemberResults;
    }

    /**
     * Check if the currentUser has the proper privileges then call addGroupMembers.
     */
    @Override
    public List<AddMemberResult> addIncludeMembers(String currentUser, String groupingPath, List<String> usersToAdd) {
        logger.info("addIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + INCLUDE, usersToAdd);
    }

    /**
     * Check if the currentUser has the proper privileges then call addGroupMembers.
     */
    @Override
    public List<AddMemberResult> addExcludeMembers(String currentUser, String groupingPath, List<String> usersToAdd) {
        logger.info("addExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + EXCLUDE, usersToAdd);
    }

    /**
     * Remove all the members in list usersToRemove from group at groupPath. This method was designed to remove members
     * from the include and exclude groups only. Passing in other group paths will result in undefined behavior.
     */
    @Override
    public List<RemoveMemberResult> removeGroupMembers(String currentUser, String groupPath,
            List<String> usersToRemove) {
        logger.info("removeGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToRemove: " + usersToRemove + ";");
        if (!groupPath.endsWith(INCLUDE) && !groupPath.endsWith(EXCLUDE)) {
            throw new GcWebServiceError("404: Invalid group path.");
        }
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        for (String userToRemove : usersToRemove) {
            RemoveMemberResult removeMemberResult;
            WsDeleteMemberResults wsDeleteMemberResults = grouperApiService.removeMember(groupPath, userToRemove);
            removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
            if (removeMemberResult.isUserWasRemoved()) {
                membershipService.updateLastModified(groupPath);
                if (removeMemberResult.getUid() == null) {
                    removeMemberResult.setUid(memberAttributeService
                            .getMemberAttributes(currentUser, removeMemberResult.getUhUuid()).getUsername());
                }
            }
            removeMemberResults.add(removeMemberResult);
            logger.info("removeGroupMembers; " + removeMemberResult.toString());
        }
        return removeMemberResults;
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    @Override public List<RemoveMemberResult> removeIncludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return removeGroupMembers(currentUser, groupingPath + INCLUDE, usersToRemove);
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    @Override public List<RemoveMemberResult> removeExcludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return removeGroupMembers(currentUser, groupingPath + EXCLUDE, usersToRemove);
    }

    /**
     * Remove owner/owners from groupings at groupingPath.
     */
    public List<RemoveMemberResult> removeOwnerships(String groupingPath, String actor, List<String> ownersToRemove) {
        logger.info("removeOwnership; grouping: "
                + groupingPath
                + "; username: "
                + actor
                + "; ownerToRemove: "
                + ownersToRemove
                + ";");

        if (!memberAttributeService.isOwner(groupingPath, actor) && !memberAttributeService.isAdmin(actor)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        // Makes the admin also the owner in the event that there are no remaining owners otherwise.
        if (!memberAttributeService.isOwner(groupingPath, actor) && memberAttributeService.isAdmin(actor)) {
            addOwnerships(groupingPath, ownersToRemove.get(0), Arrays.asList(actor));
        }

        List<RemoveMemberResult> removeMemberResultList = new ArrayList<>();
        WsSubjectLookup lookup = grouperApiService.subjectLookup(actor);
        for (String ownerToRemove : ownersToRemove) {
            RemoveMemberResult ownershipResults;

            WsDeleteMemberResults memberResults =
                    grouperApiService.removeMember(groupingPath + OWNERS, lookup, ownerToRemove);
            ownershipResults = new RemoveMemberResult(memberResults);
            if (ownershipResults.isUserWasRemoved()) {
                membershipService.updateLastModified(groupingPath);
                membershipService.updateLastModified(groupingPath + OWNERS);
            }
            removeMemberResultList.add(ownershipResults);
        }
        return removeMemberResultList;
    }

    /**
     * Gives ownership to a single or multiple users.
     */
    public List<AddMemberResult> addOwnerships(String groupingPath, String ownerUsername, List<String> ownersToAdd) {
        logger.info("assignOwnership; groupingPath: "
                + groupingPath
                + "; ownerUsername: "
                + ownerUsername
                + "; newOwnerUsername: "
                + ownersToAdd
                + ";");
        List<AddMemberResult> addOwnerResults = new ArrayList<>();
        WsSubjectLookup wsSubjectLookup = grouperApiService.subjectLookup(ownerUsername);
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService
                .isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        for (String ownerToAdd : ownersToAdd) {
            AddMemberResult addOwnerResult;
            WsAddMemberResults wsAddMemberResults =
                    grouperApiService.addMember(groupingPath + OWNERS, wsSubjectLookup, ownerToAdd);
            addOwnerResult = new AddMemberResult(wsAddMemberResults);
            if (addOwnerResult.isUserWasAdded()) {
                membershipService.updateLastModified(groupingPath);
                membershipService.updateLastModified(groupingPath + OWNERS);
            }
            addOwnerResults.add(addOwnerResult);
        }
        return addOwnerResults;
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting in adds a member/user at
     * uid to the include list and removes them from the exclude list.
     */
    @Override public List<AddMemberResult> optIn(String currentUser, String groupingPath, String uid) {
        logger.info("optIn; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + INCLUDE, Collections.singletonList(uid));
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting out adds a member/user
     * at uid to the exclude list and removes them from the include list.
     */
    @Override public List<AddMemberResult> optOut(String currentUser, String groupingPath, String uid) {
        logger.info("optOut; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + EXCLUDE, Collections.singletonList(uid));
    }

    /**
     * Remove a user from all groups listed in groupPaths
     */
    @Override
    public List<RemoveMemberResult> removeFromGroups(String adminUsername, String userToRemove,
            List<String> groupPaths) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<RemoveMemberResult> results = new ArrayList<>();
        for (String groupPath : groupPaths) {
            if (!groupPath.endsWith(OWNERS) && !groupPath.endsWith(INCLUDE) && !groupPath.endsWith(EXCLUDE)) {
                throw new GcWebServiceError("404: Invalid group path.");
            }
            results.add(new RemoveMemberResult(grouperApiService.removeMember(groupPath, userToRemove)));
        }
        return results;
    }

    /**
     * Remove all uses from the include and/or exclude groups of grouping at path.
     */
    @Override
    public List<RemoveMemberResult> resetGroup(String currentUser, String path, List<String> uhNumbersInclude,
            List<String> uhNumbersExclude) {
        if (!memberAttributeService.isAdmin(currentUser) && !memberAttributeService.isOwner(path, currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<RemoveMemberResult> results = new ArrayList<>();
        if (!uhNumbersInclude.isEmpty()) {
            results.addAll(removeGroupMembers(currentUser, path + INCLUDE, uhNumbersInclude));
        }
        if (!uhNumbersExclude.isEmpty()) {
            results.addAll(removeGroupMembers(currentUser, path + EXCLUDE, uhNumbersExclude));
        }
        return results;
    }

    /**
     * Update the last modified attribute of a group to the current date and time.
     */
    @Override
    public UpdateTimestampResult updateLastModified(String groupPath) {
        logger.info("updateLastModified; group: " + groupPath + ";");
        String dateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        return updateLastModifiedTimestamp(dateTime, groupPath);
    }

    /**
     * Update the last modified attribute of a group to dateTime.
     */
    @Override
    public UpdateTimestampResult updateLastModifiedTimestamp(String dateTime, String groupPath) {
        WsAttributeAssignValue wsAttributeAssignValue = grouperApiService.assignAttributeValue(dateTime);
        return new UpdateTimestampResult(grouperApiService.assignAttributesResults(
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                groupPath,
                YYYYMMDDTHHMM,
                OPERATION_REPLACE_VALUES,
                wsAttributeAssignValue));
    }

    /**
     * Get the number of memberships.
     */
    @Override public Integer getNumberOfMemberships(String currentUser, String uid) {
        return membershipResults(currentUser, uid).size();
    }
}