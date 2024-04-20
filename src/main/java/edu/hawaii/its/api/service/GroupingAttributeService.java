package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.GroupingsServiceResultException;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdatedAttributesResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.GroupAttribute;

@Service
public class GroupingAttributeService {

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

    public static final Log logger = LogFactory.getLog(GroupingAttributeService.class);

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private UpdateTimestampService timestampService;

    /**
     * Turn the ability for users to opt-in/opt-out to a grouping on or off.
     */
    public List<GroupingsServiceResult> changeOptStatus(OptRequest optInRequest, OptRequest optOutRequest) {

        checkPrivileges(optInRequest.getGroupNameRoot(), optInRequest.getUid());

        List<GroupingsServiceResult> results = new ArrayList<>();

        results.add(assignGrouperPrivilege(
                optInRequest.getUid(),
                optInRequest.getPrivilegeType().value(),
                optInRequest.getGroupName(),
                optInRequest.getOptValue()));

        results.add(assignGrouperPrivilege(
                optOutRequest.getUid(),
                optOutRequest.getPrivilegeType().value(),
                optOutRequest.getGroupName(),
                optOutRequest.getOptValue()));

        results.add(changeGroupAttributeStatus(optInRequest.getGroupNameRoot(),
                optInRequest.getUid(),
                optInRequest.getOptId(),
                optInRequest.getOptValue()));

        return results;
    }

    /**
     * Turns the attribute on or off in a group.
     * OPT_IN, OPT_OUT, and sync destinations are allowed.
     */
    public GroupingsServiceResult changeGroupAttributeStatus(String groupPath, String ownerUhIdentifier,
            String attributeName, boolean turnAttributeOn) {
        logger.info(String.format("changeGroupAttributeStatus; groupPath: %s; ownerUhIdentifier: %s; attributeName: %s, turnAttributeOn: %s",
                groupPath, ownerUhIdentifier, attributeName, turnAttributeOn));
        checkPrivileges(groupPath, ownerUhIdentifier);
        String verb = "removed from ";
        String resultCode = SUCCESS;
        if (turnAttributeOn) {
            verb = "added to ";
        }

        String action = attributeName + " has been " + verb + groupPath + " by " + ownerUhIdentifier;
        boolean isHasAttribute = isGroupAttribute(groupPath, attributeName);

        if (turnAttributeOn) {
            if (!isHasAttribute) {
                assignAttribute(ownerUhIdentifier, attributeName, groupPath);
            } else {
                resultCode += ", " + attributeName + " already existed";
            }
        } else {
            if (isHasAttribute) {
                removeAttribute(ownerUhIdentifier, attributeName, groupPath);
            } else {
                resultCode += ", " + attributeName + " did not exist";
            }
        }
        return new GroupingsServiceResult(resultCode, action);
    }

    // Check if attribute is on.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        List<GroupAttribute> groupAttributes = grouperService
                .groupAttributeResults(attributeName, groupPath)
                .getGroupAttributes();
        return groupAttributes.stream()
                .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributeName));
    }

    public GroupingUpdatedAttributesResult assignAttribute(String currentUser, String attributeName, String groupingPath) {
        return updateAttribute(currentUser, attributeName, OPERATION_ASSIGN_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributesResult removeAttribute(String currentUser, String attributeName, String groupingPath) {
        return updateAttribute(currentUser, attributeName, OPERATION_REMOVE_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributesResult updateAttribute(String currentUser, String attributeName, String assignOperation,
            String groupingPath) {
        AssignAttributesResults assignAttributesResults = grouperService.assignAttributesResults(
                currentUser, ASSIGN_TYPE_GROUP, assignOperation, groupingPath, attributeName);

        GroupingUpdatedAttributesResult result = new GroupingUpdatedAttributesResult(assignAttributesResults);
        timestampService.update(result);
        return result;
    }

    /**
     * Helper - changeOptStatus
     */
    public GroupingsServiceResult assignGrouperPrivilege(String currentUser, String privilegeName, String groupName, boolean isSet) {
        String action = "set " + privilegeName + " " + isSet + " for " + EVERY_ENTITY + " in " + groupName;
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                grouperService.assignGrouperPrivilegesResult(currentUser, groupName, privilegeName, EVERY_ENTITY, isSet);
        return makeGroupingsServiceResult(assignGrouperPrivilegesResult.getResultCode(), action);
    }

    // Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
    public GroupingUpdateDescriptionResult updateDescription(String groupPath, String ownerUid,
            String description) {
        logger.info(String.format("updateDescription; groupPath: %s; ownerUid: %s; description: %s;",
                groupPath, ownerUid, description));

        if (!memberService.isOwner(groupPath, ownerUid) && !memberService.isAdmin(
                ownerUid)) {
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
