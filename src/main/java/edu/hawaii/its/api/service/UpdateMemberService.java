package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A service for adding and removing UH grouping members.
 */
@Service("updateMemberService")
public class UpdateMemberService {
    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    public static final Log log = LogFactory.getLog(UpdateMemberService.class);

    @Autowired
    private UpdateTimestampService timestampService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GrouperApiService grouperApiService;

    public GroupingAddResult addAdmin(String currentUser, String uhIdentifier) {
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return new GroupingAddResult(grouperApiService.addMember(GROUPING_ADMINS, uhIdentifier));
    }

    public GroupingRemoveResult removeAdmin(String currentUser, String uhIdentifier) {
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return new GroupingRemoveResult(grouperApiService.removeMember(GROUPING_ADMINS, uhIdentifier));
    }

    public GroupingAddResults addOwnerships(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return addOwners(groupingPath, validIdentifiers);
    }

    public GroupingAddResult addOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return addOwner(groupingPath, validIdentifier);
    }

    public GroupingRemoveResults removeOwnerships(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return removeOwners(groupingPath, validIdentifiers);
    }

    public GroupingRemoveResult removeOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        if (!memberService.isOwner(groupingPath, currentUser) && memberService.isAdmin(currentUser)) {
            addOwnership(currentUser, groupingPath, currentUser);
        }
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return removeOwner(groupingPath, validIdentifier);
    }

    public GroupingMoveMembersResult addIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                validIdentifiers);
    }

    public GroupingMoveMemberResult addIncludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                validIdentifier);
    }

    public GroupingMoveMembersResult addExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.INCLUDE.value(), validIdentifiers);
    }

    public GroupingMoveMemberResult addExcludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.EXCLUDE.value(), groupingPath + GroupType.INCLUDE.value(),
                validIdentifier);
    }

    public GroupingRemoveResults removeIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(groupingPath + GroupType.INCLUDE.value(), uhIdentifiers);
    }

    public GroupingRemoveResult removeIncludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public GroupingRemoveResults removeExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(groupingPath + GroupType.EXCLUDE.value(), uhIdentifiers);
    }

    public GroupingRemoveResult removeExcludeMember(String currentUser, String groupingPath,
            String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public GroupingMoveMemberResult optIn(String currentUser, String groupingPath, String uhIdentifier) {
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                uhIdentifier);
    }

    public GroupingMoveMemberResult optOut(String currentUser, String groupingPath, String uhIdentifier) {
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.EXCLUDE.value(), groupingPath + GroupType.INCLUDE.value(),
                uhIdentifier);
    }

    public GroupingRemoveResults removeFromGroups(String currentUser, String uhIdentifier, List<String> groupPaths) {
        checkIfAdminUser(currentUser);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        for (String groupPath : groupPaths) {
            if (!groupPathService.isGroupPath(groupPath)) {
                throw new GcWebServiceError("404: Invalid group path.");
            }
        }
        for (String groupPath : groupPaths) {
            groupingRemoveResults.add(removeGroupMember(groupPath, validIdentifier));
        }
        return groupingRemoveResults;
    }

    /**
     * Remove all members from the include group at groupingPath.
     */
    public GroupingReplaceGroupMembersResult resetIncludeGroup(String currentUser, String groupingPath) {
        log.info("resetIncludeGroup; currentUser: " + currentUser + "; groupingPath: " + groupingPath);
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getIncludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from the exclude group at groupingPath.
     */
    public GroupingReplaceGroupMembersResult resetExcludeGroup(String currentUser, String groupingPath) {
        log.info("resetExcludeGroup; currentUser: " + currentUser + "; groupingPath: " + groupingPath);
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getExcludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from group at groupPath.
     */
    private GroupingReplaceGroupMembersResult resetGroup(String groupPath) {
        AddMembersResults addMembersResults = grouperApiService.resetGroupMembers(groupPath);
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult(addMembersResults);
        timestampService.update(result);
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

    private GroupingMoveMembersResult moveGroupMembers(String addGroupPath, String removeGroupPath,
            List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults = grouperApiService.removeMembers(removeGroupPath, uhIdentifiers);
        AddMembersResults addMembersResults = grouperApiService.addMembers(addGroupPath, uhIdentifiers);
        GroupingMoveMembersResult result = new GroupingMoveMembersResult(addMembersResults, removeMembersResults);
        timestampService.update(result.getAddResults());
        timestampService.update(result.getRemoveResults());
        return result;
    }

    private GroupingMoveMemberResult moveGroupMember(String addGroupPath, String removeGroupPath,
            String uhIdentifier) {
        RemoveMemberResult removeMemberResult = grouperApiService.removeMember(removeGroupPath, uhIdentifier);
        AddMemberResult addMemberResult = grouperApiService.addMember(addGroupPath, uhIdentifier);
        GroupingMoveMemberResult result = new GroupingMoveMemberResult(addMemberResult, removeMemberResult);
        timestampService.update(result.getAddResult());
        timestampService.update(result.getRemoveResult());
        return result;
    }

    private GroupingRemoveResults removeGroupMembers(String groupPath, List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults = grouperApiService.removeMembers(groupPath, uhIdentifiers);
        GroupingRemoveResults results = new GroupingRemoveResults(removeMembersResults);
        timestampService.update(results);
        return results;
    }

    private GroupingRemoveResult removeGroupMember(String groupPath, String uhIdentifier) {
        RemoveMemberResult removeMemberResult = grouperApiService.removeMember(groupPath, uhIdentifier);
        GroupingRemoveResult result = new GroupingRemoveResult(removeMemberResult);
        timestampService.update(result);
        return result;
    }

    private GroupingAddResults addOwners(String groupingPath, List<String> uhIdentifiers) {
        AddMembersResults addMembersResults =
                grouperApiService.addMembers(groupingPath + GroupType.OWNERS.value(), uhIdentifiers);
        GroupingAddResults results = new GroupingAddResults(addMembersResults);
        timestampService.update(results);
        return results;
    }

    private GroupingAddResult addOwner(String groupingPath, String uhIdentifier) {
        AddMemberResult addMemberResult =
                grouperApiService.addMember(groupingPath + GroupType.OWNERS.value(), uhIdentifier);
        GroupingAddResult result = new GroupingAddResult(addMemberResult);
        timestampService.update(result);
        return result;
    }

    private GroupingRemoveResults removeOwners(String groupingPath, List<String> uhIdentifiers) {
        RemoveMembersResults removeMembersResults =
                grouperApiService.removeMembers(groupingPath + GroupType.OWNERS.value(), uhIdentifiers);
        GroupingRemoveResults results = new GroupingRemoveResults(removeMembersResults);
        timestampService.update(results);
        return results;
    }

    private GroupingRemoveResult removeOwner(String groupingPath, String uhIdentifier) {
        RemoveMemberResult removeMemberResult =
                grouperApiService.removeMember(groupingPath + GroupType.OWNERS.value(), uhIdentifier);
        GroupingRemoveResult result = new GroupingRemoveResult(removeMemberResult);
        timestampService.update(result);
        return result;
    }
}
