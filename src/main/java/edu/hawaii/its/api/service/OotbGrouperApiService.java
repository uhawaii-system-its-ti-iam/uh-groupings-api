package edu.hawaii.its.api.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

public class OotbGrouperApiService implements GrouperService {

    public static final Log log = LogFactory.getLog(OotbGrouperApiService.class);

    private final OotbGroupingPropertiesService ootbGroupingPropertiesService;

    public OotbGrouperApiService(OotbGroupingPropertiesService ootbGroupingPropertiesService) {
        this.ootbGroupingPropertiesService = ootbGroupingPropertiesService;
    }

    /**
     * Check if a UH identifier is listed in a group.
     */
    public HasMembersResults hasMemberResults(String groupPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = ootbGroupingPropertiesService.getHasMembersResults();
        return hasMembersResults;
    }

    /**
     * Check if multiple UH identifiers are listed in a group.
     */
    public HasMembersResults hasMembersResults(String groupPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = ootbGroupingPropertiesService.getHasMembersResults();
        return hasMembersResults;
    }

    /**
     * Update a groups description.
     */
    public GroupSaveResults groupSaveResults(String groupingPath, String description) {
        return ootbGroupingPropertiesService.updateDescription(groupingPath, description);
    }

    /**
     * Check if a group exists.
     */
    public FindGroupsResults findGroupsResults(String groupPath) {
        return ootbGroupingPropertiesService.getFindGroups(groupPath);
    }

    public FindGroupsResults findGroupsResults(String currentUser, String groupPath) {
        return ootbGroupingPropertiesService.getFindGroups(groupPath);
    }

    /**
     * Check if multiple groups exist.
     */
    public FindGroupsResults findGroupsResults(List<String> groupPaths) {
        return ootbGroupingPropertiesService.getFindGroups(groupPaths);
    }

    /**
     * Check if a UH identifier is valid.
     */
    public SubjectsResults getSubjects(String uhIdentifier) {
        return ootbGroupingPropertiesService.getSubject(uhIdentifier);
    }

    /**
     * Check if multiple UH identifiers are valid.
     */
    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        return ootbGroupingPropertiesService.getSubjects(uhIdentifiers);
    }

    /**
     * Get all the groups with the specified attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute) {
        return ootbGroupingPropertiesService.getGroupAttributeResultsByAttribute(attribute);
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
        return ootbGroupingPropertiesService.getGroupAttributeResultsByAttributeAndGroupPathList(attribute,
                Collections.singletonList(groupPath));
    }

    /**
     * Check if multiple groups contain an attribute.
     */
    public GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths) {
        return ootbGroupingPropertiesService.getGroupAttributeResultsByAttributeAndGroupPathList(attribute, groupPaths);
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
        return ootbGroupingPropertiesService.getGroupAttributeResults(currentUser, groupPath);
    }

    /**
     * Get all groups that a UH identifier is listed in.
     */
    public GetGroupsResults getGroupsResults(String uhIdentifier) {
        return ootbGroupingPropertiesService.getGroups(uhIdentifier);
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
        return ootbGroupingPropertiesService.addMember(currentUser, groupPath, uhIdentifier);
    }

    /**
     * Add multiple UH identifiers to a group listing.
     */
    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return ootbGroupingPropertiesService.addMembers(currentUser, groupPath, uhIdentifiers);
    }

    /**
     * Remove a UH identifier from a group listing.
     */
    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        return ootbGroupingPropertiesService.removeMember(currentUser, groupPath, uhIdentifier);
    }

    /**
     * Remove multiple UH identifiers from a group listing.
     */
    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        return ootbGroupingPropertiesService.removeMembers(currentUser, groupPath, uhIdentifiers);
    }

    /**
     * Remove all listed members from a group.
     */
    public AddMembersResults resetGroupMembers(String groupPath) {
        return ootbGroupingPropertiesService.resetGroup(groupPath);
    }

    /**
     * Add or remove an attribute from a group. This is used to update a groupings
     * preferences.
     */
    public AssignAttributesResults assignAttributesResults(String currentUser, String assignType,
            String assignOperation, String groupPath,
            String attributeName) {
        return ootbGroupingPropertiesService.manageAttributeAssignment(groupPath, attributeName, assignOperation);
    }

    /**
     * Change a group attribute's privilege to true or false.
     */
    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath,
            String privilegeName,
            String uhIdentifier, boolean isAllowed) {

        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult =
                new WsAssignGrouperPrivilegesLiteResult();
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        wsAssignGrouperPrivilegesLiteResult.setResultMetadata(resultMetadata);

        return new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
    }

    /**
     * Get a list of members for each groupPath.
     */
    public GetMembersResults getMembersResults(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        return ootbGroupingPropertiesService.getOwnedGroupings(groupPaths);
    }
}
