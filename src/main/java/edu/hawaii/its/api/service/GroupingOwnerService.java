package edu.hawaii.its.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

/**
 * GroupingOwnerService contains all the necessary functions to hydrate a selected grouping.
 */
@Service("ownerService")
public class GroupingOwnerService {
    public static final Log log = LogFactory.getLog(GroupingOwnerService.class);
    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    @Autowired private GroupingPropertiesService groupingPropertiesService;

    /**
     * Get all members listed in the groups in groupsPath. This should be used iteratively from the UI to get all
     * members of a grouping.
     */
    public GroupingGroupsMembers paginatedGrouping(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        log.debug(String.format(
                "paginatedGrouping; currentUser: %s; groupPaths: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b;",
                currentUser, groupPaths, pageNumber, pageSize, sortString, isAscending));
        GetMembersResults getMembersResults = groupingPropertiesService.getGrouperService().getMembersResults(
                currentUser,
                groupPaths,
                pageNumber,
                pageSize,
                sortString,
                isAscending);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        groupingGroupsMembers.setPageNumber(pageNumber);
        return groupingGroupsMembers;
    }

    /**
     * Get the opt attributes of a selected grouping.
     */
    public GroupingOptAttributes groupingOptAttributes(String currentUser, String groupingPath) {
        log.debug(String.format("groupingOptAttributes; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOptAttributes(groupingPropertiesService.getGrouperService().groupAttributeResult(currentUser, groupingPath));
    }

    /**
     * Get the description of a selected grouping.
     */
    public GroupingDescription groupingsDescription(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsDescription; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingDescription(groupingPropertiesService.getGrouperService().findGroupsResults(currentUser, groupingPath).getGroup());
    }

    /**
     * Get a list of sync-destinations for a selected grouping.
     */
    public GroupingSyncDestinations groupingsSyncDestinations(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsSyncDestinations; currentUser: %s; groupingPath: %s;", currentUser,
                groupingPath));
        FindAttributesResults findAttributesResults = groupingPropertiesService.getGrouperService().findAttributesResults(
                currentUser,
                SYNC_DESTINATIONS_CHECKBOXES,
                SYNC_DESTINATIONS_LOCATION);
        GroupAttributeResults groupAttributeResults = groupingPropertiesService.getGrouperService().groupAttributeResults(
                currentUser,
                findAttributesResults.getResults().stream().map(AttributesResult::getName).collect(Collectors.toList()),
                groupingPath);
        return new GroupingSyncDestinations(findAttributesResults, groupAttributeResults);
    }
}