package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service("membershipService")
public class MembershipServiceImpl implements MembershipService {

    @Value("${groupings.api.settings}")
    private String SETTINGS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.grouping_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_superusers}")
    private String GROUPING_SUPERUSERS;

    @Value("${groupings.api.attributes}")
    private String ATTRIBUTES;

    @Value("${groupings.api.for_groups}")
    private String FOR_GROUPS;

    @Value("${groupings.api.for_memberships}")
    private String FOR_MEMBERSHIPS;

    @Value("${groupings.api.last_modified}")
    private String LAST_MODIFIED;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.uhgrouping}")
    private String UHGROUPING;

    @Value("${groupings.api.destinations}")
    private String DESTINATIONS;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.purge_grouping}")
    private String PURGE_GROUPING;

    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    @Value("${groupings.api.anyone_can}")
    private String ANYONE_CAN;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.assign_type_immediate_membership}")
    private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;

    @Value("${groupings.api.subject_attribute_name_uhuuid}")
    private String SUBJECT_ATTRIBUTE_NAME_UID;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.privilege_opt_out}")
    private String PRIVILEGE_OPT_OUT;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

    @Value("${groupings.api.is_member}")
    private String IS_MEMBER;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.success_allowed}")
    private String SUCCESS_ALLOWED;

    @Value("${groupings.api.stem}")
    private String STEM;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    private GrouperFactoryService grouperFS;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private GroupingsMailService groupingsMailService;

    public static final Log logger = LogFactory.getLog(MembershipServiceImpl.class);

    // returns true if username is a UH id number
    public boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }

    /**
     * Get a list of memberships pertaining to uid.
     */
    @Override public List<Membership> getMembershipResults(String owner, String uid) {
        String action = "getMembershipResults; owner: " + owner + "; uid: " + uid + ";";
        logger.info(action);

        if (!memberAttributeService.isAdmin(owner) && !owner.equals(uid)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<Membership> memberships = new ArrayList<>();
        List<String> groupPaths;
        List<String> optOutList;
        try {
            groupPaths = groupingAssignmentService.getGroupPaths(owner, uid);
            optOutList = groupingAssignmentService.getOptOutGroups(owner, uid);
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
        WsSubjectLookup wsSubjectLookup = grouperFS.makeWsSubjectLookup(currentUser);
        String removalPath = helperService.parentGroupingPath(groupPath);

        if (groupPath.endsWith(INCLUDE)) {
            removalPath += EXCLUDE;
        } else if (groupPath.endsWith(EXCLUDE)) {
            removalPath += INCLUDE;
        } else {
            throw new GcWebServiceError("404: Invalid group path.");
        }

        for (String userToAdd : usersToAdd) {
            WsDeleteMemberResults wsDeleteMemberResults;
            WsAddMemberResults wsAddMemberResults;
            AddMemberResult addMemberResult;
            boolean wasRemoved;
            boolean wasAdded;
            String uhUuid;
            String name;
            String uid;
            try {
                // Remove.
                wsDeleteMemberResults = grouperFS.makeWsDeleteMemberResults(removalPath, wsSubjectLookup, userToAdd);
                // Add.
                wsAddMemberResults = grouperFS.makeWsAddMemberResults(groupPath, wsSubjectLookup, userToAdd);
                // Store results.
                wasRemoved = SUCCESS.equals(wsDeleteMemberResults.getResults()[0].getResultMetadata().getResultCode());
                wasAdded = SUCCESS.equals(wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode());
                uhUuid = wsAddMemberResults.getResults()[0].getWsSubject().getId();
                name = wsAddMemberResults.getResults()[0].getWsSubject().getName();
                uid = wsAddMemberResults.getResults()[0].getWsSubject().getIdentifierLookup();
                if (wasAdded) {
                    membershipService.updateLastModified(groupPath);
                }
                addMemberResult = new AddMemberResult(
                        wasAdded, wasRemoved, groupPath, removalPath, name, uhUuid, uid, SUCCESS, userToAdd);
                addMemberResults.add(addMemberResult);

            } catch (GcWebServiceError | NullPointerException e) {
                addMemberResult = new AddMemberResult(userToAdd, FAILURE);
                addMemberResults.add(addMemberResult);
            }
            logger.info("addGroupMembers; " + addMemberResult.toString());
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
        if (!memberAttributeService.isOwner(groupingPath, currentUser)) {
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
        if (!memberAttributeService.isOwner(groupingPath, currentUser)) {
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
        WsSubjectLookup wsSubjectLookup = grouperFS.makeWsSubjectLookup(currentUser);
        for (String userToRemove : usersToRemove) {
            WsDeleteMemberResults wsDeleteMemberResults;
            RemoveMemberResult removeMemberResult;
            boolean wasRemoved;
            String uhUuid;
            String result;
            String name;
            String uid;
            try {
                // Remove.
                wsDeleteMemberResults = grouperFS.makeWsDeleteMemberResults(groupPath, wsSubjectLookup, userToRemove);
                // Store results.
                wasRemoved = SUCCESS.equals(wsDeleteMemberResults.getResults()[0].getResultMetadata().getResultCode());
                uhUuid = wsDeleteMemberResults.getResults()[0].getWsSubject().getId();
                result = wasRemoved ? SUCCESS : FAILURE;
                name = wsDeleteMemberResults.getResults()[0].getWsSubject().getName();
                uid = wsDeleteMemberResults.getResults()[0].getWsSubject().getIdentifierLookup();
                if (wasRemoved) {
                    membershipService.updateLastModified(groupPath);
                }
                removeMemberResult = new RemoveMemberResult(
                        wasRemoved, groupPath, name, uhUuid, uid, result, userToRemove);
                removeMemberResults.add(removeMemberResult);

            } catch (GcWebServiceError | NullPointerException e) {
                removeMemberResult = new RemoveMemberResult(userToRemove, FAILURE);
                removeMemberResults.add(removeMemberResult);
            }
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
        if (!memberAttributeService.isOwner(groupingPath, currentUser)) {
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
        if (!memberAttributeService.isOwner(groupingPath, currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return removeGroupMembers(currentUser, groupingPath + EXCLUDE, usersToRemove);
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

    //adds a user to the admins group via username or UH id number
    @Override
    public GroupingsServiceResult addAdmin(String currentAdminUsername, String newAdminUsername) {
        logger.info("addAdmin; username: " + currentAdminUsername + "; newAdmin: " + newAdminUsername + ";");

        String action = "add " + newAdminUsername + " to " + GROUPING_ADMINS;

        if (memberAttributeService.isAdmin(currentAdminUsername)) {
            if (memberAttributeService.isAdmin(newAdminUsername)) {
                return helperService.makeGroupingsServiceResult(
                        SUCCESS + ": " + newAdminUsername + " was already in" + GROUPING_ADMINS, action);
            }
            WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(
                    GROUPING_ADMINS,
                    newAdminUsername);

            return helperService.makeGroupingsServiceResult(addMemberResults, action);
        }

        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    //removes a user from the admins group
    @Override
    public GroupingsServiceResult deleteAdmin(String adminUsername, String adminToDeleteUsername) {
        logger.info("deleteAdmin; username: " + adminUsername + "; adminToDelete: " + adminToDeleteUsername + ";");

        String action;
        action = "delete " + adminToDeleteUsername + " from " + GROUPING_ADMINS;

        if (memberAttributeService.isAdmin(adminUsername)) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(adminUsername);

            WsDeleteMemberResults deleteMemberResults = grouperFS.makeWsDeleteMemberResults(
                    GROUPING_ADMINS,
                    user,
                    adminToDeleteUsername);

            return helperService.makeGroupingsServiceResult(deleteMemberResults, action);
        }

        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    @Override
    public List<GroupingsServiceResult> removeFromGroups(String adminUsername, String userToRemove,
            List<String> GroupPaths) {
        List<GroupingsServiceResult> result = new ArrayList<GroupingsServiceResult>();
        List<WsDeleteMemberResults> deleteMemberResults =
                makeWsBatchDeleteMemberResults(GroupPaths, userToRemove);
        for (int i = 0; i < deleteMemberResults.size(); i++) {
            logger.info("Removing " + userToRemove + " from Group " + i + ":" + GroupPaths.get(i));
            String action = "delete " + userToRemove + " from " + GroupPaths.get(i);
            result.add(helperService.makeGroupingsServiceResult(deleteMemberResults.get(i), action));
        }
        return result;
    }

    @Override
    public List<GroupingsServiceResult> resetGroup(String currentUser, String path,
            List<String> includeIdentifier, List<String> excludeIdentifier) {

        List<GroupingsServiceResult> result = new ArrayList<GroupingsServiceResult>();
        String excludePath = path + EXCLUDE;
        String includePath = path + INCLUDE;

        if (!includeIdentifier.get(0).equals("empty")) {
            for (int i = 0; i < includeIdentifier.size(); i++) {
                logger.info("Removing " + includeIdentifier.get(i) + " from Group " + i + ":" + includePath);
                String action = "delete " + includeIdentifier.get(i) + " from " + includePath;
                WsSubjectLookup ownerLookup = grouperFS.makeWsSubjectLookup(currentUser);
                WsDeleteMemberResults deleteMemberResults =
                        grouperFS.makeWsDeleteMemberResults(includePath, ownerLookup, includeIdentifier.get(i));
                result.add(helperService.makeGroupingsServiceResult(deleteMemberResults, action));
            }
        }
        if (!excludeIdentifier.get(0).equals("empty")) {
            for (int i = 0; i < excludeIdentifier.size(); i++) {
                logger.info("Removing " + excludeIdentifier.get(i) + " from Group " + i + ":" + excludePath);
                String action = "delete " + excludeIdentifier.get(i) + " from " + excludePath;
                WsSubjectLookup ownerLookup = grouperFS.makeWsSubjectLookup(currentUser);
                WsDeleteMemberResults deleteMemberResults =
                        grouperFS.makeWsDeleteMemberResults(excludePath, ownerLookup, excludeIdentifier.get(i));
                result.add(helperService.makeGroupingsServiceResult(deleteMemberResults, action));
            }
        }

        return result;
    }

    //returns true if the group allows that user to opt in
    @Override
    public boolean isGroupCanOptIn(String optInUsername, String groupPath) {
        logger.info("groupOptInPermission; group: " + groupPath + "; username: " + optInUsername + ";");

        WsGetGrouperPrivilegesLiteResult result = getGrouperPrivilege(optInUsername, PRIVILEGE_OPT_IN, groupPath);

        return result
                .getResultMetadata()
                .getResultCode()
                .equals(SUCCESS_ALLOWED);
    }

    //returns true if the group allows that user to opt out
    @Override
    public boolean isGroupCanOptOut(String optOutUsername, String groupPath) {
        logger.info("groupOptOutPermission; group: " + groupPath + "; username: " + optOutUsername + ";");
        WsGetGrouperPrivilegesLiteResult result = getGrouperPrivilege(optOutUsername, PRIVILEGE_OPT_OUT, groupPath);

        return result
                .getResultMetadata()
                .getResultCode()
                .equals(SUCCESS_ALLOWED);
    }

    //adds the self-opted attribute to the membership between the group and user
    @Override
    public GroupingsServiceResult addSelfOpted(String groupPath, String username) {
        logger.info("addSelfOpted; group: " + groupPath + "; username: " + username + ";");

        String action = "add self-opted attribute to the membership of " + username + " to " + groupPath;

        if (memberAttributeService.isMember(groupPath, username)) {
            if (!memberAttributeService.isSelfOpted(groupPath, username)) {
                WsGetMembershipsResults includeMembershipsResults =
                        helperService.membershipsResults(username, groupPath);

                String membershipID = helperService.extractFirstMembershipID(includeMembershipsResults);

                return helperService.makeGroupingsServiceResult(
                        assignMembershipAttributes(OPERATION_ASSIGN_ATTRIBUTE, SELF_OPTED, membershipID),
                        action);
            }
            return helperService.makeGroupingsServiceResult(
                    SUCCESS + ", " + username + " was already self opted into " + groupPath,
                    action);
        }
        return helperService.makeGroupingsServiceResult(
                FAILURE + ", " + username + " is not a member of " + groupPath,
                action);
    }

    //removes the self-opted attribute from the membership that corresponds to the user and group
    @Override
    public GroupingsServiceResult removeSelfOpted(String groupPath, String username) {
        logger.info("removeSelfOpted; group: " + groupPath + "; username: " + username + ";");

        String action = "remove self-opted attribute from the membership of " + username + " to " + groupPath;

        if (memberAttributeService.isMember(groupPath, username)) {
            if (memberAttributeService.isSelfOpted(groupPath, username)) {
                WsGetMembershipsResults membershipsResults = helperService.membershipsResults(username, groupPath);
                String membershipID = helperService.extractFirstMembershipID(membershipsResults);

                return helperService.makeGroupingsServiceResult(
                        assignMembershipAttributes(OPERATION_REMOVE_ATTRIBUTE, SELF_OPTED, membershipID),
                        action);
            }
            return helperService.makeGroupingsServiceResult(
                    SUCCESS + ", " + username + " was not self-opted into " + groupPath,
                    action);
        }
        return helperService.makeGroupingsServiceResult(
                FAILURE + ", " + username + " is not a member of " + groupPath,
                action);
    }

    // Get the number of memberships the current user has
    @Override public Integer getNumberOfMemberships(String currentUser, String uid) {
        return getMembershipResults(currentUser, uid).size();
    }

    //updates the last modified attribute of the group to the current date and time
    @Override
    public GroupingsServiceResult updateLastModified(String groupPath) {
        logger.info("updateLastModified; group: " + groupPath + ";");
        String time = wsDateTime();
        WsAttributeAssignValue dateTimeValue = grouperFS.makeWsAttributeAssignValue(time);

        WsAssignAttributesResults assignAttributesResults = grouperFS.makeWsAssignAttributesResults(
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                groupPath,
                YYYYMMDDTHHMM,
                OPERATION_REPLACE_VALUES,
                dateTimeValue);

        return helperService.makeGroupingsServiceResult(assignAttributesResults,
                "update last-modified attribute for " + groupPath + " to time " + time);

    }

    //checks to see if the user has the privilege in that group
    public WsGetGrouperPrivilegesLiteResult getGrouperPrivilege(String username, String privilegeName,
            String groupPath) {
        logger.info("getGrouperPrivilege; username: "
                + username
                + "; group: "
                + groupPath
                + "; privilegeName: "
                + privilegeName
                + ";");

        WsSubjectLookup lookup = grouperFS.makeWsSubjectLookup(username);

        return grouperFS.makeWsGetGrouperPrivilegesLiteResult(groupPath, privilegeName, lookup);
    }

    /*
     * @return date and time in yyyymmddThhmm format
     * ex. 20170314T0923
     */
    public String wsDateTime() {
        return Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
    }

    //adds, removes, updates (operationName) the attribute for the membership
    public WsAssignAttributesResults assignMembershipAttributes(String operationName, String attributeUuid,
            String membershipID) {
        logger.info("assignMembershipAttributes; operation: "
                + operationName
                + "; uhUuid: "
                + attributeUuid
                + "; membershipID: "
                + membershipID
                + ";");

        return grouperFS.makeWsAssignAttributesResultsForMembership(ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP, operationName,
                attributeUuid, membershipID);
    }

    public void setGrouperFactoryService(GrouperFactoryService grouperFactoryService) {
        this.grouperFS = grouperFactoryService;
    }

    public void setMemberAttributeService(MemberAttributeService memberAttributeService) {
        this.memberAttributeService = memberAttributeService;
    }

    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    public List<WsDeleteMemberResults> makeWsBatchDeleteMemberResults(List<String> groupPaths, String userToRemove) {
        // Creating a thread list which is populated with a thread for each removal that needs to be done.
        List<WsDeleteMemberResults> results = new ArrayList<>();
        List<Callable<WsDeleteMemberResults>> threads = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(groupPaths.size());
        for (int currGroup = 0; currGroup < groupPaths.size(); currGroup++) {
            //creating runnable object containing the data needed for each individual delete.
            Callable<WsDeleteMemberResults> master =
                    new BatchDeleterTask(userToRemove, groupPaths.get(currGroup), grouperFS);
            threads.add(master);
        }
        // Starting all of the created threads.
        List<Future<WsDeleteMemberResults>> futures = null;
        try {
            futures = executor.invokeAll(threads);
        } catch (InterruptedException e) {
            logger.info("Executor Interrupted: " + e);
        }
        // Waiting to return result until every thread in the list has completed running.
        for (Future future : futures) {
            try {
                results.add((WsDeleteMemberResults) future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.info("Thread Interrupted: " + e);
            }
        }
        // Shuts down the service once all threads have completed.
        executor.shutdown();
        return results;
    }

}
