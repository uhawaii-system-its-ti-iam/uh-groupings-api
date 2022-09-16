package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;
import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.GroupingsAddResults;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.UIAddMemberResults;
import edu.hawaii.its.api.type.UIRemoveMemberResults;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersCommand;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsCommand;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("membershipService")
public class MembershipServiceImpl implements MembershipService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

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
    public UIAddMemberResults addAdmin(String currentUser, String adminToAdd) {
        logger.info("addAdmin; username: " + currentUser + "; newAdmin: " + adminToAdd + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        AddMemberResult addMemberResult = grouperApiService.addMember(GROUPING_ADMINS, adminToAdd);
        return new UIAddMemberResults(addMemberResult);
    }

    /**
     * Remove an admin.
     */
    @Override
    public UIRemoveMemberResults removeAdmin(String currentUser, String adminToRemove) {
        logger.info("removeAdmin; username: " + currentUser + "; adminToRemove: " + adminToRemove + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        RemoveMemberResult removeMemberResult = grouperApiService.removeMember(GROUPING_ADMINS, adminToRemove);
        return new UIRemoveMemberResults(removeMemberResult);
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
            optOutList = groupingAssignmentService.optableGroupings(OptType.OUT.value());
        } catch (GcWebServiceError e) {
            return memberships;
        }
        Map<String, List<String>> pathMap = new HashMap<>();
        for (String pathToCheck : groupPaths) {
            if (!pathToCheck.endsWith(GroupType.INCLUDE.value())
                    && !pathToCheck.endsWith(GroupType.EXCLUDE.value())
                    && !pathToCheck.endsWith(GroupType.BASIS.value())
                    && !pathToCheck.endsWith(GroupType.OWNERS.value())) {
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
                if (path.endsWith(GroupType.BASIS.value())) {
                    membership.setInBasis(true);
                }
                if (path.endsWith(GroupType.INCLUDE.value())) {
                    membership.setInInclude(true);
                }
                if (path.endsWith(GroupType.EXCLUDE.value())) {
                    membership.setInExclude(true);
                }
                if (path.endsWith(GroupType.OWNERS.value())) {
                    membership.setInOwner(true);
                }
            }
            membership.setPath(groupingPath);
            membership.setOptOutEnabled(optOutList.contains(groupingPath));
            membership.setName(helperService.nameGroupingPath(groupingPath));
            membership.setDescription(grouperApiService.descriptionOf(groupingPath));
            memberships.add(membership);
        }
        return memberships;
    }

    @Override
    public GroupingsAddResults addGroupMembersNewImplementation(String currentUser, String groupPath, List<String> usersToAdd) {
        logger.info("addGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToAdd: " + usersToAdd + ";");

        String removalPath = helperService.parentGroupingPath(groupPath);

        if (groupPath.endsWith(GroupType.INCLUDE.value())) {
            removalPath += GroupType.EXCLUDE.value();
        } else if (groupPath.endsWith(GroupType.EXCLUDE.value())) {
            removalPath += GroupType.INCLUDE.value();
        } else {
            throw new GcWebServiceError("404: Invalid group path.");
        }
        SubjectsResults subjectsResults = new SubjectsCommand(usersToAdd).execute();
        List<String> validIdentifiers = new SubjectsService(subjectsResults)
                .check()
                .validUhUuids();
        RemoveMembersResults removeMembersResults = new RemoveMembersCommand(removalPath, validIdentifiers).execute();
        AddMembersResults addMembersResults = new AddMembersCommand(groupPath, validIdentifiers).execute();
        return new GroupingsAddResults(addMembersResults);
    }

    /**
     * Add all uids/uhUuids contained in list usersToAdd to the group at groupPath. When adding to the include group
     * members which already exist in the exclude group will be removed from the exclude group and visa-versa. This
     * method was designed to add new members to the include and exclude groups only. Upon passing group paths other than
     * include or exclude, addGroupMembers will return empty list.
     */
    @Override
    public List<UIAddMemberResults> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd) {
        logger.info("addGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToAdd: " + usersToAdd + ";");

        List<UIAddMemberResults> addMemberResults = new ArrayList<>();
        String removalPath = helperService.parentGroupingPath(groupPath);

        if (groupPath.endsWith(GroupType.INCLUDE.value())) {
            removalPath += GroupType.EXCLUDE.value();
        } else if (groupPath.endsWith(GroupType.EXCLUDE.value())) {
            removalPath += GroupType.INCLUDE.value();
        } else {
            throw new GcWebServiceError("404: Invalid group path.");
        }

        for (String userToAdd : usersToAdd) {
            addMemberResults.add(addMember(currentUser, userToAdd, removalPath, groupPath));
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
    public List<UIAddMemberResults> addIncludeMembers(String currentUser, String groupingPath,
            List<String> usersToAdd) {
        logger.info("addIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(), usersToAdd);
    }

    /**
     * Check if the currentUser has the proper privileges then call addGroupMembers.
     */
    @Override
    public List<UIAddMemberResults> addExcludeMembers(String currentUser, String groupingPath,
            List<String> usersToAdd) {
        logger.info("addExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return addGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(), usersToAdd);
    }

    /**
     * Remove all the members in list usersToRemove from group at groupPath. This method was designed to remove members
     * from the include and exclude groups only. Passing in other group paths will result in undefined behavior.
     */
    @Override
    public List<UIRemoveMemberResults> removeGroupMembers(String currentUser, String groupPath,
            List<String> usersToRemove) {
        logger.info("removeGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToRemove: " + usersToRemove + ";");
        if (!groupPath.endsWith(GroupType.INCLUDE.value()) && !groupPath.endsWith(GroupType.EXCLUDE.value())) {
            throw new GcWebServiceError("404: Invalid group path.");
        }
        List<UIRemoveMemberResults> removeMemberResults = new ArrayList<>();
        for (String userToRemove : usersToRemove) {
            UIRemoveMemberResults removeMemberResult;
            RemoveMemberResult removeMemberResponse = grouperApiService.removeMember(groupPath, userToRemove);
            removeMemberResult = new UIRemoveMemberResults(removeMemberResponse);
            if (removeMemberResult.isUserWasRemoved()) {
                membershipService.updateLastModified(groupPath);
            }
            removeMemberResults.add(removeMemberResult);
            logger.info("removeGroupMembers; " + removeMemberResult.toString());
        }
        return removeMemberResults;
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    @Override public List<UIRemoveMemberResults> removeIncludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return removeGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(), usersToRemove);
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    @Override public List<UIRemoveMemberResults> removeExcludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return removeGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(), usersToRemove);
    }

    /**
     * Remove owner/owners from groupings at groupingPath.
     */
    public List<UIRemoveMemberResults> removeOwnerships(String groupingPath, String actor,
            List<String> ownersToRemove) {
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

        List<UIRemoveMemberResults> removeMemberResultList = new ArrayList<>();
        for (String ownerToRemove : ownersToRemove) {
            UIRemoveMemberResults ownershipResults;

            RemoveMemberResult removeMemberResult =
                    grouperApiService.removeMember(groupingPath + GroupType.OWNERS.value(), ownerToRemove);
            ownershipResults = new UIRemoveMemberResults(removeMemberResult);
            if (ownershipResults.isUserWasRemoved()) {
                membershipService.updateLastModified(groupingPath);
                membershipService.updateLastModified(groupingPath + GroupType.OWNERS.value());
            }
            removeMemberResultList.add(ownershipResults);
        }
        return removeMemberResultList;
    }

    /**
     * Gives ownership to a single or multiple users.
     */
    public List<UIAddMemberResults> addOwnerships(String groupingPath, String ownerUsername, List<String> ownersToAdd) {
        logger.info("assignOwnership; groupingPath: "
                + groupingPath
                + "; ownerUsername: "
                + ownerUsername
                + "; newOwnerUsername: "
                + ownersToAdd
                + ";");
        List<UIAddMemberResults> addOwnerResults = new ArrayList<>();
        WsSubjectLookup wsSubjectLookup = grouperApiService.subjectLookup(ownerUsername);
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService
                .isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        for (String ownerToAdd : ownersToAdd) {
            UIAddMemberResults addOwnerResult;
            AddMemberResult addMemberResult =
                    grouperApiService.addMember(groupingPath + GroupType.OWNERS.value(), ownerToAdd);
            addOwnerResult = new UIAddMemberResults(addMemberResult);
            if (addOwnerResult.isUserWasAdded()) {
                membershipService.updateLastModified(groupingPath);
                membershipService.updateLastModified(groupingPath + GroupType.OWNERS.value());
            }
            addOwnerResults.add(addOwnerResult);
        }
        return addOwnerResults;
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting in adds a member/user at
     * uid to the include list and removes them from the exclude list.
     */
    @Override public UIAddMemberResults optIn(String currentUser, String groupingPath, String uid) {
        logger.info("optIn; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        String removalPath = groupingPath + GroupType.EXCLUDE.value();
        String additionPath = groupingPath + GroupType.INCLUDE.value();
        return addMember(currentUser, uid, removalPath, additionPath);
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting out adds a member/user
     * at uid to the exclude list and removes them from the include list.
     */
    @Override public UIAddMemberResults optOut(String currentUser, String groupingPath, String uid) {
        logger.info("optOut; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        String removalPath = groupingPath + GroupType.INCLUDE.value();
        String additionPath = groupingPath + GroupType.EXCLUDE.value();
        return addMember(currentUser, uid, removalPath, additionPath);
    }

    /**
     * Remove a user from all groups listed in groupPaths
     */
    @Override
    public List<UIRemoveMemberResults> removeFromGroups(String adminUsername, String userToRemove,
            List<String> groupPaths) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<UIRemoveMemberResults> results = new ArrayList<>();
        for (String groupPath : groupPaths) {
            if (!groupPath.endsWith(GroupType.OWNERS.value())
                    && !groupPath.endsWith(GroupType.INCLUDE.value())
                    && !groupPath.endsWith(GroupType.EXCLUDE.value())) {
                throw new GcWebServiceError("404: Invalid group path.");
            }
            results.add(new UIRemoveMemberResults(grouperApiService.removeMember(groupPath, userToRemove)));
        }
        return results;
    }

    /**
     * Remove all uses from the include and/or exclude groups of grouping at path.
     */
    @Override
    public List<UIRemoveMemberResults> resetGroup(String currentUser, String path, List<String> uhNumbersInclude,
            List<String> uhNumbersExclude) {
        if (!memberAttributeService.isAdmin(currentUser) && !memberAttributeService.isOwner(path, currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<UIRemoveMemberResults> results = new ArrayList<>();
        if (!uhNumbersInclude.isEmpty()) {
            results.addAll(removeGroupMembers(currentUser, path + GroupType.INCLUDE.value(), uhNumbersInclude));
        }
        if (!uhNumbersExclude.isEmpty()) {
            results.addAll(removeGroupMembers(currentUser, path + GroupType.EXCLUDE.value(), uhNumbersExclude));
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

    /**
     * Adds the uid/uhUuid in userToAdd to the group at additionPath and removes userToAdd from the group at
     * removalPath. If the userToAdd is already in the group at additionPath, it does not get added.
     */
    public UIAddMemberResults addMember(String currentUser, String userToAdd, String removalPath, String additionPath) {
        UIAddMemberResults addMemberResult;
        try {
            RemoveMemberResult removeMemberResult = grouperApiService.removeMember(removalPath, userToAdd);
            AddMemberResult addMemberResponse = grouperApiService.addMember(additionPath, userToAdd);
            addMemberResult = new UIAddMemberResults(addMemberResponse, removeMemberResult);
            if (addMemberResult.isUserWasAdded()) {
                membershipService.updateLastModified(additionPath);
            }
            if (addMemberResult.isUserWasRemoved()) {
                membershipService.updateLastModified(removalPath);
            }
        } catch (AddMemberRequestRejectedException | RemoveMemberRequestRejectedException e) {
            addMemberResult = new UIAddMemberResults(userToAdd, FAILURE);
        }
        logger.info("addGroupMembers; " + addMemberResult);
        return addMemberResult;
    }
}