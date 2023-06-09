package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdatedAttributesResult;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GroupAttribute;

import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class GroupingAttributeService {

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.google.sync.dest.suffix}")
    private String GOOGLE_SYNC_DEST_SUFFIX;

    public static final Log logger = LogFactory.getLog(GroupingAttributeService.class);
    public static final Pattern pattern = Pattern.compile("\\$\\{srhfgs}");

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private UpdateTimestampService timestampService;

    /**
     * Get all the sync destinations for a specific grouping.
     */
    public List<SyncDestination> getAllSyncDestinations(String currentUser, String path) {

        checkPrivileges(currentUser);

        Grouping grouping = groupingAssignmentService.getGrouping(path, currentUser);
        List<SyncDestination> finSyncDestList = syncDestinations();

        for (SyncDestination dest : finSyncDestList) {
            dest.setDescription(parseKeyVal(grouping.getName(), dest));
        }

        return finSyncDestList;
    }

    /**
     * Similar to the getAllSyncDestination except it is called through getGrouping and thus doesn't check to see if
     * person requesting the information is an owner or superuser as that has already been checked.
     */
    public List<SyncDestination> getSyncDestinations(Grouping grouping) {
        List<SyncDestination> syncDestinations = syncDestinations();

        for (SyncDestination destination : syncDestinations) {
            destination.setDescription(parseKeyVal(grouping.getName(), destination));
            destination.setSynced(isGroupAttribute(grouping.getPath(), destination.getName()));
        }
        return syncDestinations;
    }

    public List<SyncDestination> syncDestinations() {
        FindAttributesResults findAttributesResults =
                grouperApiService.findAttributesResults(SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);
        List<SyncDestination> syncDestinations = new ArrayList<>();
        for (AttributesResult attributesResult : findAttributesResults.getResults()) {
            SyncDestination syncDestination =
                    JsonUtil.asObject(attributesResult.getDescription(), SyncDestination.class);
            syncDestination.setName(attributesResult.getName());
            syncDestinations.add(syncDestination);
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
        String verb = "removed from ";
        String resultCode = SUCCESS;
        if (turnAttributeOn) {
            verb = "added to ";
        }

        String action = attributeName + " has been " + verb + groupPath + " by " + ownerUsername;
        boolean isHasAttribute = isGroupAttribute(groupPath, attributeName);

        if (turnAttributeOn) {
            if (!isHasAttribute) {
                assignAttribute(attributeName, groupPath);
            } else {
                resultCode += ", " + attributeName + " already existed";
            }
        } else {
            if (isHasAttribute) {
                removeAttribute(attributeName, groupPath);
            } else {
                resultCode += ", " + attributeName + " did not exist";
            }
        }
        return new GroupingsServiceResult(resultCode, action);
    }

    // Check if attribute is on.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        List<GroupAttribute> groupAttributes = grouperApiService
                .groupAttributeResults(attributeName, groupPath)
                .getGroupAttributes();
        return groupAttributes.stream()
                .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributeName));
    }

    public GroupingUpdatedAttributesResult assignAttribute(String attributeName, String groupingPath) {
        return updateAttribute(attributeName, OPERATION_ASSIGN_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributesResult removeAttribute(String attributeName, String groupingPath) {
        return updateAttribute(attributeName, OPERATION_REMOVE_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributesResult updateAttribute(String attributeName, String assignOperation,
            String groupingPath) {
        AssignAttributesResults assignAttributesResults = grouperApiService.assignAttributesResults(
                ASSIGN_TYPE_GROUP, assignOperation, groupingPath, attributeName);

        GroupingUpdatedAttributesResult result = new GroupingUpdatedAttributesResult(assignAttributesResults);
        timestampService.update(result);
        return result;
    }

    /**
     * Helper - changeOptStatus
     */
    public GroupingsServiceResult assignGrouperPrivilege(String privilegeName, String groupName, boolean isSet) {
        String action = "set " + privilegeName + " " + isSet + " for " + EVERY_ENTITY + " in " + groupName;
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                grouperApiService.assignGrouperPrivilegesResult(groupName, privilegeName, EVERY_ENTITY, isSet);
        return makeGroupingsServiceResult(assignGrouperPrivilegesResult.getResultCode(), action);
    }

    // Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
    public GroupingUpdateDescriptionResult updateDescription(String groupPath, String ownerUsername,
            String description) {
        logger.info(
                "updateDescription(); groupPath:" + groupPath + "; ownerUsername:" + ownerUsername + "; description: "
                        + description + ";");

        if (!memberService.isOwner(groupPath, ownerUsername) && !memberService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException();
        }
        return groupingsService.updateGroupingDescription(groupPath, description);

    }

    //TODO: Move both checkPrivileges helper methods to the Governor class once it's built

    /**
     * Helper - changeOptStatus, changeGroupAttributeStatus
     */
    private void checkPrivileges(String groupingPath, String ownerIdentifier) {
        if (!memberService.isOwner(groupingPath, ownerIdentifier) && !memberService.isAdmin(ownerIdentifier)) {
            throw new AccessDeniedException();
        }
    }

    /**
     * Helper - getAllSyncDestinations
     */
    private void checkPrivileges(String ownerIdentifier) {
        if (!memberService.isOwner(ownerIdentifier) && !memberService.isAdmin(ownerIdentifier)) {
            throw new AccessDeniedException();
        }
    }

    /**
     * Helper - getAllSyncDestinations
     * Replace ${} with replace in desc otherwise return desc.
     */
    private String parseKeyVal(String replace, SyncDestination dest) {
        String result = replace;
        if (dest.getName().contains("google-group")) {
            result += GOOGLE_SYNC_DEST_SUFFIX;
        }

        try {
            result = pattern.matcher(dest.getDescription()).replaceFirst(result);
        } catch (PatternSyntaxException e) {
            result = dest.getDescription();
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
