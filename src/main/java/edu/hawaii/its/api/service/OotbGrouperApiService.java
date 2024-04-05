package edu.hawaii.its.api.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

public class OotbGrouperApiService implements GrouperService {

    public static final Log log = LogFactory.getLog(OotbGrouperApiService.class);
    @Autowired
    OotbGroupingPropertiesService ootbGroupingPropertiesService;

    /**
     * Check if a UH identifier is listed in a group.
     */
    public HasMembersResults hasMemberResults(String groupPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = ootbGroupingPropertiesService.getHasMembersResultsBean();
        return hasMembersResults;
    }

    /**
     * Check if multiple UH identifiers are listed in a group.
     */
    public HasMembersResults hasMembersResults(String groupPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = ootbGroupingPropertiesService.getHasMembersResultsBean();
        return hasMembersResults;
    }

    /**
     * Update a groups description.
     */
    public GroupSaveResults groupSaveResults(String groupingPath, String description) {
        GroupSaveResults groupSaveResults = ootbGroupingPropertiesService.getGroupSaveResults();
        return groupSaveResults;
    }

    /**
     * Check if a group exists.
     */
    public FindGroupsResults findGroupsResults(String groupPath) {
        FindGroupsResults findGroupsResults = ootbGroupingPropertiesService.getFindGroupsResults();
        return findGroupsResults;
    }

    public FindGroupsResults findGroupsResults(String currentUser, String groupPath) {
        FindGroupsResults findGroupsResults = ootbGroupingPropertiesService.getFindGroupsResults();
        return findGroupsResults;
    }

    /**
     * Check if multiple groups exist.
     */
    public FindGroupsResults findGroupsResults(List<String> groupPaths) {
        FindGroupsResults findGroupsResults = ootbGroupingPropertiesService.getFindGroupsResults();
        return findGroupsResults;
    }

    /**
     * Check if a UH identifier is valid.
     */
    public SubjectsResults getSubjects(String uhIdentifier) {
        ootbGroupingPropertiesService.updateSubjectsByUhIdentifier(uhIdentifier);

        return ootbGroupingPropertiesService.getSubjectsResults();
    }

    /**
     * Check if multiple UH identifiers are valid.
     */
    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        ootbGroupingPropertiesService.updateSubjectsByUhIdentifiers(uhIdentifiers);

        return ootbGroupingPropertiesService.getSubjectsResults();
    }

    /**
     * Get all the groups with the specified attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Get all the groups with the specified attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Check if a group contains an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, String groupPath) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Check if multiple groups contain an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Check if a group contains multiple attributes.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, String groupPath) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    public GroupAttributeResults groupAttributeResults(String currentUser, List<String> attributes, String groupPath) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Check if multiple groups contain attributes from the list specified.
     */
    public GroupAttributeResults groupAttributeResults(List<String> attributes, List<String> groupPaths) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Get all listed attributes of a group.
     */
    public GroupAttributeResults groupAttributeResult(String groupPath) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    public GroupAttributeResults groupAttributeResult(String currentUser, String groupPath) {
        return ootbGroupingPropertiesService.getGroupAttributeResults();
    }

    /**
     * Get all groups that a UH identifier is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier) {
        GetGroupsResults getGroupsResults = ootbGroupingPropertiesService.getGroupsResults();
        return getGroupsResults;
    }

    /**
     * Get all groups that a UH identifier is listed in with respect to the query string, e.g. passing
     * getGroupsResults("some-identifier", "tmp") will return all the groups with a group path starting with the string
     * "tmp" that "some-identifier" is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier, String query) {
        return ootbGroupingPropertiesService.getGroupsResults();
    }

    /**
     * Get all members listed in a group.
     */
    public GetMembersResult getMembersResult(String currentUser, String groupPath) {
        return ootbGroupingPropertiesService.getMembersByGroupPath(groupPath);
    }

    /**
     * Get all members listed in each group.
     */
    public GetMembersResults getMembersResults(List<String> groupPaths) {
        GetMembersResults getMembersResults = ootbGroupingPropertiesService.getMembersResults();
        return getMembersResults;
    }

    /**
     * Find all group attributes containing the specified attribute type, e.g. passing the sync-dest attribute type name
     * will return a list of all sync-destinations. All sync-destinations are listed as a distinct attribute each
     * containing a matching sync-dest attribute type string.
     */
    public FindAttributesResults findAttributesResults(String attributeTypeName, String searchScope) {
        return new FindAttributesResults(new WsFindAttributeDefNamesResults());
    }

    /**
     * Same as findAttributesResults(String attributeTypeName, String searchScope) except the currentUser is used to
     * implement the "act-as" requirements."
     */
    public FindAttributesResults findAttributesResults(String currentUser, String attributeTypeName,
            String searchScope) {
        return new FindAttributesResults(new WsFindAttributeDefNamesResults());
    }

    /**
     * Add a UH identifier to group listing.
     */
    public AddMemberResult addMember(String currentUser, String groupPath, String uhIdentifier) {
        ootbGroupingPropertiesService.addMember(groupPath, uhIdentifier);

        return ootbGroupingPropertiesService.getAddMembersResults().getResults().get(0);
    }

    /**
     * Add multiple UH identifiers to a group listing.
     */
    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        ootbGroupingPropertiesService.addMembers(groupPath, uhIdentifiers);

        return ootbGroupingPropertiesService.getAddMembersResults();
    }

    /**
     * Remove a UH identifier from a group listing.
     */
    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        ootbGroupingPropertiesService.removeMember(groupPath, uhIdentifier);

        return ootbGroupingPropertiesService.getRemoveMembersResults().getResults().get(0);
    }

    /**
     * Remove multiple UH identifiers from a group listing.
     */
    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        ootbGroupingPropertiesService.removeMembers(groupPath, uhIdentifiers);

        return ootbGroupingPropertiesService.getRemoveMembersResults();
    }

    /**
     * Remove all listed members from a group.
     */
    public AddMembersResults resetGroupMembers(String groupPath) {
        return ootbGroupingPropertiesService.getAddMembersResults();
    }

    /**
     * Add or remove an attribute from a group. This is used to update a groupings
     * preferences.
     */
    public AssignAttributesResults assignAttributesResults(String currentUser, String assignType,
            String assignOperation, String groupPath,
            String attributeName) {
        return ootbGroupingPropertiesService.getAssignAttributesResults();
    }

    /**
     * Change a group attribute's privilege to true or false.
     */
    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath,
            String privilegeName,
            String uhIdentifier, boolean isAllowed) {
        return new AssignGrouperPrivilegesResult();
    }

    /**
     * Get a list of members for each groupPath.
     */
    public GetMembersResults getMembersResults(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        GetMembersResults getMembersResults = ootbGroupingPropertiesService.getMembersResults();
        return getMembersResults;
    }
}
