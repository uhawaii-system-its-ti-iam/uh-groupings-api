package edu.hawaii.its.api.service;

import edu.hawaii.its.api.groupings.GroupingsUpdateDescriptionResult;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.hawaii.its.api.service.PathFilter.*;

@Service
public class GroupingsService {

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private GrouperApiService grouperApiService;

    /**
     * A list of grouping paths for all groupings.
     */
    public List<String> groupingPaths() {
        return allGroupingPaths(TRIO);
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
    private List<String> allGroupingPaths(String optAttribute) {
        GroupAttributeResults groupAttributeResults = new GroupAttributeCommand(optAttribute).execute();
        List<String> results = groupAttributeResults.getGroupAttributes().stream().map(GroupAttribute::getGroupPath)
                .collect(Collectors.toList());
        return removeDuplicates(results);
    }

    /**
     * A list of grouping paths from the list of groupPaths that contain the optAttribute.
     */
    private List<String> allGroupingPaths(String optAttribute, List<String> groupingPaths) {
        GroupAttributeResults groupAttributeResults = new GroupAttributeCommand(optAttribute, groupingPaths).execute();
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
        Group group = grouperApiService.findGroupsResults(path).getGroup();
        if (!groupPathService.isGroupingPath(group)) {
            return "";
        }
        return group.getDescription();
    }

    public GroupingsUpdateDescriptionResult updateGroupingDescription(String path, String description) {
        String updatedDescription = getGroupingDescription(path);
        return new GroupingsUpdateDescriptionResult(grouperApiService.groupSaveResults(path, description),
                updatedDescription);
    }

    /**
     * A list of all group paths, in which the uhIdentifier is listed..
     */
    private List<String> allGroupPaths(String uhIdentifier) {
        List<Group> groups = new GetGroupsCommand(uhIdentifier, "").execute().getGroups();
        return groups.stream().map(Group::getGroupPath).collect(Collectors.toList());
    }
}
