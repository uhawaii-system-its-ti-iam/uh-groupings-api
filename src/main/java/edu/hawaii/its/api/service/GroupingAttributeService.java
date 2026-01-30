package edu.hawaii.its.api.service;

import java.util.regex.Pattern;

import edu.hawaii.its.api.exception.GrouperException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.InvalidDescriptionException;
import edu.hawaii.its.api.groupings.GroupingPrivilegeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdateOptAttributeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateSyncDestResult;
import edu.hawaii.its.api.groupings.GroupingUpdatedAttributeResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

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

    @Value("${groupings.api.trio}")
    private String TRIO;

    private static final Log logger = LogFactory.getLog(GroupingAttributeService.class);

    private final PolicyFactory policy = Sanitizers.FORMATTING;;

    private final GrouperService grouperService;

    private final MemberService memberService;

    private final GroupingsService groupingsService;

    private final UpdateTimestampService timestampService;

    @Value("${groupings.api.validation.description.maxlength}")
    private int DESCIPTION_MAX_LENGTH;

    @Value("${groupings.api.validation.description.regex}")
    private String DESCRIPTION_REGEX;

    private static Pattern DESCRIPTION_PATTERN;

    public GroupingAttributeService(GrouperService grouperService,
            MemberService memberService,
            GroupingsService groupingsService,
            UpdateTimestampService timestampService) {
        this.grouperService = grouperService;
        this.memberService = memberService;
        this.groupingsService = groupingsService;
        this.timestampService = timestampService;
    }

    /**
    * Checks if a group at path has TRIO attribute.
    */
    public boolean isGroupingPath(String path) {
        return isGroupAttribute(path, TRIO);
    }

    /**
     * Turns the attribute on or off in a group.
     * OPT_IN, OPT_OUT, and sync destinations are allowed.
     */
    public GroupingUpdatedAttributeResult changeGroupAttributeStatus(String groupPath, String ownerUhIdentifier,
                                                                      String attributeName, boolean turnAttributeOn) {
        logger.info(String.format("changeGroupAttributeStatus; groupPath: %s; ownerUhIdentifier: %s; attributeName: %s, turnAttributeOn: %s",
                groupPath, ownerUhIdentifier, attributeName, turnAttributeOn));
        checkPrivileges(groupPath, ownerUhIdentifier);
        if (turnAttributeOn) {
            return assignAttribute(ownerUhIdentifier, attributeName, groupPath);
        }
        return removeAttribute(ownerUhIdentifier, attributeName, groupPath);
    }
    // Check if attribute is on.
    public boolean isGroupAttribute(String groupPath, String attributeName) {
        try {
            GroupAttributeResults groupAttributes = grouperService.groupAttributeResults(attributeName, groupPath);
            return groupAttributes.getGroupAttributes().stream()
                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributeName));
        } catch (GrouperException e) {
            logger.error("Failed to get group attribute results from GrouperClient for path: " + groupPath, e);
            return false;
        }
    }
    public GroupingUpdatedAttributeResult assignAttribute(String currentUser, String attributeName, String groupingPath) {
        return updateAttribute(currentUser, attributeName, OPERATION_ASSIGN_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributeResult removeAttribute(String currentUser, String attributeName, String groupingPath) {
        return updateAttribute(currentUser, attributeName, OPERATION_REMOVE_ATTRIBUTE, groupingPath);
    }

    public GroupingUpdatedAttributeResult updateAttribute(String currentUser, String attributeName, String assignOperation, String groupingPath) {
        AssignAttributesResults assignAttributesResults = grouperService.assignAttributesResults(
                currentUser, ASSIGN_TYPE_GROUP, assignOperation, groupingPath, attributeName, true);
        GroupingUpdatedAttributeResult result = new GroupingUpdatedAttributeResult(assignAttributesResults);
        if(grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    /**
     * Helper - changeOptStatus
     */
    public GroupingPrivilegeResult assignGrouperPrivilege(String currentUser, String privilegeName, String groupName, boolean isSet) {
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                grouperService.assignGrouperPrivilegesResult(currentUser, groupName, privilegeName, EVERY_ENTITY, isSet, true);
        if (assignGrouperPrivilegesResult.getResultCode().startsWith(FAILURE)) {
            throw new AccessDeniedException(assignGrouperPrivilegesResult.getResultCode());
        }
        return new GroupingPrivilegeResult(assignGrouperPrivilegesResult);
    }

    /**
     * Updates a Group's description, then passes the Group object to GrouperFactoryService to be saved in Grouper.
     */
    public GroupingUpdateDescriptionResult updateDescription(String groupPath, String ownerUid,
            String description) {
        logger.info(String.format("updateDescription; groupPath: %s; ownerUid: %s; description: %s;",
                groupPath, ownerUid, description));

        if (!memberService.isOwner(groupPath, ownerUid) && !memberService.isAdmin(
                ownerUid)) {
            throw new AccessDeniedException();
        }
        String santized_description = validateAndSanitizeDescription(description);
        return groupingsService.updateGroupingDescription(groupPath, santized_description);
    }

    public String validateAndSanitizeDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw new InvalidDescriptionException("Description cannot be null or empty.");
        }
        if (description.length() > DESCIPTION_MAX_LENGTH) {
            throw new InvalidDescriptionException("Description length is too long.");
        }
        if (DESCRIPTION_PATTERN == null) {
            DESCRIPTION_PATTERN = Pattern.compile(DESCRIPTION_REGEX);
        }
        if (!DESCRIPTION_PATTERN.matcher(description).matches()) {
            throw new InvalidDescriptionException("Description contains invalid characters.");
        }
        return policy.sanitize(description);
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
    public GroupingUpdateSyncDestResult updateGroupingSyncDest(String groupPath, String currentUser, String id, boolean status) {
        GroupingUpdatedAttributeResult updatedAttributeResult = changeGroupAttributeStatus(groupPath, currentUser, id, status);
        return new GroupingUpdateSyncDestResult(updatedAttributeResult);
    }

    /**
     * Turn the ability for users to opt-in/opt-out to a grouping on or off.
     */
    public GroupingUpdateOptAttributeResult updateOptAttribute(OptRequest optInRequest, OptRequest optOutRequest) {

        checkPrivileges(optInRequest.getGroupNameRoot(), optInRequest.getUid());

        GroupingPrivilegeResult groupingPrivilegeOptInResult = assignGrouperPrivilege(
                optInRequest.getUid(),
                optInRequest.getPrivilegeType().value(),
                optInRequest.getGroupName(),
                optInRequest.getOptValue());

        GroupingPrivilegeResult groupingPrivilegeOptOutResult = assignGrouperPrivilege(
                optOutRequest.getUid(),
                optOutRequest.getPrivilegeType().value(),
                optOutRequest.getGroupName(),
                optOutRequest.getOptValue());

        GroupingUpdatedAttributeResult groupingUpdatedAttributeResult = changeGroupAttributeStatus(optInRequest.getGroupNameRoot(),
                optInRequest.getUid(),
                optInRequest.getOptId(),
                optInRequest.getOptValue());

        return new GroupingUpdateOptAttributeResult(groupingUpdatedAttributeResult, groupingPrivilegeOptInResult,
                groupingPrivilegeOptOutResult);
    }
}
