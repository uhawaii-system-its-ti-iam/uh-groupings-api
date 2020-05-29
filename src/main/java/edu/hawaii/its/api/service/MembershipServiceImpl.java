package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.MembershipAssignment;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private JavaMailSender javaMailSender;

    @Autowired
    private GroupingsMailService groupingsMailService;

    public static final Log logger = LogFactory.getLog(MembershipServiceImpl.class);

    // returns true if username is a UH id number
    public boolean isUhUuid(String naming) {
        return naming.matches("\\d+");
    }

    // Creates a Person depending on the input used. For example, if input is UhUuid, user will be created using that.
    public Person createNewPerson(String userToAdd) {
        Person createdPerson;

        try {
            Integer.parseInt(userToAdd);
            createdPerson = new Person(null, userToAdd, null, null, null);

        } catch (Exception NumberFormatException) {
            createdPerson = new Person(null, null, userToAdd, null, null);
        }

        return createdPerson;
    }

    // Adds a member to a Grouping from either UH username or UH ID number.
    @Override
    public List<GroupingsServiceResult> addGroupingMember(String username, String groupingPath,
            String userIdentifier) {
        logger.info("addGroupingMemberByUuid; user: " + username + "; grouping: " + groupingPath + "; userToAdd: "
                + userIdentifier + ";");

        return addGroupingMemberHelper(username, groupingPath, userIdentifier, createNewPerson(userIdentifier));
    }

    /**
     * Delete a member of groupingPath with respect to uid or uhUuid as ownerUsername.
     */
    @Override
    public List<GroupingsServiceResult> deleteGroupingMember(String ownerUsername, String groupingPath,
            String userIdentifier) {
        logger.info("deleteGroupingMember; username: "
                + ownerUsername
                + "; groupingPath: "
                + groupingPath + "; userToDelete: "
                + userIdentifier
                + ";");

        return deleteGroupingMemberHelper(ownerUsername, groupingPath, userIdentifier, createNewPerson(userIdentifier));
    }

    /**
     * Get a list of groupsOwned by user as admin
     */
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

    /**
     * Add a list of multiple usersToAdd to groupPath as ownerUsername
     */
    @Override
    public List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupPath,
            List<String> usersToAdd) {
        List<GroupingsServiceResult> gsrs = new ArrayList<>();
        for (String userToAdd : usersToAdd) {
            try {
                gsrs.addAll(addGroupMember(ownerUsername, groupPath, userToAdd));
            } catch (GcWebServiceError e) {
            }
        }

        if (usersToAdd.size() > 100) {
            groupingsMailService
                    .setJavaMailSender(javaMailSender)
                    .setFrom("no-reply@its.hawaii.edu");
            groupingsMailService.sendCSVMessage(
                    "no-reply@its.hawaii.edu",
                    groupingsMailService.getUserEmail(ownerUsername),
                    "Groupings: Add " + groupPath,
                    "",
                    "UH-Groupings-Report-" + LocalDateTime.now().toString() + ".csv", gsrs);
        }
        return gsrs;
    }

    /**
     * Authenticate userIdentifier as a valid person and add them to groupPath as ownerUsername
     */
    @Override
    public List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupPath,
            String userIdentifier) {
        logger.info("addGroupMember; user: " + ownerUsername + "; groupPath: " + groupPath + "; userToAdd: "
                + userIdentifier + ";");

        return addMemberHelper(ownerUsername, groupPath, createNewPerson(userIdentifier));
    }

    @Override
    public GroupingsServiceResult deleteGroupMember(String ownerUsername, String groupPath,
            String userIdentifier) {
        logger.info("deleteGroupMemberByUuid; user: " + ownerUsername
                + "; group: " + groupPath
                + "; userToDelete: " + userIdentifier
                + ";");

        return deleteMemberHelper(ownerUsername, groupPath, createNewPerson(userIdentifier));
    }

    /**
     * Remove the valid members contained in usersToDelete from groupPath as currentUser.
     *
     * @param currentUser   Must be an admin and owner of grouping at groupPath.
     * @param groupPath     Full path of group at grouping.
     * @param usersToDelete List of potential members to be deleted.
     * @return FAILURE if none of the usersToDelete are valid members of groupPath, otherwise return SUCCESS with
     * response containing the members which were deleted. Throws AccessDeniedException if currentUser is not
     * an admin and an owner of grouping at groupPath.
     */
    @Override
    public GenericServiceResult removeGroupMembers(String currentUser, String groupPath,
            List<String> membersToRemove) {

        String composite = helperService.parentGroupingPath(groupPath);

        if (!memberAttributeService.isOwner(composite, currentUser) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        List<String> membersRemoved;
        String action = "removeGroupMembers; " +
                "currentUser: " + currentUser + "; " +
                "groupPath: " + groupPath + "; " +
                "membersToRemove: " + membersToRemove.toString() + ";";
        logger.info(action);

        if ((membersRemoved = getValidMembers(groupPath, membersToRemove)).isEmpty()) {
            return new GenericServiceResult(
                    new GroupingsServiceResult(FAILURE, action + " Error Message: no valid members in membersToRemove"),
                    "membersToRemove", membersToRemove);
        }

        action += " membersRemoved: " + membersRemoved + "; ";
        logger.info(action);

        WsDeleteMemberResults removeMemberResults = grouperFS
                .makeWsDeleteMemberResults(groupPath, grouperFS.makeWsSubjectLookup(currentUser), membersRemoved);

        updateLastModified(composite);
        updateLastModified(groupPath);

        return new GenericServiceResult(helperService.makeGroupingsServiceResult(removeMemberResults, action),
                Arrays.asList("membersToRemove", "membersRemoved"), membersToRemove, membersRemoved);
    }

    /**
     * From a list of potentialMembers, return a list of members from grouping at path.
     *
     * @param path             Path of grouping and group.
     * @param potentialMembers List to be tested.
     * @return A list of valid members.
     */
    public List<String> getValidMembers(String path, List<String> potentialMembers) {
        List<String> members = new ArrayList<>();
        for (String potentialMember : potentialMembers) {
            try {
                if (memberAttributeService.isMember(path, potentialMember)) {
                    members.add(potentialMember);
                }
            } catch (GcWebServiceError e) {
                logger.info("\"" + potentialMember + "\"" + " is invalid for deletion", e);
            }
        }
        return members;
    }

    @Override
    public List<Membership> getMemberShipResults(String ownerUsername, String uid) {
        String action = "GET: " + uid + "Memberships;";

        List<Membership> memberships = new ArrayList<>();
        logger.info(action);
        if (memberAttributeService.isSuperuser(ownerUsername)) {
            List<String> groupPaths = groupingAssignmentService.getGroupPaths(ownerUsername, uid);

            for (String groupPath : groupPaths) {
                boolean hasMembership = false;

                Membership membership = new Membership();
                if (groupPath.endsWith(INCLUDE)) {
                    membership.setInInclude(true);
                    hasMembership = true;
                }
                if (groupPath.endsWith(BASIS)) {
                    membership.setInBasis(true);
                    hasMembership = true;
                }
                if (groupPath.endsWith(EXCLUDE)) {
                    membership.setInExclude(true);
                    hasMembership = true;
                }
                if (groupPath.endsWith(BASIS_PLUS_INCLUDE)) {
                    membership.setInBasisAndInclude(true);
                    hasMembership = true;
                }
                if (hasMembership) {
                    membership.setPath(groupPath);
                    memberships.add(membership);
                }
            }
            return memberships;
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

    @Override
    public List<GroupingsServiceResult> removeFromGroups(String adminUsername, String userToRemove, List<String> GroupPaths) {

        List<GroupingsServiceResult> result = new ArrayList<GroupingsServiceResult>();

        for(int i = 0; i < GroupPaths.size();i++) {
            System.out.println("Removing " + userToRemove + " from Group " + i +  ":" + GroupPaths.get(i));
            String action = "delete " + userToRemove + " from " + GroupPaths.get(i);
            WsSubjectLookup adminLookup = grouperFS.makeWsSubjectLookup(adminUsername);
            WsDeleteMemberResults deleteMemberResults = grouperFS.makeWsDeleteMemberResults(GroupPaths.get(i), adminLookup, userToRemove);
            result.add(helperService.makeGroupingsServiceResult(deleteMemberResults, action));
        }
        return result;
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
                    results.addAll(deleteGroupingMember(username, grouping, username));
                    break;

                case "in ":
                    results.addAll(addGroupingMember(username, grouping, username));
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

    @Override public GenericServiceResult generic() {
        GenericServiceResult genericServiceResult = new GenericServiceResult();

        String hello = "HelloWorld!";
        GroupingsServiceResult groupingsServiceResult =
                new GroupingsServiceResult(SUCCESS, "Testing: GenericServiceResult");
        List<String> fbb = Arrays.asList("foo", "bar", "baz");

        genericServiceResult.add("hello", hello);
        genericServiceResult.add("fbb", fbb);
        genericServiceResult.add(Arrays.asList("groupingsServiceResult", "boolean"), groupingsServiceResult, true);
        genericServiceResult.add("int", 1);

        return genericServiceResult;
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
                }
                //check to see if personToAdd is already in include
                if (!memberAttributeService.isMember(include, personToAdd)) {
                    //add to include
                    WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(include, user, personToAdd);

                    isIncludeUpdated = true;

                    // Set person's attributes whether it's with username or Uuid.
                    if (personToAdd.getUsername() != null) {
                        personToAdd.setAttributes(
                                memberAttributeService.getUserAttributes(username, personToAdd.getUsername()));
                    } else {
                        personToAdd.setAttributes(
                                memberAttributeService.getUserAttributes(username, personToAdd.getUhUuid()));
                    }

                    gsrList.add(helperService.makeGroupingsServiceResult(addMemberResults, action, personToAdd));
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
                }
                //check to see if userToAdd is not in exclude
                if (!memberAttributeService.isMember(exclude, personToAdd)) {
                    //add to exclude
                    WsAddMemberResults addMemberResults = grouperFS.makeWsAddMemberResults(exclude, user, personToAdd);

                    isExcludeUpdated = true;

                    // Set person's attributes whether it's with username or Uuid.
                    if (personToAdd.getUsername() != null) {
                        personToAdd.setAttributes(
                                memberAttributeService.getUserAttributes(username, personToAdd.getUsername()));
                    } else {
                        personToAdd.setAttributes(
                                memberAttributeService.getUserAttributes(username, personToAdd.getUhUuid()));
                    }

                    gsrList.add(helperService.makeGroupingsServiceResult(addMemberResults, action, personToAdd));
                    //if userToAdd is already in exclude
                } else {
                    gsrList.add(helperService.makeGroupingsServiceResult(
                            SUCCESS + ": " + personToAdd.getUsername() + " was already in " + groupPath, action));
                }
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

    // Logic for deleting a member.
    public GroupingsServiceResult deleteMemberHelper(String username, String groupPath, Person personToDelete) {
        String action = "delete " + personToDelete + " from " + groupPath;

        String composite = helperService.parentGroupingPath(groupPath);

        if (memberAttributeService.isOwner(composite, username) || memberAttributeService
                .isSuperuser(username) || username.equals(personToDelete.getUsername())) {
            WsSubjectLookup user = grouperFS.makeWsSubjectLookup(username);
            if (groupPath.endsWith(EXCLUDE) || groupPath.endsWith(INCLUDE) || groupPath.endsWith(OWNERS)) {
                if (memberAttributeService.isMember(groupPath, personToDelete)) {
                    WsDeleteMemberResults deleteMemberResults =
                            grouperFS.makeWsDeleteMemberResults(groupPath, user, personToDelete);

                    updateLastModified(composite);
                    updateLastModified(groupPath);
                    return helperService.makeGroupingsServiceResult(deleteMemberResults, action);
                }
                return helperService
                        .makeGroupingsServiceResult(SUCCESS + ": " + personToDelete + " was not in " + groupPath,
                                action);
            }
            return helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + username + " may only delete from exclude, include or owner group", action);
        }
        throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
    }

    // Helper method for adding a user to a grouping.
    public List<GroupingsServiceResult> addGroupingMemberHelper(String username, String groupingPath,
            String userIdentifier, Person personToAdd) {
        List<GroupingsServiceResult> gsrs = new ArrayList<>();

        String action = "add user to " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        boolean isInBasis = memberAttributeService.isMember(basis, personToAdd);
        boolean isInComposite = memberAttributeService.isMember(groupingPath, personToAdd);
        boolean isInInclude = memberAttributeService.isMember(include, personToAdd);

        //check to see if they are already in the grouping
        if (!isInComposite) {
            //get them out of the exclude
            gsrs.add(deleteGroupMember(username, exclude, userIdentifier));
            //only add them to the include if they are not in the basis
            if (!isInBasis) {
                gsrs.addAll(addGroupMember(username, include, userIdentifier));
            } else {
                gsrs.add(helperService
                        .makeGroupingsServiceResult(SUCCESS + ": " + userIdentifier + " was in " + basis, action));
            }
        } else {
            gsrs.add(helperService
                    .makeGroupingsServiceResult(SUCCESS + ": " + userIdentifier + " was already in " + groupingPath,
                            action));
        }
        //should only be in one or the other
        if (isInBasis && isInInclude) {
            gsrs.add(deleteGroupMember(username, include, userIdentifier));
        }

        return gsrs;
    }

    // Helper method for deleting a user from a grouping.
    public List<GroupingsServiceResult> deleteGroupingMemberHelper(String username, String groupingPath,
            String userIdentifier, Person personToDelete) {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();

        String action = username + " deletes " + userIdentifier + " from " + groupingPath;
        String basis = groupingPath + BASIS;
        String exclude = groupingPath + EXCLUDE;
        String include = groupingPath + INCLUDE;

        boolean isInBasis = memberAttributeService.isMember(basis, personToDelete);
        boolean isInComposite = memberAttributeService.isMember(groupingPath, personToDelete);
        boolean isInExclude = memberAttributeService.isMember(exclude, personToDelete);

        //if they are in the include group, get them out
        gsrList.add(deleteGroupMember(username, include, userIdentifier));

        //make sure userToDelete is actually in the Grouping
        if (isInComposite) {
            //if they are not in the include group, then they are in the basis, so add them to the exclude group
            if (isInBasis) {
                gsrList.addAll(addGroupMember(username, exclude, userIdentifier));
            }
        }
        //since they are not in the Grouping, do nothing, but return SUCCESS
        else {
            gsrList.add(
                    helperService.makeGroupingsServiceResult(SUCCESS + userIdentifier + " was not in " +
                            groupingPath, action));
        }

        //should not be in exclude if not in basis
        if (!isInBasis && isInExclude) {
            gsrList.add(deleteGroupMember(username, exclude, userIdentifier));
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
