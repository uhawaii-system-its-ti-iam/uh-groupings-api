package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsAddResults;
import edu.hawaii.its.api.groupings.GroupingsMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingsMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResults;
import edu.hawaii.its.api.groupings.GroupingsReplaceGroupMembersResult;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.AddMemberCommand;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersCommand;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberCommand;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.ReplaceGroupMembersCommand;
import edu.hawaii.its.api.wrapper.ReplaceGroupMembersResult;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    private UpdateTimestampService updateTimestamp;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private ExecutorService executor;

    public GroupingsAddResult addAdmin(String currentUser, String uhIdentifier) {
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return new GroupingsAddResult(executor.execute(new AddMemberCommand(GROUPING_ADMINS, validUhUuid)));
    }

    public GroupingsRemoveResult removeAdmin(String currentUser, String uhIdentifier) {
        checkIfAdminUser(currentUser);
        String validUhUuid = subjectService.getValidUhUuid(uhIdentifier);
        if (validUhUuid.equals("")) {
            throw new UhMemberNotFoundException(validUhUuid);
        }
        return new GroupingsRemoveResult(new RemoveMemberCommand(GROUPING_ADMINS, validUhUuid).execute());
    }

    public GroupingsAddResults addOwnerships(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return addOwners(groupingPath, validIdentifiers);
    }

    public GroupingsAddResult addOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return addOwner(groupingPath, validIdentifier);
    }

    public GroupingsRemoveResults removeOwnerships(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && memberAttributeService.isAdmin(currentUser)) {
            addOwnerships(currentUser, groupingPath, Arrays.asList(currentUser));
        }
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return removeOwners(groupingPath, validIdentifiers);
    }

    public GroupingsRemoveResult removeOwnership(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && memberAttributeService.isAdmin(currentUser)) {
            addOwnership(currentUser, groupingPath, currentUser);
        }
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return removeOwner(groupingPath, validIdentifier);
    }

    public GroupingsMoveMembersResult addIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                validIdentifiers);
    }

    public GroupingsMoveMemberResult addIncludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                validIdentifier);
    }

    public GroupingsMoveMembersResult addExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        List<String> validIdentifiers = subjectService.getValidUhUuids(uhIdentifiers);
        return moveGroupMembers(groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.INCLUDE.value(), validIdentifiers);
    }

    public GroupingsMoveMemberResult addExcludeMember(String currentUser, String groupingPath, String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.EXCLUDE.value(), groupingPath + GroupType.INCLUDE.value(),
                validIdentifier);
    }

    public GroupingsRemoveResults removeIncludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(groupingPath + GroupType.INCLUDE.value(), uhIdentifiers);
    }

    public GroupingsRemoveResult removeIncludeMember(String currentUser, String groupingPath,
            String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public GroupingsRemoveResults removeExcludeMembers(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMembers(groupingPath + GroupType.EXCLUDE.value(), uhIdentifiers);
    }

    public GroupingsRemoveResult removeExcludeMember(String currentUser, String groupingPath,
            String uhIdentifier) {
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        return removeGroupMember(groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public GroupingsMoveMemberResult optIn(String currentUser, String groupingPath, String uhIdentifier) {
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.INCLUDE.value(), groupingPath + GroupType.EXCLUDE.value(),
                uhIdentifier);
    }

    public GroupingsMoveMemberResult optOut(String currentUser, String groupingPath, String uhIdentifier) {
        checkIfSelfOptOrAdmin(currentUser, uhIdentifier);
        return moveGroupMember(groupingPath + GroupType.EXCLUDE.value(), groupingPath + GroupType.INCLUDE.value(),
                uhIdentifier);
    }

    public GroupingsRemoveResults removeFromGroups(String currentUser, String uhIdentifier, List<String> groupPaths) {
        checkIfAdminUser(currentUser);
        String validIdentifier = subjectService.getValidUhUuid(uhIdentifier);
        GroupingsRemoveResults groupingsRemoveResults = new GroupingsRemoveResults();
        for (String groupPath : groupPaths) {
            if (!groupPath.endsWith(GroupType.OWNERS.value()) &&
                    !groupPath.endsWith(GroupType.EXCLUDE.value()) &&
                    !groupPath.endsWith(GroupType.INCLUDE.value())) {
                throw new GcWebServiceError("404: Invalid group path.");
            }
            groupingsRemoveResults.add(removeGroupMember(groupPath, validIdentifier));
        }
        return groupingsRemoveResults;
    }

    /**
     * Remove all members from the include group at groupingPath.
     */
    public GroupingsReplaceGroupMembersResult resetIncludeGroup(String currentUser, String groupingPath) {
        log.info("resetIncludeGroup; currentUser: " + currentUser + "; groupingPath: " + groupingPath);
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getIncludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from the exclude group at groupingPath.
     */
    public GroupingsReplaceGroupMembersResult resetExcludeGroup(String currentUser, String groupingPath) {
        log.info("resetExcludeGroup; currentUser: " + currentUser + "; groupingPath: " + groupingPath);
        groupPathService.checkPath(groupingPath);
        checkIfOwnerOrAdminUser(currentUser, groupingPath);
        String groupPath = groupPathService.getExcludeGroup(groupingPath);
        return resetGroup(groupPath);
    }

    /**
     * Remove all members from group at groupPath.
     */
    private GroupingsReplaceGroupMembersResult resetGroup(String groupPath) {
        ReplaceGroupMembersResult replaceGroupMembersResult = new ReplaceGroupMembersCommand(groupPath).execute();
        GroupingsReplaceGroupMembersResult result = new GroupingsReplaceGroupMembersResult(replaceGroupMembersResult);
        updateTimestamp.replaceGroupMembersResult(result);
        return result;
    }

    public void checkIfOwnerOrAdminUser(String currentUser, String groupingPath) {
        if (!memberAttributeService.isOwner(groupingPath, currentUser) && !memberAttributeService.isAdmin(
                currentUser)) {
            throw new AccessDeniedException();
        }

    }

    public void checkIfAdminUser(String currentUser) {
        if (!memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
    }

    public void checkIfSelfOptOrAdmin(String currentUser, String identifier) {
        if (!currentUser.equals(identifier) && !memberAttributeService.isAdmin(currentUser)) {
            throw new AccessDeniedException();
        }
    }

    private GroupingsMoveMembersResult moveGroupMembers(String addGroupPath, String removeGroupPath,
            List<String> usersToAdd) {
        RemoveMembersResults removeMembersResults = new RemoveMembersCommand(removeGroupPath, usersToAdd).execute();
        AddMembersResults addMembersResults = new AddMembersCommand(addGroupPath, usersToAdd).execute();

        GroupingsMoveMembersResult result = new GroupingsMoveMembersResult(addMembersResults, removeMembersResults);
        updateTimestamp.addResults(result.getAddResults());
        updateTimestamp.removeResults(result.getRemoveResults());
        return result;
    }

    private GroupingsMoveMemberResult moveGroupMember(String addGroupPath, String removeGroupPath, String userToAdd) {
        RemoveMemberResult removeMemberResult = new RemoveMemberCommand(removeGroupPath, userToAdd).execute();
        AddMemberResult addMemberResult = new AddMemberCommand(addGroupPath, userToAdd).execute();
        GroupingsMoveMemberResult result = new GroupingsMoveMemberResult(addMemberResult, removeMemberResult);
        updateTimestamp.addResult(result.getAddResult());
        updateTimestamp.removeResult(result.getRemoveResult());
        return result;
    }

    private GroupingsRemoveResults removeGroupMembers(String groupPath, List<String> usersToRemove) {
        RemoveMembersResults removeMembersResults = new RemoveMembersCommand(groupPath, usersToRemove).execute();
        GroupingsRemoveResults results = new GroupingsRemoveResults(removeMembersResults);
        updateTimestamp.removeResults(results);
        return results;
    }

    private GroupingsRemoveResult removeGroupMember(String groupPath, String userToRemove) {
        RemoveMemberResult removeMemberResult = new RemoveMemberCommand(groupPath, userToRemove).execute();
        GroupingsRemoveResult result = new GroupingsRemoveResult(removeMemberResult);
        updateTimestamp.removeResult(result);
        return result;
    }

    private GroupingsAddResults addOwners(String groupingPath, List<String> usersToAdd) {
        AddMembersResults addMembersResults =
                new AddMembersCommand(groupingPath + GroupType.OWNERS.value(), usersToAdd).execute();
        GroupingsAddResults results = new GroupingsAddResults(addMembersResults);
        updateTimestamp.addResults(results);
        return results;
    }

    private GroupingsAddResult addOwner(String groupingPath, String userToAdd) {
        AddMemberResult addMemberResult =
                new AddMemberCommand(groupingPath + GroupType.OWNERS.value(), userToAdd).execute();
        GroupingsAddResult result = new GroupingsAddResult(addMemberResult);
        updateTimestamp.addResult(result);
        return result;
    }

    private GroupingsRemoveResults removeOwners(String groupingPath, List<String> ownersToRemove) {
        RemoveMembersResults removeMembersResults =
                new RemoveMembersCommand(groupingPath + GroupType.OWNERS.value(), ownersToRemove).execute();
        GroupingsRemoveResults results = new GroupingsRemoveResults(removeMembersResults);
        updateTimestamp.removeResults(results);
        return results;
    }

    private GroupingsRemoveResult removeOwner(String groupingPath, String ownerToRemove) {
        RemoveMemberResult removeMemberResult =
                new RemoveMemberCommand(groupingPath + GroupType.OWNERS.value(), ownerToRemove).execute();
        GroupingsRemoveResult result = new GroupingsRemoveResult(removeMemberResult);
        updateTimestamp.removeResult(result);
        return result;
    }

}
