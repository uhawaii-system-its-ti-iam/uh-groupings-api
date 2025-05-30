package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.Strings;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * GroupingOwnerService contains all the necessary functions to hydrate a selected grouping.
 */
@Service("ownerService")
public class GroupingOwnerService {

    private static final Log log = LogFactory.getLog(GroupingOwnerService.class);

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    private final GrouperService grouperService;

    private final MemberService memberService;

    public GroupingOwnerService(GrouperService grouperService, MemberService memberService) {
        this.grouperService = grouperService;
        this.memberService = memberService;
    }

    /**
     * Get the number of grouping members
     */
    public Integer numberOfGroupingMembers(String currentUser, String groupingPath) {
        log.debug(String.format("numberOfGroupingMembers; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        GetMembersResult getMembersResult = grouperService.getMembersResult(currentUser, groupingPath);
        return getMembersResult.getSubjects().size();
    }

    /**
     * Get all members listed in the groups in groupsPath. This should be used iteratively from the UI to get all
     * members of a grouping.
     */
    public GroupingGroupsMembers paginatedGrouping(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        log.debug(String.format(
                "paginatedGrouping; currentUser: %s; groupPaths: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b;",
                currentUser, groupPaths, pageNumber, pageSize, sortString, isAscending));
        GetMembersResults getMembersResults = grouperService.getMembersResults(
                currentUser,
                groupPaths,
                pageNumber,
                pageSize,
                sortString,
                isAscending);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        groupingGroupsMembers.setPageNumber(pageNumber);
        if (grouperService instanceof OotbGrouperApiService && pageNumber > 1) {
            groupingGroupsMembers.setPaginationCompleteTrue();
        }
        return groupingGroupsMembers;
    }

    public GroupingGroupMembers getGroupingMembers(String currentUser, String groupingPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        log.debug(String.format(
                "getGroupingMembers; currentUser: %s; groupingPath: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b;",
                currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending));
        GetMembersResult getMembersResult = grouperService.getMembersResult(
                currentUser,
                groupingPath,
                pageNumber,
                pageSize,
                sortString,
                isAscending);
        return new GroupingGroupMembers(getMembersResult);
    }

    public GroupingGroupMembers getGroupingMembers(String currentUser, String groupingPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending, String searchString) {
        log.debug(String.format(
            "getGroupingMembers; currentUser: %s; groupingPath: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b; searchString: %s;",
            currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending, searchString));

        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(groupingPath, currentUser)) {
            throw new AccessDeniedException();
        }

        if (Strings.isEmpty(searchString)) {
            return getGroupingMembers(currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending);
        }

        SubjectsResults subjectsResults = grouperService.getSubjects(groupingPath, searchString);

        return new GroupingGroupMembers(subjectsResults).sort(sortString, isAscending).paginate(pageNumber, pageSize);
    }

    public GroupingMembers getGroupingMembersWhereListed(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResultsBasis = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.BASIS.value(), uhIdentifiers);
        HasMembersResults hasMembersResultsInclude = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.INCLUDE.value(), uhIdentifiers);

        return new GroupingMembers(hasMembersResultsBasis, hasMembersResultsInclude);
    }

    public GroupingMembers getGroupingMembersIsBasis(String currentUser, String groupingPath, List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.BASIS.value(), uhIdentifiers);
        return new GroupingMembers(hasMembersResults);
    }

    /**
     * Get the opt attributes of a selected grouping.
     */
    public GroupingOptAttributes groupingOptAttributes(String currentUser, String groupingPath) {
        log.debug(
                String.format("groupingOptAttributes; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOptAttributes(grouperService.groupAttributeResult(currentUser, groupingPath));
    }

    /**
     * Get the description of a selected grouping.
     */
    public GroupingDescription groupingsDescription(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsDescription; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingDescription(grouperService.findGroupsResults(currentUser, groupingPath).getGroup());
    }

    /**
     * Get a list of sync-destinations for a selected grouping.
     */
    public GroupingSyncDestinations groupingsSyncDestinations(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsSyncDestinations; currentUser: %s; groupingPath: %s;", currentUser,
                groupingPath));
        FindAttributesResults findAttributesResults = grouperService.findAttributesResults(
                currentUser,
                SYNC_DESTINATIONS_CHECKBOXES,
                SYNC_DESTINATIONS_LOCATION);

        GroupAttributeResults groupAttributeResults = grouperService.groupAttributeResults(
                currentUser,
                findAttributesResults.getResults().stream().map(AttributesResult::getName).collect(Collectors.toList()),
                groupingPath);

        List<GroupingSyncDestination> syncDestinationList =
                createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults);

        return new GroupingSyncDestinations(findAttributesResults, groupAttributeResults, syncDestinationList);
    }

    /**
     * Create a list of groupingSyncDestination with findAttributesResults and groupAttributeResults
     */
    public List<GroupingSyncDestination> createGroupingSyncDestinationList(FindAttributesResults findAttributesResults,
            GroupAttributeResults groupAttributeResults) {
        List<AttributesResult> attributesResults = findAttributesResults.getResults();
        List<GroupingSyncDestination> syncDestinationList = new ArrayList<>();
        for (AttributesResult attributesResult : attributesResults) {
            GroupingSyncDestination groupingSyncDestination =
                    JsonUtil.asObject(attributesResult.getDescription(), GroupingSyncDestination.class);
            groupingSyncDestination.setName(attributesResult.getName());
            groupingSyncDestination.setDescription(groupingSyncDestination.getDescription()
                    .replaceFirst("\\$\\{srhfgs}", groupAttributeResults.getGroups().stream()
                            .findFirst()
                            .map(Group::getExtension)
                            .orElse("")));
            groupingSyncDestination.setSynced(groupAttributeResults.getGroupAttributes().stream()
                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
            syncDestinationList.add(groupingSyncDestination);
        }
        syncDestinationList.sort(Comparator.comparing(GroupingSyncDestination::getDescription));
        return syncDestinationList;
    }
}