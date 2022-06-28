package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.AttributeAssignmentsResults;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Service("groupAttributeService")
public class GroupAttributeServiceImpl implements GroupAttributeService {

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.privilege_opt_out}")
    private String PRIVILEGE_OPT_OUT;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    public static final Log logger = LogFactory.getLog(GroupAttributeServiceImpl.class);

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    /**
     * Get all the sync destinations for a specific grouping.
     */
    @Override
    public List<SyncDestination> getAllSyncDestinations(String currentUsername, String path) {

        if (!memberAttributeService.isAdmin(currentUsername) && !memberAttributeService.isOwner(currentUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        Grouping grouping = groupingAssignmentService.getGrouping(path, currentUsername);
        List<SyncDestination> finSyncDestList = grouperApiService.syncDestinations();

        for (SyncDestination dest : finSyncDestList) {
            dest.setDescription(parseKeyVal(grouping.getName(), dest.getDescription()));
        }

        return finSyncDestList;
    }

    /**
     * Similar to the getAllSyncDestination except it is called through getGrouping and thus doesn't check to see if
     * person requesting the information is an owner or superuser as that has already been checked.
     */
    @Override
    public List<SyncDestination> getSyncDestinations(Grouping grouping) {
        List<SyncDestination> syncDestinations = grouperApiService.syncDestinations();

        for (SyncDestination destination : syncDestinations) {
            destination.setSynced(isGroupAttribute(grouping.getPath(), destination.getName()));
            destination.setDescription(parseKeyVal(grouping.getName(), destination.getDescription()));
        }
        return syncDestinations;
    }

    /**
     * Turn the ability for users to opt-in to a grouping on or off.
     */
    @Override
    public List<GroupingsServiceResult> changeOptInStatus(String groupingPath, String ownerUsername,
            boolean isOptInOn) {
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_IN, groupingPath + INCLUDE, isOptInOn));
        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_OUT, groupingPath + EXCLUDE, isOptInOn));
        results.add(changeGroupAttributeStatus(groupingPath, ownerUsername, OPT_IN, isOptInOn));

        return results;
    }

    /**
     * Turn the ability for users to opt-out of a grouping on or off.
     */
    @Override
    public List<GroupingsServiceResult> changeOptOutStatus(String groupingPath, String ownerUsername,
            boolean isOptOutOn) {

        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_IN, groupingPath + EXCLUDE, isOptOutOn));
        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_OUT, groupingPath + INCLUDE, isOptOutOn));
        results.add(changeGroupAttributeStatus(groupingPath, ownerUsername, OPT_OUT, isOptOutOn));

        return results;
    }

    /**
     * Turns the attribute on or off in a group.
     * OPT_IN, OPT_OUT, and sync destinations are allowed.
     */
    @Override
    public GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUsername,
            String attributeName, boolean turnAttributeOn) {

        if (!memberAttributeService.isOwner(groupPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        GroupingsServiceResult gsr;
        String verb = "removed from ";
        if (turnAttributeOn) {
            verb = "added to ";
        }
        String action = attributeName + " has been " + verb + groupPath + " by " + ownerUsername;
        boolean isHasAttribute = isGroupAttribute(groupPath, attributeName);

        if (turnAttributeOn) {
            if (!isHasAttribute) {
                grouperApiService.assignAttributesResultsForGroup(ASSIGN_TYPE_GROUP,
                        OPERATION_ASSIGN_ATTRIBUTE, attributeName, groupPath);

                gsr = helperService.makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " already existed", action);
            }
        } else {
            if (isHasAttribute) {
                grouperApiService.assignAttributesResultsForGroup(ASSIGN_TYPE_GROUP,
                        OPERATION_REMOVE_ATTRIBUTE, attributeName, groupPath);

                gsr = helperService.makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " did not exist", action);
            }
        }
        return gsr;
    }

    // Check if attribute is on.
    @Override
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        AttributeAssignmentsResults attributeAssignmentsResults = new AttributeAssignmentsResults(
                grouperApiService.groupAttributeAssigns(ASSIGN_TYPE_GROUP, attributeName, groupPath));
        return attributeAssignmentsResults.isAttributeDefName(attributeName);
    }

    //gives the user the privilege for that group
    public GroupingsServiceResult assignGrouperPrivilege(String username, String privilegeName, String groupPath,
            boolean isSet) {

        logger.info("assignGrouperPrivilege; username: " + username + "; group: " + groupPath + "; privilegeName: "
                + privilegeName + " set: " + isSet + ";");
        WsSubjectLookup lookup = grouperApiService.subjectLookup(username);
        String action = "set " + privilegeName + " " + isSet + " for " + username + " in " + groupPath;

        WsAssignGrouperPrivilegesLiteResult grouperPrivilegesLiteResult =
                grouperApiService.assignGrouperPrivilegesLiteResult(
                        groupPath,
                        privilegeName,
                        lookup,
                        isSet);

        return helperService.makeGroupingsServiceResult(grouperPrivilegesLiteResult, action);
    }

    // Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
    public GroupingsServiceResult updateDescription(String groupPath, String ownerUsername, String description) {
        logger.info("updateDescription(); groupPath:" + groupPath +
                "; ownerUsername:" + ownerUsername +
                "; description: " + description + ";");

        String action = "Description field of grouping " + groupPath + " has been updated by " + ownerUsername;

        if (!memberAttributeService.isOwner(groupPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        grouperApiService.updateGroupDescription(groupPath, description);

        return helperService.makeGroupingsServiceResult(SUCCESS + ", description updated", action);
    }

    /**
     * Replace ${} with replace in desc otherwise return desc.
     */
    private String parseKeyVal(String replace, String desc) {
        final String regex = "(\\$\\{)(.*)(})";
        String result;
        try {
            result = desc.replaceFirst(regex, replace);
        } catch (PatternSyntaxException e) {
            result = desc;
        }
        return result;
    }

}