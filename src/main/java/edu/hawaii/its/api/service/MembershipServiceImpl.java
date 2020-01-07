package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static final Log logger = LogFactory.getLog(MembershipServiceImpl.class);

    // returns true if username is a UH id number
    public boolean isUhUuid(String naming) {
        return naming.matches("\\d+");
    }

    // creates/returns a user depending on the input used. for example, if input is UhUuid, user will be created using that
    public Person createNewPerson(String userToAdd) {
        List<GroupingsServiceResult> gsrs;
        Person createdPerson;

        if (isUhUuid(userToAdd)) {
            return createdPerson = new Person(null, null, userToAdd);
        } else {
            return createdPerson = new Person(null, userToAdd, null);
        }
    }

    @Override
    public List<GroupingsServiceResult> addGroupingMember(String ownerUsername, String groupingPath, String userToAdd) {
        List<GroupingsServiceResult> gsrs;

        try {
            Integer.parseInt(userToAdd);
            gsrs = addGroupingMemberByUhUuid(ownerUsername, groupingPath, userToAdd);

        } catch (Exception NumberFormatException) {
            gsrs = addGroupingMemberByUsername(ownerUsername, groupingPath, userToAdd);

        }

        return gsrs;
    }

    //finds a user by a username and adds them to a grouping
    @Override
    public List<GroupingsServiceResult> addGroupingMemberByUsername(String ownerUsername, String groupingPath,
            String userToAddUsername) {
        logger.info(
                "addGroupingMemberByUsername; user: " + ownerUsername + "; group: " + groupingPath + "; usersToAdd: "
                        + userToAddUsername + ";");

        List<GroupingsServiceResult> gsrs = new ArrayList<>();

        String action = "add user to " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        // Grouper
        boolean isInBasis = memberAttributeService.isMember(basis, userToAddUsername);
        boolean isInComposite = memberAttributeService.isMember(groupingPath, userToAddUsername);
        boolean isInInclude = memberAttributeService.isMember(include, userToAddUsername);

        // check to see if they are already in the grouping
        if (!isInComposite) {
            // get them out of the exclude
            gsrs.add(deleteGroupMemberByUsername(ownerUsername, exclude, userToAddUsername));
            // only add them to the include if they are not in the basis
            if (!isInBasis) {
                gsrs.addAll(addGroupMember(ownerUsername, include, userToAddUsername));
            } else {
                gsrs.add(helperService
                    .makeGroupingsServiceResult(SUCCESS + ": " + userToAddUsername + " was in " + basis,
                        action));
            }
        } else {
            gsrs.add(helperService.makeGroupingsServiceResult(
                    SUCCESS + ": " + userToAddUsername + " was already in " + groupingPath, action));
        }
        // should only be in one or the other
        if (isInBasis && isInInclude) {
            gsrs.add(deleteGroupMemberByUsername(ownerUsername, include, userToAddUsername));
        }

        return gsrs;
    }

    //find a user by a uuid and add them to a grouping
    @Override
    public List<GroupingsServiceResult> addGroupingMemberByUhUuid(String username, String groupingPath,
            String userToAddUhUuid) {
        logger.info("addGroupingMemberByUuid; user: " + username + "; grouping: " + groupingPath + "; userToAdd: "
                + userToAddUhUuid + ";");

        List<GroupingsServiceResult> gsrs = new ArrayList<>();

        String action = "add user to " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        Person personToAdd = new Person(null, userToAddUhUuid, null);

        boolean isInBasis = memberAttributeService.isMember(basis, personToAdd);
        boolean isInComposite = memberAttributeService.isMember(groupingPath, personToAdd);
        boolean isInInclude = memberAttributeService.isMember(include, personToAdd);

        //check to see if they are already in the grouping
        if (!isInComposite) {
            //get them out of the exclude
            gsrs.add(deleteGroupMemberByUsername(username, exclude, userToAddUhUuid));
            //only add them to the include if they are not in the basis
            if (!isInBasis) {
                gsrs.addAll(addGroupMember(username, include, userToAddUhUuid));
            } else {
                gsrs.add(helperService
                        .makeGroupingsServiceResult(SUCCESS + ": " + userToAddUhUuid + " was in " + basis, action));
            }
        } else {
            gsrs.add(helperService
                    .makeGroupingsServiceResult(SUCCESS + ": " + userToAddUhUuid + " was already in " + groupingPath,
                            action));
        }
        //should only be in one or the other
        if (isInBasis && isInInclude) {
            gsrs.add(deleteGroupMemberByUsername(username, include, userToAddUhUuid));
        }

        return gsrs;
    }

    //find a user by a username and remove them from the grouping
    @Override
    public List<GroupingsServiceResult> deleteGroupingMemberByUsername(String ownerUsername, String groupingPath,
            String userToDeleteUsername) {
        logger.info("deleteGroupingMemberByUsername; username: "
                + ownerUsername
                + "; groupingPath: "
                + groupingPath + "; userToDelete: "
                + userToDeleteUsername
                + ";");

        List<GroupingsServiceResult> gsrList = new ArrayList<>();

        String action = ownerUsername + " deletes " + userToDeleteUsername + " from " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        boolean inBasis = memberAttributeService.isMember(basis, userToDeleteUsername);
        boolean inComposite = memberAttributeService.isMember(groupingPath, userToDeleteUsername);
        boolean inExclude = memberAttributeService.isMember(exclude, userToDeleteUsername);

        //if they are in the include group, get them out
        gsrList.add(deleteGroupMemberByUsername(ownerUsername, include, userToDeleteUsername));

        //make sure userToDelete is actually in the Grouping
        if (inComposite) {
            //if they are not in the include group, then they are in the basis, so add them to the exclude group
            if (inBasis) {
                gsrList.addAll(addGroupMember(ownerUsername, exclude, userToDeleteUsername));
            }
        }
        //since they are not in the Grouping, do nothing, but return SUCCESS
        else {
            gsrList.add(helperService
                    .makeGroupingsServiceResult(SUCCESS + userToDeleteUsername + " was not in " + groupingPath,
                            action));
        }

        //should not be in exclude if not in basis
        if (!inBasis && inExclude) {
            gsrList.add(deleteGroupMemberByUsername(ownerUsername, exclude, userToDeleteUsername));
        }

        return gsrList;
    }

    //finds a user by a uhUuid and remove them from a grouping
    public List<GroupingsServiceResult> deleteGroupingMemberByUhUuid(String ownerUsername, String groupingPath,
            String userToDeleteUhUuid) {
        logger.info("deleteGroupingMemberByUuid; ownerUsername: "
                + ownerUsername
                + "; groupingPath: "
                + groupingPath + "; userToDelete: "
                + userToDeleteUhUuid
                + ";");

        List<GroupingsServiceResult> gsrList = new ArrayList<>();

        String action = ownerUsername + " deletes " + userToDeleteUhUuid + " from " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        Person personToDelete = new Person(null, userToDeleteUhUuid, null);

        boolean isInBasis = memberAttributeService.isMember(basis, personToDelete);
        boolean isInComposite = memberAttributeService.isMember(groupingPath, personToDelete);
        boolean isInExclude = memberAttributeService.isMember(exclude, personToDelete);

        //if they are in the include group, get them out
        gsrList.add(deleteGroupMemberByUsername(ownerUsername, include, userToDeleteUhUuid));

        //make sure userToDelete is actually in the Grouping
        if (isInComposite) {
            //if they are not in the include group, then they are in the basis, so add them to the exclude group
            if (isInBasis) {
                gsrList.addAll(addGroupMember(ownerUsername, exclude, userToDeleteUhUuid));
            }
        }
        //since they are not in the Grouping, do nothing, but return SUCCESS
        else {
            gsrList.add(
                    helperService.makeGroupingsServiceResult(SUCCESS + userToDeleteUhUuid + " was not in " +
                            groupingPath, action));
        }

        //should not be in exclude if not in basis
        if (!isInBasis && isInExclude) {
            gsrList.add(deleteGroupMemberByUsername(ownerUsername, exclude, userToDeleteUhUuid));
        }

        return gsrList;
    }

    // Takes an admin and user and returns list of groups that a user may own. An empty list will be returned if no
    // groups are owned.
    @Override
    public List<String> listOwned(String admin, String user) {
        List<String> groupsOwned = new ArrayList<>();

        if (memberAttributeService.isSuperuser(admin)) {
            List<Grouping> groups =
                    groupingAssignmentService.groupingsOwned(groupingAssignmentService.getGroupPaths(admin, user));

            for (Grouping group : groups) {
                groupsOwned.add(group.getPath());

            }
            return groupsOwned;
        }
        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    //Takes the owner of the group, the path to the group, and a list of users to add. Goes through the list and adds
    //each user to the specified group
    @Override
    public List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupPath,
            List<String> usersToAdd) {
        List<GroupingsServiceResult> gsrs = new ArrayList<>();

        for (String userToAdd : usersToAdd) {
            try {
                Integer.parseInt(userToAdd);
                gsrs = addGroupMember(ownerUsername, groupPath, userToAdd);

            } catch (Exception NumberFormatException) {
                try {
                    gsrs = addGroupMember(ownerUsername, groupPath, userToAdd);
                } catch (GcWebServiceError e) {

                }
            }
        }

        return gsrs;
    }

    //finds a user by a username and adds that user to the group
    @Override
    public List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupPath,
            String userIdentifier) {
        logger.info("addGroupMember; user: " + ownerUsername + "; groupPath: " + groupPath + "; userToAdd: "
                + userIdentifier + ";");

      return addMemberHelper(ownerUsername, groupPath, createNewPerson(userIdentifier));
    }

    //finds all the user from a list of usernames and adds them to the group
    @Override
    public List<GroupingsServiceResult> addGroupMembersByUsername(String ownerUsername, String groupPath,
            List<String> usernamesToAdd) {
        logger.info(
                "addGroupMembersByUsername; user: " + ownerUsername + "; group: " + groupPath + "; usersToAddUsername: "
                        + usernamesToAdd + ";");
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        for (String userToAdd : usernamesToAdd) {
            gsrList.addAll(addGroupMember(ownerUsername, groupPath, userToAdd));
        }
        return gsrList;
    }

    //finds all the user from a list of uhUuids and adds them to the group
    @Override
    public List<GroupingsServiceResult> addGroupMembersByUhUuid(String ownerUsername, String groupPath,
            List<String> usersToAddUhUuid) {
        logger.info("addGroupMembersByUuid; user: " + ownerUsername + "; groupPath: " + groupPath + "; usersToAddUuid: "
                + usersToAddUhUuid + ";");
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        for (String userToAdd : usersToAddUhUuid) {
            gsrList.addAll(addGroupMemberByUhUuid(ownerUsername, groupPath, userToAdd));
        }
        return gsrList;
    }

    @Override
    public GroupingsServiceResult deleteGroupMember(String ownerUsername, String groupPath,
            String userToDelete) {
        if (isUhUuid(userToDelete)) {
            return deleteGroupMemberByUhUuid(ownerUsername, groupPath, userToDelete);
        }
        return deleteGroupMemberByUsername(ownerUsername, groupPath, userToDelete);
    }

    //find a user by a username and remove them from a group
    @Override
    public GroupingsServiceResult deleteGroupMemberByUsername(String ownerUsername, String groupPath,
            String userToDeleteUsername) {
        logger.info("deleteGroupMemberByUsername; user: " + ownerUsername
                + "; group: " + groupPath
                + "; userToDelete: " + userToDeleteUsername
                + ";");

        if (isUhUuid(userToDeleteUsername)) {
            return deleteGroupMemberByUhUuid(ownerUsername, groupPath, userToDeleteUsername);
        }

        String action = "delete " + userToDeleteUsername + " from " + groupPath;

        String composite = helperService.parentGroupingPath(groupPath);

        if (memberAttributeService.isSuperuser(ownerUsername) || memberAttributeService
                .isOwner(composite, ownerUsername) || userToDeleteUsername
                .equals(ownerUsername)) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(ownerUsername);
            if (groupPath.endsWith(EXCLUDE) || groupPath.endsWith(INCLUDE) || groupPath.endsWith(OWNERS)) {
                if (memberAttributeService.isMember(groupPath, userToDeleteUsername)) {
                    WsDeleteMemberResults deleteMemberResults =
                            grouperFS.makeWsDeleteMemberResults(groupPath, user, userToDeleteUsername);

                    updateLastModified(composite);
                    updateLastModified(groupPath);
                    return helperService.makeGroupingsServiceResult(deleteMemberResults, action);
                }
                return helperService
                        .makeGroupingsServiceResult(SUCCESS + ": " + userToDeleteUsername + " was not in " + groupPath,
                                action);
            }
            return helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + ownerUsername + " may only delete from exclude, include or owner group", action);
        }
        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    public GroupingsServiceResult deleteGroupMemberByUhUuid(String ownerUsername, String groupPath,
            String userToDeleteUhUuid) {
        logger.info("deleteGroupMemberByUuid; user: " + ownerUsername
                + "; group: " + groupPath
                + "; userToDelete: " + userToDeleteUhUuid
                + ";");

        String action = "delete " + userToDeleteUhUuid + " from " + groupPath;
        Person personToDelete = new Person(null, userToDeleteUhUuid, null);

        String composite = helperService.parentGroupingPath(groupPath);

        if (memberAttributeService.isOwner(composite, ownerUsername) || memberAttributeService
                .isSuperuser(ownerUsername)) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(ownerUsername);
            if (groupPath.endsWith(EXCLUDE) || groupPath.endsWith(INCLUDE) || groupPath.endsWith(OWNERS)) {
                if (memberAttributeService.isMember(groupPath, personToDelete)) {
                    WsDeleteMemberResults deleteMemberResults =
                            grouperFS.makeWsDeleteMemberResults(groupPath, user, personToDelete);

                    updateLastModified(composite);
                    updateLastModified(groupPath);
                    return helperService.makeGroupingsServiceResult(deleteMemberResults, action);
                }
                return helperService
                        .makeGroupingsServiceResult(SUCCESS + ": " + userToDeleteUhUuid + " was not in " + groupPath,
                                action);
            }
            return helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + ownerUsername + " may only delete from exclude, include or owner group", action);
        }
        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    //adds a user to the admins group via username or UH id number
    @Override
    public GroupingsServiceResult addAdmin(String currentAdminUsername, String newAdminUsername) {
        logger.info("addAdmin; username: " + currentAdminUsername + "; newAdmin: " + newAdminUsername + ";");

        String action = "add " + newAdminUsername + " to " + GROUPING_ADMINS;

        if (memberAttributeService.isUhUuid(newAdminUsername)) {
            action = "add user with uhUuid " + newAdminUsername + " to " + GROUPING_ADMINS;
            return new GroupingsServiceResult(FAILURE + ": adding admins with UHUUID is not implemented", action);
        }

        if (memberAttributeService.isSuperuser(currentAdminUsername)) {
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
        if (memberAttributeService.isUhUuid(adminToDeleteUsername)) {

            action = "delete user with uhUuid " + adminToDeleteUsername + " from " + GROUPING_ADMINS;
            return new GroupingsServiceResult(FAILURE + ": adding admins with UHUUID is not implemented", action);
        }

        if (memberAttributeService.isSuperuser(adminUsername)) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(adminUsername);

            WsDeleteMemberResults deleteMemberResults = grouperFS.makeWsDeleteMemberResults(
                    GROUPING_ADMINS,
                    user,
                    adminToDeleteUsername);

            return helperService.makeGroupingsServiceResult(deleteMemberResults, action);
        }

        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    //user adds them self to the group if they have permission
    @Override
    public List<GroupingsServiceResult> optIn(String optInUsername, String groupingPath) {
        String outOrrIn = "in ";
        String preposition = "to ";
        String addGroup = groupingPath + INCLUDE;

        return opt(optInUsername, groupingPath, addGroup, outOrrIn, preposition);
    }

    //user removes them self from the group if they have permission
    @Override
    public List<GroupingsServiceResult> optOut(String optOutUsername, String groupingPath) {
        String outOrrIn = "out ";
        String preposition = "from ";
        String addGroup = groupingPath + EXCLUDE;

        return opt(optOutUsername, groupingPath, addGroup, outOrrIn, preposition);
    }

    //user adds them self to the group if they have permission
    @Override
    public List<GroupingsServiceResult> optIn(String currentUser, String groupingPath, String uid) {
        String outOrrIn = "in ";
        String preposition = "to ";
        String addGroup = groupingPath + INCLUDE;

        if (currentUser.equals(uid) || memberAttributeService.isSuperuser(currentUser)) {
            return opt(uid, groupingPath, addGroup, outOrrIn, preposition);
        } else {
            GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult(
                    FAILURE + currentUser + " cannot opt " + uid + " into " + groupingPath,
                    currentUser + " opts " + uid + " into " + groupingPath);
            List<GroupingsServiceResult> list = new ArrayList<>();
            list.add(groupingsServiceResult);
            return list;
        }
    }

    //user removes them self from the group if they have permission
    @Override
    public List<GroupingsServiceResult> optOut(String currentUser, String groupingPath, String uid) {
        String outOrrIn = "out ";
        String preposition = "from ";
        String addGroup = groupingPath + EXCLUDE;

        if (currentUser.equals(uid) || memberAttributeService.isSuperuser(currentUser)) {
            return opt(uid, groupingPath, addGroup, outOrrIn, preposition);
        } else {
            GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult(
                    FAILURE + currentUser + " cannot opt " + uid + " out of " + groupingPath,
                    currentUser + " opts " + uid + " out of " + groupingPath);
            List<GroupingsServiceResult> list = new ArrayList<>();
            list.add(groupingsServiceResult);
            return list;
        }
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

    public List<GroupingsServiceResult> opt(String username, String grouping, String addGroup, String outOrrIn,
            String preposition) {

        List<GroupingsServiceResult> results = new ArrayList<>();

        if (isGroupCanOptIn(username, addGroup)) {

            switch (outOrrIn) {
                case "out ":
                    results.addAll(deleteGroupingMemberByUsername(username, grouping, username));
                    break;

                case "in ":
                    results.addAll(addGroupingMemberByUsername(username, grouping, username));
                    break;
            }

            if (memberAttributeService.isMember(addGroup, username)) {
                results.add(addSelfOpted(addGroup, username));
            }
        } else {

            String action = "opt " + outOrrIn + username + " " + preposition + grouping;
            String failureResult = FAILURE
                    + ": "
                    + username
                    + " does not have permission to opt "
                    + outOrrIn
                    + preposition
                    + grouping;
            results.add(helperService.makeGroupingsServiceResult(failureResult, action));
        }
        return results;
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

    //logic for adding a member
    public List<GroupingsServiceResult> addMemberHelper(String username, String groupPath, Person personToAdd) {
        logger.info(
                "addMemberHelper; user: " + username + "; group: " + groupPath + "; personToAdd: " + personToAdd + ";");

        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        String action = "add users to " + groupPath;

        if (memberAttributeService.isOwner(helperService.parentGroupingPath(groupPath), username)
                || memberAttributeService.isSuperuser(username) || (personToAdd.getUsername() != null && personToAdd
                .getUsername().equals(username))) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(username);
            String composite = helperService.parentGroupingPath(groupPath);
            String exclude = composite + EXCLUDE;
            String include = composite + INCLUDE;
            String owners = composite + OWNERS;

            boolean isCompositeUpdated = false;
            boolean isExcludeUpdated = false;
            boolean isIncludeUpdated = false;
            boolean isOwnersUpdated = false;

            //check to see if it is the include, exclude or owners
            if (groupPath.endsWith(INCLUDE)) {
                //if personToAdd is in exclude, get them out
                if (memberAttributeService.isMember(exclude, personToAdd)) {
                    WsDeleteMemberResults wsDeleteMemberResults = grouperFS.makeWsDeleteMemberResults(
                            exclude,
                            user,
                            personToAdd);

                    isExcludeUpdated = true;

                    gsrList.add(helperService.makeGroupingsServiceResult(wsDeleteMemberResults,
                            "delete " + personToAdd.toString() + " from " + exclude));
                }
                //check to see if personToAdd is already in include
                if (!memberAttributeService.isMember(include, personToAdd)) {
                    //add to include
                    WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(include, user, personToAdd);

                    isIncludeUpdated = true;

                    gsrList.add(helperService.makeGroupingsServiceResult(addMemberResults, action));
                } else {
                    //They are already in the group, so just return SUCCESS
                    gsrList.add(helperService.makeGroupingsServiceResult(
                            SUCCESS + ": " + personToAdd.toString() + " was already in " + groupPath, action));
                }
            }

            //if exclude check if personToAdd is in the include
            else if (groupPath.endsWith(EXCLUDE)) {
                //if personToAdd is in include, get them out
                if (memberAttributeService.isMember(include, personToAdd)) {
                    WsDeleteMemberResults wsDeleteMemberResults = grouperFS.makeWsDeleteMemberResults(
                            include,
                            user,
                            personToAdd);

                    isIncludeUpdated = true;

                    gsrList.add(helperService.makeGroupingsServiceResult(wsDeleteMemberResults,
                            "delete " + personToAdd.toString() + " from " + include));
                }
                //check to see if userToAdd is already in exclude
                if (!memberAttributeService.isMember(exclude, personToAdd)) {
                    //add to exclude
                    WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(exclude, user, personToAdd);

                    isExcludeUpdated = true;

                    gsrList.add(helperService.makeGroupingsServiceResult(addMemberResults, action));
                }
                //They are already in the group, so just return SUCCESS
                gsrList.add(helperService.makeGroupingsServiceResult(
                        SUCCESS + ": " + personToAdd.toString() + " was already in " + groupPath, action));

            }
            //if owners check to see if the user is already in owners
            else if (groupPath.endsWith(OWNERS)) {
                //check to see if userToAdd is already in owners
                if (!memberAttributeService.isMember(owners, personToAdd)) {
                    //add userToAdd to owners
                    WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(owners, user, personToAdd);

                    isOwnersUpdated = true;

                    gsrList.add(helperService.makeGroupingsServiceResult(addMemberResults, action));
                }
                //They are already in the group, so just return SUCCESS
                gsrList.add(helperService.makeGroupingsServiceResult(
                        SUCCESS + ": " + personToAdd.toString() + " was already in " + groupPath, action));
            }
            //Owners can only change include, exclude and owners groups
            else {
                gsrList.add(helperService.makeGroupingsServiceResult(
                        FAILURE + ": " + username + " may only add to exclude, include or owner group", action));
            }

            //update groups that were changed
            if (isExcludeUpdated) {
                updateLastModified(exclude);
                isCompositeUpdated = true;
            }
            if (isIncludeUpdated) {
                updateLastModified(include);
                isCompositeUpdated = true;
            }
            if (isOwnersUpdated) {
                updateLastModified(owners);
                isCompositeUpdated = true;
            }
            if (isCompositeUpdated) {
                updateLastModified(composite);
            }
        } else {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        return gsrList;
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

}
