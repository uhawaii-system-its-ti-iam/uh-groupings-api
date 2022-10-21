package edu.hawaii.its.api.service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.wrapper.AttributeAssignmentsResults;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.AttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@Service
public class GroupAttributeService {

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    public static final Log logger = LogFactory.getLog(GroupAttributeService.class);

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    /**
     * Get all the sync destinations for a specific grouping.
     */
    public List<SyncDestination> getAllSyncDestinations(String currentUser, String path) {

        checkPrivileges(currentUser);

        Grouping grouping = groupingAssignmentService.getGrouping(path, currentUser);
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
    public List<SyncDestination> getSyncDestinations(Grouping grouping) {
        List<SyncDestination> syncDestinations = grouperApiService.syncDestinations();

        for (SyncDestination destination : syncDestinations) {
            destination.setSynced(isGroupAttribute(grouping.getPath(), destination.getName()));
            destination.setDescription(parseKeyVal(grouping.getName(), destination.getDescription()));
        }
        return syncDestinations;
    }

    /**
     * Turn the ability for users to opt-in/opt-out to a grouping on or off.
     */
    public List<GroupingsServiceResult> changeOptStatus(OptRequest optInRequest, OptRequest optOutRequest) {

        checkPrivileges(optInRequest.getGroupNameRoot(), optInRequest.getUsername());

        List<GroupingsServiceResult> results = new ArrayList<>();

        results.add(assignGrouperPrivilege(
                optInRequest.getPrivilegeType().value(),
                optInRequest.getGroupName(),
                optInRequest.getOptValue()));

        results.add(assignGrouperPrivilege(
                optOutRequest.getPrivilegeType().value(),
                optOutRequest.getGroupName(),
                optOutRequest.getOptValue()));

        results.add(changeGroupAttributeStatus(optInRequest.getGroupNameRoot(),
                optInRequest.getUsername(),
                optInRequest.getOptId(),
                optInRequest.getOptValue()));

        return results;
    }

    /**
     * Turns the attribute on or off in a group.
     * OPT_IN, OPT_OUT, and sync destinations are allowed.
     */
    public GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUsername,
            String attributeName, boolean turnAttributeOn) {

        checkPrivileges(groupPath, ownerUsername);
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

                gsr = makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " already existed", action);
            }
        } else {
            if (isHasAttribute) {
                grouperApiService.assignAttributesResultsForGroup(ASSIGN_TYPE_GROUP,
                        OPERATION_REMOVE_ATTRIBUTE, attributeName, groupPath);

                gsr = makeGroupingsServiceResult(SUCCESS, action);

                membershipService.updateLastModified(groupPath);
            } else {
                gsr = makeGroupingsServiceResult(SUCCESS + ", " + attributeName + " did not exist", action);
            }
        }
        return gsr;
    }

    // Check if attribute is on.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        AttributeAssignmentsResults attributeAssignmentsResults = new AttributeAssignmentsResults(
                grouperApiService.groupAttributeAssigns(ASSIGN_TYPE_GROUP, attributeName, groupPath));
        return attributeAssignmentsResults.isAttributeDefName(attributeName);
    }

    /**
     * Helper - changeOptStatus
     */
    public GroupingsServiceResult assignGrouperPrivilege(String privilegeName, String groupName, boolean isSet) {

        logger.info("assignGrouperPrivilege; group: " + groupName
                + "; privilegeName: " + privilegeName
                + " set: " + isSet + ";");

        WsSubjectLookup lookup = grouperApiService.subjectLookup(EVERY_ENTITY);
        String action = "set " + privilegeName + " " + isSet + " for " + EVERY_ENTITY + " in " + groupName;

        WsAssignGrouperPrivilegesLiteResult grouperPrivilegesLiteResult =
                grouperApiService.assignGrouperPrivilegesLiteResult(
                        groupName,
                        privilegeName,
                        lookup,
                        isSet);

        return makeGroupingsServiceResult(grouperPrivilegesLiteResult, action);
    }

    // Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
    public GroupingsServiceResult updateDescription(String groupPath, String ownerUsername, String description) {
        logger.info("updateDescription(); groupPath:" + groupPath +
                "; ownerUsername:" + ownerUsername +
                "; description: " + description + ";");

        String action = "Description field of grouping " + groupPath + " has been updated by " + ownerUsername;

        if (!memberAttributeService.isOwner(groupPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException();
        }
        grouperApiService.updateGroupDescription(groupPath, description);

        return makeGroupingsServiceResult(SUCCESS + ", description updated", action);
    }

    //TODO: Move both checkPrivileges helper methods to the Governor class once it's built
    /**
     * Helper - changeOptStatus, changeGroupAttributeStatus
     */
    private void checkPrivileges(String groupingPath, String ownerIdentifier) {
        if (!memberAttributeService.isOwner(groupingPath, ownerIdentifier)
                && !memberAttributeService.isAdmin(ownerIdentifier)) {
            throw new AccessDeniedException();
        }
    }

    /**
     * Helper - getAllSyncDestinations
     */
    private void checkPrivileges(String ownerIdentifier) {
        if (!memberAttributeService.isOwner(ownerIdentifier) && !memberAttributeService.isAdmin(
                ownerIdentifier)) {
            throw new AccessDeniedException();
        }
    }

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

    /**
     * Make a groupingsServiceResult with the result code from the metadataHolder and the action string.
     */
    public GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultMetadataHolder.getResultMetadata().getResultCode());

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }

        return groupingsServiceResult;
    }

    public GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action,
                                                             Person person) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultMetadataHolder.getResultMetadata().getResultCode());
        groupingsServiceResult.setPerson(person);

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }

        return groupingsServiceResult;
    }

    public GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultCode);

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }
        return groupingsServiceResult;
    }

}
