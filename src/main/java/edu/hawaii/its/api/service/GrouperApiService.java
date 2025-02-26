package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

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
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsCommand;
import edu.hawaii.its.api.wrapper.SubjectsResults;

public class GrouperApiService implements GrouperService {

    private final ExecutorService exec;
    
    private final boolean RETRY = true;
    private final boolean NO_RETRY = false;

    public GrouperApiService(ExecutorService exec) {
        this.exec = exec;
    }

    /**
     * Check if a UH identifier is listed in a group.
     */
    public HasMembersResults hasMemberResults(String groupPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = exec.execute(NO_RETRY, new HasMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier));
        return hasMembersResults;
    }

    /**
     * Check if multiple UH identifiers are listed in a group.
     */
    public HasMembersResults hasMembersResults(String currentUser, String groupPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = exec.execute(NO_RETRY, new HasMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
        return hasMembersResults;
    }

    /**
     * Update a groups description.
     */
    public GroupSaveResults groupSaveResults(String groupingPath, String description) {
        GroupSaveResults groupSaveResults = exec.execute(NO_RETRY, new GroupSaveCommand()
                .setGroupingPath(groupingPath)
                .setDescription(description));
        return groupSaveResults;
    }

    /**
     * Check if a group exists.
     */
    public FindGroupsResults findGroupsResults(String groupPath) {
        FindGroupsResults findGroupsResults = exec.execute(NO_RETRY, new FindGroupsCommand()
                .addPath(groupPath));
        return findGroupsResults;
    }

    public FindGroupsResults findGroupsResults(String currentUser, String groupPath) {
        FindGroupsResults findGroupsResults = exec.execute(NO_RETRY ,new FindGroupsCommand()
                .owner(currentUser)
                .addPath(groupPath));
        return findGroupsResults;
    }

    /**
     * Check if multiple groups exist.
     */
    public FindGroupsResults findGroupsResults(List<String> groupPaths) {
        FindGroupsResults findGroupsResults = exec.execute(NO_RETRY, new FindGroupsCommand()
                .addPaths(groupPaths));
        return findGroupsResults;
    }

    /**
     * Check if a UH identifier is valid.
     */
    public SubjectsResults getSubjects(String uhIdentifier) {
        SubjectsResults subjectsResults = exec.execute(NO_RETRY, new SubjectsCommand()
                .addSubject(uhIdentifier));
        return subjectsResults;
    }

    /**
     * Check if multiple UH identifiers are valid.
     */
    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        SubjectsResults subjectsResults = exec.execute(NO_RETRY, new SubjectsCommand()
                .addSubjects(uhIdentifiers));
        return subjectsResults;
    }

    /**
     * Get a list of members for a specific grouping path with search string
     */
    @Cacheable("search")
    public SubjectsResults getSubjects(String groupingPath, String searchString) {
        SubjectsResults subjectsResults = exec.execute(NO_RETRY, new SubjectsCommand()
                .assignGroupingPath(groupingPath)
                .assignSearchString(searchString));
        return subjectsResults;
    }

    /**
     * Get all the groups with the specified attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttribute(attribute));
    }

    /**
     * Get all the groups with the specified attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttributes(attributes));
    }

    /**
     * Check if a group contains an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, String groupPath) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroup(groupPath));
    }

    /**
     * Check if multiple groups contain an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroups(groupPaths));
    }

    /**
     * Check if a group contains multiple attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, String groupPath) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroup(groupPath));
    }

    public GroupAttributeResults groupAttributeResults(String currentUser, List<String> attributes, String groupPath) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .owner(currentUser)
                .addAttributes(attributes)
                .addGroup(groupPath));
    }

    /**
     * Check if multiple groups contain attributes from the list specified.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, List<String> groupPaths) {
        return exec.execute(NO_RETRY, new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroups(groupPaths));
    }

    /**
     * Get all listed attributes of a group.
     */
    public GroupAttributeResults groupAttributeResult(String groupPath) {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand()
                .addGroup(groupPath);
        return exec.execute(NO_RETRY, groupAttributeCommand);
    }

    public GroupAttributeResults groupAttributeResult(String currentUser, String groupPath) {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand()
                .owner(currentUser)
                .addGroup(groupPath);
        return exec.execute(NO_RETRY, groupAttributeCommand);
    }

    /**
     * Get all groups that a UH identifier is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier) {
        return exec.execute(NO_RETRY, new GetGroupsCommand()
                .addUhIdentifier(uhIdentifier)
                .query(""));
    }

    /**
     * Get all groups that a UH identifier is listed in with respect to the query string, e.g. passing
     * getGroupsResults("some-identifier", "tmp") will return all the groups with a group path starting with the string
     * "tmp" that "some-identifier" is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier, String query) {
        return exec.execute(NO_RETRY, new GetGroupsCommand()
                .addUhIdentifier(uhIdentifier)
                .query(query));
    }

    /**
     * Get all members listed in a group.
     */
    public GetMembersResult getMembersResult(String currentUser, String groupPath) {
        GetMembersResults getMembersResults = exec.execute(NO_RETRY, new GetMembersCommand()
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
        GetMembersResults getMembersResults = exec.execute(NO_RETRY, new GetMembersCommand()
                .addGroupPaths(groupPaths));
        return getMembersResults;
    }

    /**
     * Find all group attributes containing the specified attribute type, e.g. passing the sync-dest attribute type name
     * will return a list of all sync-destinations. All sync-destinations are listed as a distinct attribute each
     * containing a matching sync-dest attribute type string.
     */
    public FindAttributesResults findAttributesResults(String attributeTypeName, String searchScope) {
        return exec.execute(NO_RETRY, new FindAttributesCommand()
                .assignAttributeName(attributeTypeName)
                .assignSearchScope(searchScope));
    }

    /**
     * Same as findAttributesResults(String attributeTypeName, String searchScope) except the currentUser is used to
     * implement the "act-as" requirements."
     */
    public FindAttributesResults findAttributesResults(String currentUser, String attributeTypeName,
            String searchScope) {
        return exec.execute(NO_RETRY, new FindAttributesCommand()
                .owner(currentUser)
                .assignAttributeName(attributeTypeName)
                .assignSearchScope(searchScope));
    }

    /**
     * Add a UH identifier to group listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public AddMemberResult addMember(String currentUser, String groupPath, String uhIdentifier) {
        return exec.execute(NO_RETRY, new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    /**
     * Add multiple UH identifiers to a group listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return exec.execute(NO_RETRY, new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
    }

    /**
     * Add multiple path owners to a group owner listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public AddMembersResults addGroupPathOwners(String currentUser, String groupPath, List<String> groupPathOwners) {
        return exec.execute(NO_RETRY, new AddMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addGroupPathOwners(groupPathOwners));
    }

    /**
     * Remove a UH identifier from a group listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        return exec.execute(NO_RETRY, new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    /**
     * Remove multiple UH identifiers from a group listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return exec.execute(NO_RETRY, new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
    }

    /**
     * Remove multiple path owners from a group owner listing.
     */
    @CacheEvict(value="search", key="#groupPath")
    public RemoveMembersResults removeGroupPathOwners(String currentUser, String groupPath,
            List<String> groupPathOwners) {
        return exec.execute(NO_RETRY, new RemoveMembersCommand()
                .owner(currentUser)
                .assignGroupPath(groupPath)
                .addGroupPathOwners(groupPathOwners));
    }

    /**
     * Remove all listed members from a group.
     */
    @CacheEvict(value="search", key="#groupPath")
    public AddMembersResults resetGroupMembers(String groupPath) {
        return exec.execute(NO_RETRY, new AddMembersCommand()
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
        return exec.execute(RETRY, new AssignAttributesCommand()
                .owner(currentUser)
                .setAssignType(assignType)
                .setAssignOperation(assignOperation)
                .addGroupPath(groupPath)
                .addAttribute(attributeName));
    }

    /**
     * Change a group attribute's privilege to true or false.
     */
    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath,
            String privilegeName,
            String uhIdentifier, boolean isAllowed) {
        return exec.execute(RETRY, new AssignGrouperPrivilegesCommand()
                .owner(currentUser)
                .setGroupPath(groupPath)
                .setPrivilege(privilegeName)
                .setSubjectLookup(uhIdentifier)
                .setIsAllowed(isAllowed));
    }

    /**
     * Get all members listed in a group.
     */
    public GetMembersResult getMembersResult(String currentUser, String groupPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        GetMembersResults getMembersResults = exec.execute(NO_RETRY, new GetMembersCommand()
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
        return exec.execute(NO_RETRY, new GetMembersCommand()
                .owner(currentUser)
                .addGroupPaths(groupPaths)
                .setPageNumber(pageNumber)
                .setPageSize(pageSize)
                .setAscending(isAscending)
                .sortBy(sortString));
    }
}
