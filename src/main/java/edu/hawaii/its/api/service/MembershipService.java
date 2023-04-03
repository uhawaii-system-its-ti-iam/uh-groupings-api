package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;
import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.UIAddMemberResults;
import edu.hawaii.its.api.type.UIRemoveMemberResults;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.wrapper.AddMemberCommand;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.RemoveMemberCommand;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static edu.hawaii.its.api.service.PathFilter.disjoint;
import static edu.hawaii.its.api.service.PathFilter.nameGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasExclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

@Service("membershipService")
public class MembershipService {

    public static final Log logger = LogFactory.getLog(MembershipService.class);

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

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private GroupPathService groupPathService;

    /**
     * Add am admin.
     */
    public GroupingsAddResult addAdmin(String currentUser, String adminToAdd) {
        logger.info("addAdmin; username: " + currentUser + "; newAdmin: " + adminToAdd + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
        String validUhUuid = subjectService.getValidUhUuid(adminToAdd);
        return new GroupingsAddResult(new AddMemberCommand(GROUPING_ADMINS, validUhUuid).execute());
    }

    /**
     * Remove an admin.
     */
    public GroupingsRemoveResult removeAdmin(String currentUser, String adminToRemove) {
        logger.info("removeAdmin; username: " + currentUser + "; adminToRemove: " + adminToRemove + ";");

        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
        String validUhUuid = subjectService.getValidUhUuid(adminToRemove);
        return new GroupingsRemoveResult(new RemoveMemberCommand(GROUPING_ADMINS, validUhUuid).execute());
    }

    /**
     * Get a list of memberships pertaining to uid. A list of memberships is made up from the groups listings of
     * (basis + include) - exclude.
     */
    public List<Membership> membershipResults(String currentUser, String uid) {
        String action = "membershipResults; currentUser: " + currentUser + "; uid: " + uid + ";";
        logger.info(action);

        if (!memberAttributeService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException();
        }
        String uhUuid = subjectService.getValidUhUuid(uid);
        if (uhUuid.equals("")) {
            throw new UhMemberNotFoundException(uid);
        }
        // Get all basis, include and exclude paths from grouper.
        List<String> basisIncludeExcludePaths =
                groupingsService.groupPaths(uid, pathHasBasis().or(pathHasInclude().or(pathHasExclude())));
        // Get all basis and include paths to check the opt-out attribute.
        List<String> basisAndInclude =
                groupingsService.filterGroupPaths(basisIncludeExcludePaths, pathHasBasis().or(pathHasInclude()));
        // Get all exclude paths for the disjoint.
        List<String> excludePaths = groupingsService.filterGroupPaths(basisIncludeExcludePaths, pathHasExclude());
        // The disjoint of basis plus include and exclude: (Basis + Include) - Exclude
        List<String> groupingMembershipPaths = disjoint(parentGroupingPaths(basisIncludeExcludePaths),
                parentGroupingPaths(excludePaths));
        // Send all the grouping Membership paths to grouper to obtain grouping descriptions.
        List<Group> membershipGroupings = groupPathService.getValidGroupings(groupingMembershipPaths);
        // Get a list of groupings paths of all basis and include groups that have the opt-out attribute.
        List<String> optOutList = groupingsService.optOutEnabledGroupingPaths(parentGroupingPaths(basisAndInclude));
        return createMemberships(membershipGroupings, optOutList);
    }

    List<Membership> createMemberships(List<Group> membershipGroupings, List<String> optOutList) {
        List<Membership> memberships = new ArrayList<>();
        for (Group grouping : membershipGroupings) {
            Membership membership = new Membership();
            membership.setDescription(grouping.getDescription());
            membership.setPath(grouping.getGroupPath());
            membership.setName(nameGroupingPath(grouping.getGroupPath()));
            membership.setOptOutEnabled(optOutList.contains(grouping.getGroupPath()));
            memberships.add(membership);
        }
        return memberships;
    }

    /**
     * Get a list of all groupings pertaining to uid (nonfiltered).
     */
    public List<Membership> managePersonResults(String currentUser, String uid) {
        String action = "managePersonResults; currentUser: " + currentUser + "; uid: " + uid + ";";
        logger.info(action);

        if (!memberAttributeService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException();
        }
        List<Membership> memberships = new ArrayList<>();
        List<String> groupPaths;
        List<String> optOutList;
        try {
            groupPaths = groupingAssignmentService.getGroupPaths(currentUser, uid);
            optOutList = groupingAssignmentService.optableGroupings(OptType.OUT.value());
        } catch (GcWebServiceError e) {
            logger.warn("membershipResults;" + e);
            return memberships;
        }

        return createMembershipList(groupPaths, optOutList, memberships);
    }

    /**
     * Helper - membershipResults, managePersonResults
     */
    private List<Membership> createMembershipList(List<String> groupPaths, List<String> optOutList,
            List<Membership> memberships) {
        Map<String, List<String>> pathMap = new HashMap<>();

        for (String pathToCheck : groupPaths) {
            if (pathToCheck.endsWith(GroupType.INCLUDE.value())
                    || pathToCheck.endsWith(GroupType.EXCLUDE.value())
                    || pathToCheck.endsWith(GroupType.BASIS.value())
                    || pathToCheck.endsWith(GroupType.OWNERS.value())) {
                String parentPath = parentGroupingPath(pathToCheck);
                if (!pathMap.containsKey(parentPath)) {
                    pathMap.put(parentPath, new ArrayList<>());
                }
                pathMap.get(parentPath).add(pathToCheck);
            }
        }

        List<Group> groupingMemberships = groupPathService.getValidGroupings(new ArrayList<>(pathMap.keySet()));

        for (Group group : groupingMemberships) {
            String groupingPath = group.getGroupPath();
            List<String> paths = pathMap.get(groupingPath);
            Membership membership = subgroups(paths);
            membership.setPath(groupingPath);
            membership.setOptOutEnabled(optOutList.contains(groupingPath));
            membership.setName(nameGroupingPath(groupingPath));
            membership.setDescription(group.getDescription());
            memberships.add(membership);
        }
        return memberships;
    }

    /**
     * Helper - membershipResults, managePersonResults
     */
    private Membership subgroups(List<String> paths) {
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
        return membership;
    }

    /**
     * Add all uids/uhUuids contained in list usersToAdd to the group at groupPath. When adding to the include group
     * members which already exist in the exclude group will be removed from the exclude group and visa-versa. This
     * method was designed to add new members to the include and exclude groups only. Upon passing group paths other than
     * include or exclude, addGroupMembers will return empty list.
     */
    public List<UIAddMemberResults> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd) {
        logger.info("addGroupMembers; currentUser: " + currentUser + "; groupPath: " + groupPath + ";"
                + "usersToAdd: " + usersToAdd + ";");

        List<UIAddMemberResults> addMemberResults = new ArrayList<>();
        String removalPath = parentGroupingPath(groupPath);

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
    public List<UIAddMemberResults> addIncludeMembers(String currentUser, String groupingPath,
            List<String> usersToAdd) {
        logger.info("addIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException();
        }
        return addGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(), usersToAdd);
    }

    /**
     * Check if the currentUser has the proper privileges then call addGroupMembers.
     */
    public List<UIAddMemberResults> addExcludeMembers(String currentUser, String groupingPath,
            List<String> usersToAdd) {
        logger.info("addExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToAdd: " + usersToAdd + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException();
        }
        return addGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(), usersToAdd);
    }

    /**
     * Remove all the members in list usersToRemove from group at groupPath. This method was designed to remove members
     * from the include and exclude groups only. Passing in other group paths will result in undefined behavior.
     */
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
                updateLastModified(groupPath);
            }
            removeMemberResults.add(removeMemberResult);
            logger.info("removeGroupMembers; " + removeMemberResult.toString());
        }
        return removeMemberResults;
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    public List<UIRemoveMemberResults> removeIncludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeIncludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException();
        }
        return removeGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(), usersToRemove);
    }

    /**
     * Check if the currentUser has the proper privileges then call removeGroupMembers.
     */
    public List<UIRemoveMemberResults> removeExcludeMembers(String currentUser, String groupingPath,
            List<String> usersToRemove) {
        logger.info("removeExcludeMembers; currentUser: " + currentUser +
                "; groupingPath: " + groupingPath + "; usersToRemove: " + usersToRemove + ";");
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException();
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
            throw new AccessDeniedException();
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
                updateLastModified(groupingPath);
                updateLastModified(groupingPath + GroupType.OWNERS.value());
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
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService
                .isAdmin(ownerUsername)) {
            throw new AccessDeniedException();
        }

        UIAddMemberResults addOwnerResult;
        for (String ownerToAdd : ownersToAdd) {
            AddMemberResult addMemberResult =
                    grouperApiService.addMember(groupingPath + GroupType.OWNERS.value(), ownerToAdd);
            addOwnerResult = new UIAddMemberResults(addMemberResult);
            if (addOwnerResult.isUserWasAdded()) {
                updateLastModified(groupingPath);
                updateLastModified(groupingPath + GroupType.OWNERS.value());
            }
            addOwnerResults.add(addOwnerResult);
        }
        return addOwnerResults;
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting in adds a member/user at
     * uid to the include list and removes them from the exclude list.
     */
    public UIAddMemberResults optIn(String currentUser, String groupingPath, String uid) {
        logger.info("optIn; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }

        String removalPath = groupingPath + GroupType.EXCLUDE.value();
        String additionPath = groupingPath + GroupType.INCLUDE.value();
        return addMember(currentUser, uid, removalPath, additionPath);
    }

    /**
     * Check if the currentUser has the proper privileges to opt, then call addGroupMembers. Opting out adds a member/user
     * at uid to the exclude list and removes them from the include list.
     */
    public UIAddMemberResults optOut(String currentUser, String groupingPath, String uid) {
        logger.info("optOut; currentUser: " + currentUser + "; groupingPath: " + groupingPath + "; uid: " + uid + ";");
        if (!currentUser.equals(uid) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }

        String removalPath = groupingPath + GroupType.INCLUDE.value();
        String additionPath = groupingPath + GroupType.EXCLUDE.value();
        return addMember(currentUser, uid, removalPath, additionPath);
    }

    /**
     * Remove a user from all groups listed in groupPaths
     */
    public List<UIRemoveMemberResults> removeFromGroups(String adminUsername, String userToRemove,
            List<String> groupPaths) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException();
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
    public List<UIRemoveMemberResults> resetGroup(String currentUser, String path, List<String> uhNumbersInclude,
            List<String> uhNumbersExclude) {
        if (!memberAttributeService.isAdmin(currentUser) && !memberAttributeService.isOwner(path, currentUser)) {
            throw new AccessDeniedException();
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
    public UpdateTimestampResult updateLastModified(String groupPath) {
        logger.info("updateLastModified; group: " + groupPath + ";");
        String dateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        return updateLastModifiedTimestamp(dateTime, groupPath);
    }

    /**
     * Update the last modified attribute of a group to dateTime.
     */
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
    public Integer numberOfMemberships(String currentUser, String uid) {
        return membershipResults(currentUser, uid).size();
    }

    /**
     * Helper - numberOfMemberships
     */
    private List<String> fetchGroupPaths(String currentUser, String uid, Predicate<String> predicate) {
        List<String> groupPaths = new ArrayList<>();
        try {
            groupPaths = groupingAssignmentService.getGroupPaths(currentUser, uid, predicate);
        } catch (GcWebServiceError e) {
            // Returning empty list for an expected error
        }
        return groupPaths;
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
                updateLastModified(additionPath);
            }
            if (addMemberResult.isUserWasRemoved()) {
                updateLastModified(removalPath);
            }
        } catch (AddMemberRequestRejectedException | RemoveMemberRequestRejectedException e) {
            addMemberResult = new UIAddMemberResults(userToAdd, FAILURE);
        }
        logger.info("addGroupMembers; " + addMemberResult);
        return addMemberResult;
    }
}
