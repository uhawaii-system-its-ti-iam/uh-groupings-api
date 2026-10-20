package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersCommand;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesCommand;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesCommand;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.FindAttributesCommand;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsCommand;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsCommand;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersCommand;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeCommand;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveCommand;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersCommand;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.MemberFilter;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsCommand;
import edu.hawaii.its.api.wrapper.SubjectsResults;

public class GrouperApiService implements GrouperService {

    private static final IntConsumer IGNORE_BATCHES = count -> {
    };

    private final ExecutorService exec;

    /**
     * Sends the bulk lookup, add and remove to Grouper in batches.
     */
    private final BatchExecutor batchExecutor;

    public GrouperApiService(ExecutorService exec, BatchExecutor batchExecutor) {
        this.exec = exec;
        this.batchExecutor = batchExecutor;
    }

    /**
     * Check if a UH identifier is listed in a group.
     */
    public HasMembersResults hasMemberResults(String groupPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = exec.execute(new HasMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier));
        return hasMembersResults;
    }

    /**
     * Check if multiple UH identifiers are listed in a group.
     */
    public HasMembersResults hasMembersResults(String currentUser, String groupPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = exec.execute(new HasMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
        return hasMembersResults;
    }

    /**
     * Update a groups description.
     */
    public GroupSaveResults groupSaveResults(String groupingPath, String description) {
        GroupSaveResults groupSaveResults = exec.execute(new GroupSaveCommand()
                .setGroupingPath(groupingPath)
                .setDescription(description));
        return groupSaveResults;
    }

    /**
     * Check if a group exists.
     */
    public FindGroupsResults findGroupsResults(String groupPath) {
        FindGroupsResults findGroupsResults = exec.execute(new FindGroupsCommand()
                .addPath(groupPath));
        return findGroupsResults;
    }

    public FindGroupsResults findGroupsResults(String currentUser, String groupPath) {
        FindGroupsResults findGroupsResults = exec.execute(new FindGroupsCommand()
                .owner(currentUser)
                .addPath(groupPath));
        return findGroupsResults;
    }

    /**
     * Check if multiple groups exist.
     */
    public FindGroupsResults findGroupsResults(List<String> groupPaths) {
        FindGroupsResults findGroupsResults = exec.execute(new FindGroupsCommand()
                .addPaths(groupPaths));
        return findGroupsResults;
    }

    /**
     * Check if a UH identifier is valid.
     */
    public SubjectsResults getSubjects(String uhIdentifier) {
        SubjectsResults subjectsResults = exec.execute(new SubjectsCommand()
                .addSubject(uhIdentifier));
        return subjectsResults;
    }

    /**
     * Check if multiple UH identifiers are valid. A long list is looked up in batches (see BatchExecutor).
     */
    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        return getSubjects(uhIdentifiers, null, IGNORE_BATCHES);
    }

    /**
     * Check if multiple UH identifiers are valid, looking them up in the subject source sourceId only, or in every
     * source when sourceId is null. A long list is looked up in batches (see BatchExecutor); onBatchDone is called
     * with the number of identifiers in each batch that Grouper has answered.
     */
    public SubjectsResults getSubjects(List<String> uhIdentifiers, String sourceId, IntConsumer onBatchDone) {
        return batchExecutor.lookup(uhIdentifiers, batch -> new SubjectsCommand()
                .addSubjects(batch, sourceId), SubjectsResults::merge, onBatchDone);
    }

    /**
     * Get a list of members for a specific grouping path with search string
     */
    public SubjectsResults getSubjects(String groupingPath, String searchString) {
        SubjectsResults subjectsResults = exec.execute(new SubjectsCommand()
                .assignGroupingPath(groupingPath)
                .assignSearchString(searchString));
        return subjectsResults;
    }

    /**
     * Get all immediate members of a grouping path (members with the "IMMEDIATE" filter)
     */
    public GetMembersResult getImmediateMembers(String currentUser, String groupPath) {
        return getImmediateMembers(currentUser, groupPath, true);
    }

    /**
     * Get all immediate members of a group path. Without subject details, each member carries only its subject id
     * (its UH number, or a group's uuid), which Grouper lists several times faster than members with details.
     */
    public GetMembersResult getImmediateMembers(String currentUser, String groupPath, boolean includeSubjectDetail) {
        MemberFilter memberFilter = MemberFilter.IMMEDIATE;
        GetMembersResults getMembersResults = exec.execute(new GetMembersCommand()
                .owner(currentUser)
                .addGroupPath(groupPath)
                .assignMemberFilter(memberFilter)
                .includeSubjectDetail(includeSubjectDetail));
        List<GetMembersResult> result = getMembersResults.getMembersResults();
        if (result.isEmpty()) {
            return new GetMembersResult();
        }
        return result.get(0);
    }

    /**
     * Get all members of a grouping (members with the "ALL" filter)
     */
    public GetMembersResult getAllMembers(String currentUser, String groupPath) {
        MemberFilter memberFilter = MemberFilter.ALL;
        GetMembersResults getMembersResults = exec.execute(new GetMembersCommand()
                .owner(currentUser)
                .addGroupPath(groupPath)
                .assignMemberFilter(memberFilter));
        List<GetMembersResult> result = getMembersResults.getMembersResults();
        if (result.isEmpty()) {
            return new GetMembersResult();
        }
        return result.get(0);
    }

    /**
     * Get all the groups with the specified attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute));
    }

    /**
     * Get all the groups with the specified attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes));
    }

    /**
     * Check if a group contains an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, String groupPath) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroup(groupPath));
    }

    /**
     * Check if multiple groups contain an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroups(groupPaths));
    }

    /**
     * Check if a group contains multiple attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, String groupPath) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroup(groupPath));
    }

    public GroupAttributeResults groupAttributeResults(String currentUser, List<String> attributes, String groupPath) {
        return exec.execute(new GroupAttributeCommand()
                .owner(currentUser)
                .addAttributes(attributes)
                .addGroup(groupPath));
    }

    /**
     * Check if multiple groups contain attributes from the list specified.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, List<String> groupPaths) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroups(groupPaths));
    }

    /**
     * Get all listed attributes of a group.
     */
    public GroupAttributeResults groupAttributeResult(String groupPath) {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand()
                .addGroup(groupPath);
        return exec.execute(groupAttributeCommand);
    }

    public GroupAttributeResults groupAttributeResult(String currentUser, String groupPath) {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand()
                .owner(currentUser)
                .addGroup(groupPath);
        return exec.execute(groupAttributeCommand);
    }

    /**
     * Get all groups that a uhIdentifier is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier) {
        return exec.execute(new GetGroupsCommand()
                .addUhIdentifier(uhIdentifier)
                .query(""));
    }

    /**
     * Get all groups that a UH identifier is listed in with respect to the query string, e.g. passing
     * getGroupsResults("some-identifier", "tmp") will return all the groups with a group path starting with the string
     * "tmp" that "some-identifier" is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier, String query) {
        return exec.execute(new GetGroupsCommand()
                .addUhIdentifier(uhIdentifier)
                .query(query));
    }

    /**
     * Get all groups that each of the groupPaths is listed in. The groupPaths are looked up as group subjects, so a
     * single call to grouper answers this for every path passed.
     */
    public GetGroupsResults getGroupsOfGroups(List<String> groupPaths) {
        return exec.execute(new GetGroupsCommand()
                .addGroupPaths(groupPaths)
                .query(""));
    }

    /**
     * Get all members listed in a group.
     */
    public GetMembersResult getMembersResult(String currentUser, String groupPath) {
        GetMembersResults getMembersResults = exec.execute(new GetMembersCommand()
                .owner(currentUser)
                .addGroupPath(groupPath));
        List<GetMembersResult> result = getMembersResults.getMembersResults();
        if (result.isEmpty()) {
            return new GetMembersResult();
        }
        return result.get(0);
    }

    /**
     * Get all members listed in each group.
     */
    public GetMembersResults getMembersResults(List<String> groupPaths) {
        GetMembersResults getMembersResults = exec.execute(new GetMembersCommand()
                .addGroupPaths(groupPaths));
        return getMembersResults;
    }

    /**
     * Find all group attributes containing the specified attribute type, e.g. passing the sync-dest attribute type name
     * will return a list of all sync-destinations. All sync-destinations are listed as a distinct attribute each
     * containing a matching sync-dest attribute type string.
     */
    public FindAttributesResults findAttributesResults(String attributeTypeName, String searchScope) {
        return exec.execute(new FindAttributesCommand()
                .assignAttributeName(attributeTypeName)
                .assignSearchScope(searchScope));
    }

    /**
     * Same as findAttributesResults(String attributeTypeName, String searchScope) except the currentUser is used to
     * implement the "act-as" requirements."
     */
    public FindAttributesResults findAttributesResults(String currentUser, String attributeTypeName,
            String searchScope) {
        return exec.execute(new FindAttributesCommand()
                .owner(currentUser)
                .assignAttributeName(attributeTypeName)
                .assignSearchScope(searchScope));
    }

    /**
     * Add a UH identifier to group listing.
     */
    public AddMemberResult addMember(String currentUser, String groupPath, String uhIdentifier) {
        return exec.execute(new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    /**
     * Add multiple UH identifiers to a group listing. A long list is added in batches (see BatchExecutor).
     */
    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return addMembers(currentUser, groupPath, uhIdentifiers, Map.of(), IGNORE_BATCHES);
    }

    /**
     * Add multiple UH identifiers to a group listing, each looked up only in its subject source in sourceIds, or in
     * every source when it has none there. A long list is added in batches (see BatchExecutor); onBatchDone is called
     * with the number of identifiers in each batch that Grouper has answered.
     * Grouper's GcAddMember client rejects a zero-subject request outright ("Need at least one subject to
     * add to group") since it's ambiguous whether that means "add nothing" or a caller mistake, so an empty
     * list is short-circuited here instead of being sent to Grouper.
     */
    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers,
            Map<String, String> sourceIds, IntConsumer onBatchDone) {
        if (uhIdentifiers.isEmpty()) {
            return new AddMembersResults();
        }
        return batchExecutor.add(groupPath, uhIdentifiers, batch -> new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(batch, sourceIds), AddMembersResults::merge, onBatchDone);
    }

    /**
     * Add multiple owner-groupings to a group owner listing.
     */
    public AddMembersResults addOwnerGroupings(String currentUser, String groupPath, List<String> ownerGroupings) {
        if (ownerGroupings.isEmpty()) {
            return new AddMembersResults();
        }
        return exec.execute(new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addOwnerGroupings(ownerGroupings));
    }

    /**
     * Remove a UH identifier from a group listing.
     */
    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        return exec.execute(new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    /**
     * Remove multiple UH identifiers from a group listing. A long list is removed in batches (see BatchExecutor).
     */
    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return removeMembers(currentUser, groupPath, uhIdentifiers, Map.of(), IGNORE_BATCHES);
    }

    /**
     * Remove multiple UH identifiers from a group listing, each looked up only in its subject source in sourceIds, or
     * in every source when it has none there. A long list is removed in batches (see BatchExecutor); onBatchDone is
     * called with the number of identifiers in each batch that Grouper has answered.
     * Mirrors the addMembers guard above: GcDeleteMember has no "replace all" escape hatch at all, so it
     * always rejects a zero-subject request.
     */
    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers,
            Map<String, String> sourceIds, IntConsumer onBatchDone) {
        if (uhIdentifiers.isEmpty()) {
            return new RemoveMembersResults();
        }
        return batchExecutor.remove(uhIdentifiers, batch -> new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(batch, sourceIds), RemoveMembersResults::merge, onBatchDone);
    }

    /**
     * Remove multiple owner-groupings from a group owner listing.
     */
    public RemoveMembersResults removeOwnerGroupings(String currentUser, String groupPath,
            List<String> ownerGroupings) {
        if (ownerGroupings.isEmpty()) {
            return new RemoveMembersResults();
        }
        return exec.execute(new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addOwnerGroupings(ownerGroupings));
    }

    /**
     * Remove all listed members from a group.
     */
    public AddMembersResults resetGroupMembers(String groupPath) {
        return exec.execute(new AddMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifiers(new ArrayList<>())
                .replaceGroupMembers(true));
    }

    /**
     * Add or remove an attribute from a group. This is used to update a groupings
     * preferences.
     */
    public AssignAttributesResults assignAttributesResults(String currentUser, String assignType,
            String assignOperation, String groupPath,
            String attributeName) {
        return exec.execute(new AssignAttributesCommand()
                .owner(currentUser)
                .setAssignType(assignType)
                .setAssignOperation(assignOperation)
                .addGroupPath(groupPath)
                .addAttribute(attributeName));
    }

    /**
     * Add or remove an attribute from a group. This is used to update a groupings
     * preferences.
     * @param retry used to retry execution if it fails.
     */
    public AssignAttributesResults assignAttributesResults(String currentUser, String assignType,
                                                           String assignOperation, String groupPath,
                                                           String attributeName, boolean retry) {
        return exec.execute(new AssignAttributesCommand()
                .owner(currentUser)
                .setAssignType(assignType)
                .setAssignOperation(assignOperation)
                .addGroupPath(groupPath)
                .addAttribute(attributeName)
                .setRetry(retry));
    }

    /**
     * Change a group attribute's privilege to true or false.
     */
    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath,
            String privilegeName,
            String uhIdentifier, boolean isAllowed) {
        return exec.execute(new AssignGrouperPrivilegesCommand()
                .owner(currentUser)
                .setGroupPath(groupPath)
                .setPrivilege(privilegeName)
                .setSubjectLookup(uhIdentifier)
                .setIsAllowed(isAllowed));
    }

    /**
     * Change a group attribute's privilege to true or false.
     * @param retry used to retry execution if it fails.
     */
    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath,
                                                                       String privilegeName,
                                                                       String uhIdentifier, boolean isAllowed,
                                                                       boolean retry) {
        return exec.execute(new AssignGrouperPrivilegesCommand()
                .owner(currentUser)
                .setGroupPath(groupPath)
                .setPrivilege(privilegeName)
                .setSubjectLookup(uhIdentifier)
                .setIsAllowed(isAllowed)
                .setRetry(retry));
    }

    /**
     * Get all members listed in a group.
     */
    public GetMembersResult getMembersResult(String currentUser, String groupPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        GetMembersResults getMembersResults = exec.execute(new GetMembersCommand()
                .owner(currentUser)
                .addGroupPath(groupPath)
                .setPageNumber(pageNumber)
                .setPageSize(pageSize)
                .setAscending(isAscending)
                .sortBy(sortString));
        List<GetMembersResult> result = getMembersResults.getMembersResults();
        if (result.isEmpty()) {
            return new GetMembersResult();
        }
        return result.get(0);
    }

    /**
     * Get a list of members for each groupPath.
     */
    public GetMembersResults getMembersResults(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        return exec.execute(new GetMembersCommand()
                .owner(currentUser)
                .addGroupPaths(groupPaths)
                .setPageNumber(pageNumber)
                .setPageSize(pageSize)
                .setAscending(isAscending)
                .sortBy(sortString));
    }
}
