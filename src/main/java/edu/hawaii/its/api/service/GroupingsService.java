package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;
import static edu.hawaii.its.api.service.PathFilter.removeDuplicates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

@Service
public class GroupingsService {

    @Value("${groupings.api.trio}")
    private String TRIO;

    private final GroupPathService groupPathService;

    private final GrouperService grouperService;

    public GroupingsService(GroupPathService groupPathService, GrouperService grouperService) {
        this.groupPathService = groupPathService;
        this.grouperService = grouperService;
    }

    /**
     * A list of grouping paths for all groupings.
     */
    public List<String> groupingPaths() {
        return allGroupingPaths(TRIO);
    }

    public List<GroupingPath> allGroupingPaths() {
        GroupAttributeResults groupAttributeResults = grouperService.groupAttributeResults(TRIO);
        return groupAttributeResults.getGroups().stream()
                .map(group -> new GroupingPath(group.getGroupPath(), group.getDescription())).collect(
                        Collectors.toList());
    }
    public GroupAttributeResults allGroupAttributeResults() {
        GroupAttributeResults groupAttributeResults = grouperService.groupAttributeResults(TRIO);
        return groupAttributeResults;
    }

    /**
     * A list of grouping paths for all opt out enabled Groupings.
     */
    public List<String> optOutEnabledGroupingPaths() {
        return allGroupingPaths(OptType.OUT.value());
    }

    /**
     * A list of all grouping paths from the list of groupingPaths that are opt-out enabled.
     */
    public List<String> optOutEnabledGroupingPaths(List<String> groupingPaths) {
        return allGroupingPaths(OptType.OUT.value(), groupingPaths);
    }

    /**
     * A list of grouping paths for all opt in enabled Groupings.
     */
    public List<String> optInEnabledGroupingPaths() {
        return allGroupingPaths(OptType.IN.value());
    }

    /**
     * A list of all grouping paths from the list of groupingPaths that are opt-in enabled.
     */
    public List<String> optInEnabledGroupingPaths(List<String> groupingPaths) {
        return allGroupingPaths(OptType.IN.value(), groupingPaths);
    }

    /**
     * A list of grouping paths of which uhIdentifier is an owner.
     */
    public List<String> ownedGroupingPaths(String uhIdentifier) {
        return groupingPaths(uhIdentifier, pathHasOwner());
    }

    /**
     * A list of all grouping paths of groups containing the optAttribute.
     */
    public List<String> allGroupingPaths(String optAttribute) {
        GroupAttributeResults groupAttributeResults = grouperService.groupAttributeResults(optAttribute);
        List<String> results = groupAttributeResults.getGroupAttributes().stream().map(GroupAttribute::getGroupPath)
                .collect(Collectors.toList());
        return removeDuplicates(results);
    }

    /**
     * A list of grouping paths from the list of groupPaths that contain the optAttribute.
     */
    private List<String> allGroupingPaths(String optAttribute, List<String> groupingPaths) {
        GroupAttributeResults groupAttributeResults =
                grouperService.groupAttributeResults(optAttribute, groupingPaths);
        List<String> results = groupAttributeResults.getGroupAttributes().stream().map(GroupAttribute::getGroupPath)
                .collect(Collectors.toList());
        return removeDuplicates(results);
    }

    /**
     * A list of grouping paths, filtered by the predicate, that uhIdentifier is a member of.
     */
    public List<String> groupingPaths(String uhIdentifier, Predicate<String> predicate) {
        List<String> groupPaths = groupPaths(uhIdentifier, predicate);
        Set<String> set = new HashSet<>();
        for (String groupPath : groupPaths) {
            set.add(parentGroupingPath(groupPath));
        }
        return new ArrayList<>(set);
    }

    /**
     * A list of group paths, filtered by the predicate, in which the uhIdentifier is listed.
     */
    public List<String> groupPaths(String uhIdentifier, Predicate<String> predicate) {
        return allGroupPaths(uhIdentifier).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Filter a list of groupPaths with respect to the predicate.
     */
    public List<String> filterGroupPaths(List<String> groupPaths, Predicate<String> predicate) {
        return groupPaths.stream().filter(predicate).collect(Collectors.toList());
    }

    public String getGroupingDescription(String path) {
        Group group = grouperService.findGroupsResults(path).getGroup();
        if (!groupPathService.isGroupingPath(group)) {
            return "";
        }
        return group.getDescription();
    }

    /**
     * From a list of group paths return a list of GroupingPath objects. The results are fetched from grouper in order to
     * populate the GroupingPath.description field. The list of groupPaths can contain multiple sub-group paths of a
     * Grouping, the sub-groups are filtered out after grouper returns.
     */
    public List<GroupingPath> getGroupingPaths(List<String> groupPaths) {
        return grouperService.findGroupsResults(groupPaths).getGroups().stream()
                .filter(group -> groupPathService.isGroupingPath(group)).collect(Collectors.toList()).stream()
                .map(group -> new GroupingPath(group.getGroupPath(), group.getDescription()))
                .collect(Collectors.toList());
    }

    public GroupingUpdateDescriptionResult updateGroupingDescription(String path, String description) {
        String updatedDescription = getGroupingDescription(path);
        return new GroupingUpdateDescriptionResult(grouperService.groupSaveResults(path, description),
                updatedDescription);
    }

    /**
     * A list of all group paths, in which the uhIdentifier is listed..
     */
    public List<String> allGroupPaths(String uhIdentifier) {
        List<Group> groups = grouperService.getGroupsResults(uhIdentifier).getGroups();
        return groups.stream().map(Group::getGroupPath).collect(Collectors.toList());
    }
}
