package edu.hawaii.its.api.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

/**
 * A service for adding and removing UH grouping members.
 */
@Service("updateMemberService")
public class UpdateMemberService {
    private static final Log log = LogFactory.getLog(UpdateMemberService.class);
    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    private final UpdateTimestampService timestampService;

    private final SubjectService subjectService;

    private final GroupPathService groupPathService;

    private final MemberService memberService;

    private final GrouperService grouperService;

    public UpdateMemberService(UpdateTimestampService timestampService,
            SubjectService subjectService,
            GroupPathService groupPathService,
            MemberService memberService,
            GrouperService grouperService) {
        this.timestampService = timestampService;
        this.subjectService = subjectService;
        this.groupPathService = groupPathService;
        this.memberService = memberService;
        this.grouperService = grouperService;
    }

    public GroupingAddResult addAdminMember(String currentUser, String uhIdentifier) {
        log.info(String.format("addAdmin; currentUser: %s; uhIdentifier: %s", currentUser, uhIdentifier));
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return addAdmin(currentUser, uhIdentifier);
    }

    public GroupingRemoveResult removeAdminMember(String currentUser, String uhIdentifier) {
        log.info(String.format("removeAdmin; currentUser: %s; uhIdentifier: %s", currentUser, uhIdentifier));
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return removeAdmin(currentUser, uhIdentifier);
    }

    public GroupingAddResults addOwnerships(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        log.info(String.format("addOwnerships; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return addOwners(currentUser, groupingPath, validIdentifiers);
    }

    public GroupingAddResult addOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return addOwner(currentUser, groupingPath, validIdentifier);
    }

    public GroupingAddResults addGroupPathOwnership(String currentUser, String groupingPath,
            List<String> groupPathOwners) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return addGroupPathOwners(currentUser, groupingPath, groupPathOwners);
    }

    public GroupingRemoveResults removeOwnerships(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("removeOwnerships; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return removeOwners(currentUser, groupingPath, validIdentifiers);
    }

    public GroupingRemoveResult removeOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        if (!memberService.isOwner(groupingPath, currentUser) && memberService.isAdmin(currentUser)) {
            addOwnership(currentUser, groupingPath, currentUser);
        }
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return removeOwner(currentUser, groupingPath, validIdentifier);
    }

    public GroupingRemoveResults removeGroupPathOwnerships(String currentUser, String groupingPath,
            List<String> groupPathOwners) {
        log.info(String.format("removeGroupPathOwnerships; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, groupPathOwners));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupPathOwners(currentUser, groupingPath, groupPathOwners);
    }

    public GroupingMoveMembersResult addIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("addIncludeMembers; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(),
                groupingPath + GroupType.EXCLUDE.value(), validIdentifiers);
    }

    @Async
    public CompletableFuture<GroupingMoveMembersResult> addIncludeMembersAsync(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("addIncludeMembersAsync; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return CompletableFuture.supplyAsync(() ->
                moveGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(),
                        groupingPath + GroupType.EXCLUDE.value(), validIdentifiers)
        );
    }

    public GroupingMoveMemberResult addIncludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(currentUser, groupingPath + GroupType.INCLUDE.value(),
                groupingPath + GroupType.EXCLUDE.value(), validIdentifier);
    }

    public GroupingMoveMembersResult addExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("addExcludeMembers; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.INCLUDE.value(), validIdentifiers);
    }

    @Async
    public CompletableFuture<GroupingMoveMembersResult> addExcludeMembersAsync(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("addExcludeMembersAsync; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return CompletableFuture.supplyAsync(() ->
                moveGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(),
                        groupingPath + GroupType.INCLUDE.value(), validIdentifiers)
        );
    }

    public GroupingMoveMemberResult addExcludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(currentUser, groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.INCLUDE.value(), validIdentifier);
    }

    public GroupingRemoveResults removeIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("removeIncludeMembers; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(currentUser, groupingPath + GroupType.INCLUDE.value(), uhIdentifiers);
    }

    public GroupingRemoveResult removeIncludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(currentUser, groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public GroupingRemoveResults removeExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        log.info(String.format("removeExcludeMembers; currentUser: %s; groupingPath: %s; uhIdentifiers: %s;",
                currentUser, groupingPath, uhIdentifiers));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(currentUser, groupingPath + GroupType.EXCLUDE.value(), uhIdentifiers);
    }

    public GroupingRemoveResult removeExcludeMember(String currentUser, String groupingPath,
            String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(currentUser, groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public GroupingMoveMemberResult optIn(String currentUser, String groupingPath, String uhIdentifier) {
        log.info(String.format("optIn; currentUser: %s; groupingPath: %s; uhIdentifier %s;",
                currentUser, groupingPath, uhIdentifier));
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        // To prevent a race condition and avoid calling the grouper API to update the status of someone who is already opted in
        if(memberService.isInclude(groupingPath, uhIdentifier)) {
            return new GroupingMoveMemberResult(groupingPath, "SUCCESS");
        }
        return moveGroupMember(currentUser, groupingPath + GroupType.INCLUDE.value(),
                groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public GroupingMoveMemberResult optOut(String currentUser, String groupingPath, String uhIdentifier) {
        log.info(String.format("optOut; currentUser: %s; groupingPath: %s; uhIdentifier %s;",
                currentUser, groupingPath, uhIdentifier));
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        // To prevent a race condition and avoid calling the grouper API to update the status of someone who is already opted out
        if(!memberService.isInclude(groupingPath, uhIdentifier)) {
            return new GroupingMoveMemberResult(groupingPath, "SUCCESS");
        }
        return moveGroupMember(currentUser, groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public GroupingRemoveResults removeFromGroups(String currentUser, String uhIdentifier, List<String> groupPaths) {
        log.info(String.format("removeFromGroups; currentUser: %s; uhIdentifier: %s; groupPaths: %s",
                currentUser, uhIdentifier, groupPaths));
        checkIfAdminUser(currentUser);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        for (String groupPath : groupPaths) {
            if (!groupPathService.isGroupPath(groupPath)) {
                throw new GcWebServiceError("404: Invalid group path.");
            }
        }
        for (String groupPath : groupPaths) {
            groupingRemoveResults.add(removeGroupMember(currentUser, groupPath, validIdentifier));
        }
        return groupingRemoveResults;
    }

    /**
     * Remove all members from the include group at groupingPath.
     */
    public GroupingReplaceGroupMembersResult resetIncludeGroup(String currentUser, String groupingPath) {
        log.info(String.format("resetIncludeGroup; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getIncludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from the include group at groupingPath asynchronously.
     */
    @Async
    public CompletableFuture<GroupingReplaceGroupMembersResult> resetIncludeGroupAsync(String currentUser,
            String groupingPath) {
        log.info(
                String.format("resetIncludeGroupAsync; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getIncludeGroup(groupingPath);
        return CompletableFuture.supplyAsync(() -> resetGroup(groupPath));
    }

    /**
     * Remove all members from the exclude group at groupingPath.
     */
    public GroupingReplaceGroupMembersResult resetExcludeGroup(String currentUser, String groupingPath) {
        log.info(String.format("resetIncludeGroup; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getExcludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from the exclude group at groupingPath asynchronously.
     */
    @Async
    public CompletableFuture<GroupingReplaceGroupMembersResult> resetExcludeGroupAsync(String currentUser,
            String groupingPath) {
        log.info(
                String.format("resetIncludeGroupAsync; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getExcludeGroup(groupingPath);
        return CompletableFuture.supplyAsync(() -> resetGroup(groupPath));
    }

    /**
     * Remove all members from group at groupPath.
     */
    private GroupingReplaceGroupMembersResult resetGroup(String groupPath) {
        AddMembersResults addMembersResults = grouperService.resetGroupMembers(groupPath);
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult(addMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    public void checkIfOwnerOrAdminUser(String currentUser, String groupingPath) {
        if (!memberService.isOwner(groupingPath, currentUser) && !memberService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
    }

    public void checkIfAdminUser(String currentUser) {
        if (!memberService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
    }

    public void checkIfSelfOptOrAdmin(String currentUser, String identifier) {
        if (!currentUser.equals(identifier) && !memberService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
    }

    private GroupingMoveMembersResult moveGroupMembers(String currentUser, String addGroupPath, String removeGroupPath,
            List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults =
                grouperService.removeMembers(currentUser, removeGroupPath, uhIdentifiers);
        AddMembersResults addMembersResults = grouperService.addMembers(currentUser, addGroupPath, uhIdentifiers);
        GroupingMoveMembersResult result = new GroupingMoveMembersResult(addMembersResults, removeMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result.getAddResults());
            timestampService.update(result.getRemoveResults());
        }
        return result;
    }

    private GroupingMoveMemberResult moveGroupMember(String currentUser, String addGroupPath, String removeGroupPath,
            String uhIdentifier) {
        RemoveMemberResult removeMemberResult = grouperService.removeMember(currentUser, removeGroupPath, uhIdentifier);
        AddMemberResult addMemberResult = grouperService.addMember(currentUser, addGroupPath, uhIdentifier);
        GroupingMoveMemberResult result = new GroupingMoveMemberResult(addMemberResult, removeMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result.getAddResult());
            timestampService.update(result.getRemoveResult());
        }
        return result;
    }

    private GroupingRemoveResults removeGroupMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults = grouperService.removeMembers(currentUser, groupPath, uhIdentifiers);
        GroupingRemoveResults results = new GroupingRemoveResults(removeMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(results);
        }
        return results;
    }

    private GroupingRemoveResult removeGroupMember(String currentUser, String groupPath, String uhIdentifier) {
        RemoveMemberResult removeMemberResult = grouperService.removeMember(currentUser, groupPath, uhIdentifier);
        GroupingRemoveResult result = new GroupingRemoveResult(removeMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    private GroupingAddResult addAdmin(String currentUser, String uhIdentifier) {
        AddMemberResult addMemberResult =
                grouperService.addMember(currentUser, GROUPING_ADMINS, uhIdentifier);
        GroupingAddResult result = new GroupingAddResult(addMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    private GroupingRemoveResult removeAdmin(String currentUser, String uhIdentifier) {
        RemoveMemberResult removeMemberResult =
                grouperService.removeMember(currentUser, GROUPING_ADMINS, uhIdentifier);
        GroupingRemoveResult result = new GroupingRemoveResult(removeMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    private GroupingAddResults addOwners(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        AddMembersResults addMembersResults =
                grouperService.addMembers(currentUser, groupingPath + GroupType.OWNERS.value(), uhIdentifiers);
        GroupingAddResults results = new GroupingAddResults(addMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(results);
        }
        return results;
    }

    private GroupingAddResult addOwner(String currentUser, String groupingPath, String uhIdentifier) {
        AddMemberResult addMemberResult =
                grouperService.addMember(currentUser, groupingPath + GroupType.OWNERS.value(), uhIdentifier);
        GroupingAddResult result = new GroupingAddResult(addMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    private GroupingAddResults addGroupPathOwners(String currentUser, String groupingPath,
            List<String> groupPathOwners) {
        AddMembersResults addMembersResults =
                grouperService.addGroupPathOwners(currentUser, groupingPath + GroupType.OWNERS.value(),
                        groupPathOwners);
        GroupingAddResults results = new GroupingAddResults(addMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(results);
        }
        return results;
    }

    private GroupingRemoveResults removeOwners(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults =
                grouperService.removeMembers(currentUser, groupingPath + GroupType.OWNERS.value(), uhIdentifiers);
        GroupingRemoveResults results = new GroupingRemoveResults(removeMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(results);
        }
        return results;
    }

    private GroupingRemoveResult removeOwner(String currentUser, String groupingPath, String uhIdentifier) {
        RemoveMemberResult removeMemberResult =
                grouperService.removeMember(currentUser, groupingPath + GroupType.OWNERS.value(), uhIdentifier);
        GroupingRemoveResult result = new GroupingRemoveResult(removeMemberResult);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(result);
        }
        return result;
    }

    private GroupingRemoveResults removeGroupPathOwners(String currentUser, String groupingPath,
            List<String> groupPathOwners) {
        RemoveMembersResults removeMembersResults =
                grouperService.removeGroupPathOwners(currentUser, groupingPath + GroupType.OWNERS.value(),
                        groupPathOwners);
        GroupingRemoveResults results = new GroupingRemoveResults(removeMembersResults);
        if (grouperService instanceof GrouperApiService) {
            timestampService.update(results);
        }
        return results;
    }

}
