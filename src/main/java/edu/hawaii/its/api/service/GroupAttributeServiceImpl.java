package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.type.SyncDestination.parseKeyVal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.SyncDestination;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

@Service("groupAttributeService")
public class GroupAttributeServiceImpl implements GroupAttributeService {

    private static final Log logger = LogFactory.getLog(GroupAttributeServiceImpl.class);

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

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    /**
     * Get all the sync destinations for a specific grouping.
     */
    @Override
    public List<SyncDestination> getAllSyncDestinations(String currentUsername, String path) {

        if (!memberAttributeService.isAdmin(currentUsername) && !memberAttributeService.isOwner(currentUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        Grouping grouping = groupingAssignmentService.getGrouping(path, currentUsername);
        List<SyncDestination> finSyncDestList = grouperFactoryService.getSyncDestinations();

        for (SyncDestination dest : finSyncDestList) {
            dest.setDescription(parseKeyVal(grouping.getName(), dest.getDescription()));
        }

        return finSyncDestList;
    }

    /**
     * Similar to the getAllSyncDestination except it is called through
     * getGrouping and thus doesn't check to see if person requesting the
     * information is an owner or superuser as that has already been checked.
     */
    @Override
    public List<SyncDestination> getSyncDestinations(Grouping grouping) {
        List<SyncDestination> syncDestinations = grouperFactoryService.getSyncDestinations();

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

        if (!memberAttributeService.isOwner(ownerUsername) && !memberAttributeService.isAdmin(ownerUsername)) {
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

        if (!memberAttributeService.isOwner(ownerUsername) && !memberAttributeService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_IN, groupingPath + EXCLUDE, isOptOutOn));
        results.add(assignGrouperPrivilege(EVERY_ENTITY, PRIVILEGE_OPT_OUT, groupingPath + INCLUDE, isOptOutOn));
        results.add(changeGroupAttributeStatus(groupingPath, ownerUsername, OPT_OUT, isOptOutOn));

        return results;
    }

    // Turns the attribute on or off in a group.
    // OPT_IN, OPT_OUT, and sync destinations are allowed.
    @Override
    public GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUsername,
            String attributeName, boolean turnAttributeOn) {
        GroupingsServiceResult gsr;

        String verb = "removed from ";
        if (turnAttributeOn) {
            verb = "added to ";
        }

        String action = attributeName + " has been " + verb + groupPath + " by " + ownerUsername;

        if (!memberAttributeService.isOwner(ownerUsername) && !memberAttributeService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        boolean isHasAttribute = isGroupAttribute(groupPath, attributeName);

        if (turnAttributeOn) {
            if (!isHasAttribute) {
                assignGroupAttributes(attributeName, OPERATION_ASSIGN_ATTRIBUTE, groupPath);

                gsr = helperService.makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " already existed", action);
            }
        } else {
            if (isHasAttribute) {
                assignGroupAttributes(attributeName, OPERATION_REMOVE_ATTRIBUTE, groupPath);

                gsr = helperService.makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " did not exist", action);
            }
        }

        return gsr;
    }

    // Returns true if the group has the attribute with that name.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = attributeAssignmentsResults(
                ASSIGN_TYPE_GROUP,
                groupPath,
                attributeName);

        if (wsGetAttributeAssignmentsResults.getWsAttributeAssigns() != null) {
            for (WsAttributeAssign attribute : wsGetAttributeAssignmentsResults.getWsAttributeAssigns()) {
                if (attribute.getAttributeDefNameName() != null && attribute.getAttributeDefNameName()
                        .equals(attributeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Checks to see if a group has an attribute of a specific type and returns the list if it does.
    @Override
    public WsGetAttributeAssignmentsResults attributeAssignmentsResults(String assignType, String groupPath,
            String attributeName) {
        logger.info("attributeAssignmentsResults; assignType: "
                + assignType
                + "; group: "
                + groupPath
                + "; nameName: "
                + attributeName
                + ";");

        return grouperFactoryService.makeWsGetAttributeAssignmentsResultsForGroup(assignType, attributeName, groupPath);
    }

    // Adds, removes, updates (operationName) the attribute for the group.
    public GroupingsServiceResult assignGroupAttributes(String attributeName, String attributeOperation,
            String groupPath) {
        logger.info("assignGroupAttributes; "
                + "; attributeName: "
                + attributeName
                + "; attributeOperation: "
                + attributeOperation
                + "; group: "
                + groupPath
                + ";");

        WsAssignAttributesResults attributesResults = grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                ASSIGN_TYPE_GROUP,
                attributeOperation,
                attributeName,
                groupPath);

        return helperService.makeGroupingsServiceResult(attributesResults,
                "assign " + attributeName + " attribute to " + groupPath);
    }

    //gives the user the privilege for that group
    public GroupingsServiceResult assignGrouperPrivilege(
            String username,
            String privilegeName,
            String groupPath,
            boolean isSet) {

        logger.info("assignGrouperPrivilege; username: "
                + username
                + "; group: "
                + groupPath
                + "; privilegeName: "
                + privilegeName
                + " set: "
                + isSet
                + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(username);
        String action = "set " + privilegeName + " " + isSet + " for " + username + " in " + groupPath;

        WsAssignGrouperPrivilegesLiteResult grouperPrivilegesLiteResult =
                grouperFactoryService.makeWsAssignGrouperPrivilegesLiteResult(
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

        GroupingsServiceResult gsr;

        String action = "Description field of grouping " + groupPath + " has been updated by " + ownerUsername;

        if (!memberAttributeService.isOwner(groupPath, ownerUsername) && !memberAttributeService
                .isAdmin(ownerUsername)) {

            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        grouperFactoryService.updateGroupDescription(groupPath, description);

        gsr = helperService.makeGroupingsServiceResult(SUCCESS + ", description updated", action);

        return gsr;
    }

}
