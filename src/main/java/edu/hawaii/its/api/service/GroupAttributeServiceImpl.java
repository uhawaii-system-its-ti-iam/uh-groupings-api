package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("groupAttributeService")
public class GroupAttributeServiceImpl implements GroupAttributeService {

    public static final Log logger = LogFactory.getLog(GroupAttributeServiceImpl.class);
    @Autowired
    private GrouperConfiguration grouperConfiguration;
   
    @Autowired
    private GrouperFactoryService grouperFactoryService;

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

        if (!memberAttributeService.isAdmin(currentUsername)) {
            throw new AccessDeniedException(grouperConfiguration.getInsufficientPrivileges());
        }

        Grouping grouping = groupingAssignmentService.getGrouping(path, currentUsername);
        List<SyncDestination> finSyncDestList = getAllSyncDestinations();

        for (SyncDestination dest : finSyncDestList) {
            dest.setDescription(dest.parseKeyVal(grouping.getName(), dest.getDescription()));
        }

        return finSyncDestList;
    }

    @Override
    public List<SyncDestination> getAllSyncDestinations() {
        return grouperFactoryService.getSyncDestinations();
    }

    /**
     * Turn the ability for users to opt-in to a grouping on or off.
     */
    @Override
    public List<GroupingsServiceResult> changeOptInStatus(String groupingPath, String ownerUsername,
            boolean isOptInOn) {
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!memberAttributeService.isOwner(ownerUsername) && !memberAttributeService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException(grouperConfiguration.getInsufficientPrivileges());
        }

        results.add(
                assignGrouperPrivilege(grouperConfiguration.getEveryEntity(), grouperConfiguration.getPrivilegeOptIn(),
                        groupingPath + grouperConfiguration.getInclude(), isOptInOn));
        results.add(
                assignGrouperPrivilege(grouperConfiguration.getEveryEntity(), grouperConfiguration.getPrivilegeOptOut(),
                        groupingPath + grouperConfiguration.getExclude(), isOptInOn));
        results.add(
                changeGroupAttributeStatus(groupingPath, ownerUsername, grouperConfiguration.getOptIn(), isOptInOn));

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
            throw new AccessDeniedException(grouperConfiguration.getInsufficientPrivileges());
        }

        results.add(
                assignGrouperPrivilege(grouperConfiguration.getEveryEntity(), grouperConfiguration.getPrivilegeOptIn(),
                        groupingPath + grouperConfiguration.getExclude(), isOptOutOn));
        results.add(
                assignGrouperPrivilege(grouperConfiguration.getEveryEntity(), grouperConfiguration.getPrivilegeOptOut(),
                        groupingPath + grouperConfiguration.getInclude(), isOptOutOn));
        results.add(
                changeGroupAttributeStatus(groupingPath, ownerUsername, grouperConfiguration.getOptOut(), isOptOutOn));

        return results;
    }

    // Turns the attribute on or off in a group.
    // grouperConfiguration.getOptIn(), grouperConfiguration.getOptOut(), and sync destinations are allowed.
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
            throw new AccessDeniedException(grouperConfiguration.getInsufficientPrivileges());
        }

        boolean isHasAttribute = isGroupAttribute(groupPath, attributeName);

        if (turnAttributeOn) {
            if (!isHasAttribute) {
                assignGroupAttributes(attributeName, grouperConfiguration.getOperationAssignAttribute(), groupPath);

                gsr = helperService.makeGroupingsServiceResult(grouperConfiguration.getSuccess(), action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(
                                grouperConfiguration.getSuccess() + ", " + attributeName + " already existed", action);
            }
        } else {
            if (isHasAttribute) {
                assignGroupAttributes(attributeName, grouperConfiguration.getOperationRemoveAttribute(), groupPath);

                gsr = helperService.makeGroupingsServiceResult(grouperConfiguration.getSuccess(), action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = helperService
                        .makeGroupingsServiceResult(
                                grouperConfiguration.getSuccess() + ", " + attributeName + " did not exist", action);
            }
        }

        return gsr;
    }

    // Returns true if the group has the attribute with that name.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = attributeAssignmentsResults(
                grouperConfiguration.getAssignTypeGroup(),
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

    /**
     * Similar to the getAllSyncDestination except it is called through getGrouping and thus doesn't check to see if
     * person requesting the information is an owner or superuser as that has already been checked.
     */
    @Override
    public List<SyncDestination> getSyncDestinations(Grouping grouping) {
        List<SyncDestination> syncDestinations = getAllSyncDestinations();

        if (syncDestinations == null) {
            return null;
        }
        for (SyncDestination destination : syncDestinations) {
            destination.setIsSynced(isGroupAttribute(grouping.getPath(), destination.getName()));
            destination.setDescription(destination.parseKeyVal(grouping.getName(), destination.getDescription()));
        }
        return syncDestinations;
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
                grouperConfiguration.getAssignTypeGroup(),
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

            throw new AccessDeniedException(grouperConfiguration.getInsufficientPrivileges());
        }

        grouperFactoryService.updateGroupDescription(groupPath, description);

        gsr = helperService
                .makeGroupingsServiceResult(grouperConfiguration.getSuccess() + ", description updated", action);

        return gsr;
    }

}
