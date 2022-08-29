package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.wrapper.AttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

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

        checkPrivileges(currentUsername);

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
     * Turn the ability for users to opt-in/opt-out to a grouping on or off.
     */
    @Override
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
    @Override
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

    @Override
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

        return helperService.makeGroupingsServiceResult(grouperPrivilegesLiteResult, action);
    }

    // Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
    @Override
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

    //TODO: Move both checkPrivileges helper methods to the Governor class once it's built
    private void checkPrivileges(String groupingPath, String ownerUsername) {
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername)
                && !memberAttributeService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
    }

    private void checkPrivileges(String ownerUsername) {
        if (!memberAttributeService.isOwner(ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
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
