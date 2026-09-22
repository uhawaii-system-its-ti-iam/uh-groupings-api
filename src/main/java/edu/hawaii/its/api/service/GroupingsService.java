package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;
import static edu.hawaii.its.api.service.PathFilter.removeDuplicates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.hawaii.its.api.exception.GrouperException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

@Service
public class GroupingsService {

    private static final Log logger = LogFactory.getLog(GroupingsService.class);

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.curated}")
    private String CURATED;

    @Value("${groupings.catalog.preload:true}")
    private boolean groupingCatalogPreload;

    private final GroupPathService groupPathService;

    private final GrouperService grouperService;
    private volatile CatalogSnapshot groupingCatalogSnapshot;

    @Value("${groupings.catalog.ttl-ms:60000}")
    private long groupingCatalogTtlMillis;

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

    public List<String> curatedGroupings() {
        return allGroupingPaths(CURATED);
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

    public GroupingPaths paginatedAdminGroupingPaths(int page, int pageSize, String search) {
        if (page < 1)
            throw new IllegalArgumentException("page must be greater than zero");
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<GroupingPath> filtered = groupingCatalog().stream()
                .filter(path -> term.isEmpty() || contains(path.getPath(), term) || contains(path.getDescription(),
                        term))
                .collect(Collectors.toList());
        int offset = Math.min((page - 1) * pageSize, filtered.size());
        int end = Math.min(offset + pageSize, filtered.size());
        return new GroupingPaths(new ArrayList<>(filtered.subList(offset, end)), page, pageSize, filtered.size());
    }

    public synchronized void invalidateGroupingCatalog() {
        groupingCatalogSnapshot = null;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmGroupingCatalog() {
        if (!groupingCatalogPreload)
            return;
        refreshGroupingCatalog("startup");
    }

    @Scheduled(
            fixedDelayString = "${groupings.catalog.refresh-ms:60000}",
            initialDelayString = "${groupings.catalog.refresh-initial-delay-ms:60000}")
    public void refreshGroupingCatalog() {
        if (!groupingCatalogPreload)
            return;
        refreshGroupingCatalog("scheduled");
    }

    private void refreshGroupingCatalog(String source) {
        long startedAt = System.currentTimeMillis();
        try {
            List<GroupingPath> paths = loadGroupingCatalog();
            CatalogSnapshot refreshed = new CatalogSnapshot(paths, System.currentTimeMillis());
            synchronized (this) {
                groupingCatalogSnapshot = refreshed;
            }
            logger.info(String.format(
                    "Grouping catalog %s refresh completed in %dms; entries=%d",
                    source,
                    System.currentTimeMillis() - startedAt,
                    paths.size()));
        } catch (RuntimeException exception) {
            logger.error("Grouping catalog " + source + " refresh failed; retaining existing snapshot", exception);
        }
    }

    private List<GroupingPath> groupingCatalog() {
        CatalogSnapshot snapshot = groupingCatalogSnapshot;
        long now = System.currentTimeMillis();
        if (snapshot != null && now - snapshot.createdAt < groupingCatalogTtlMillis) {
            return snapshot.paths;
        }

        synchronized (this) {
            snapshot = groupingCatalogSnapshot;
            long refreshedNow = System.currentTimeMillis();
            if (snapshot == null || refreshedNow - snapshot.createdAt >= groupingCatalogTtlMillis) {
                List<GroupingPath> paths = loadGroupingCatalog();
                snapshot = new CatalogSnapshot(paths, refreshedNow);
                groupingCatalogSnapshot = snapshot;
            }
            return snapshot.paths;
        }
    }

    private List<GroupingPath> loadGroupingCatalog() {
        return grouperService.groupAttributeResults(TRIO).getGroups().stream()
                .map(group -> new GroupingPath(group.getGroupPath(), group.getDescription()))
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private static final class CatalogSnapshot {
        private final List<GroupingPath> paths;
        private final long createdAt;

        private CatalogSnapshot(List<GroupingPath> paths, long createdAt) {
            this.paths = paths;
            this.createdAt = createdAt;
        }
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
        GroupingUpdateDescriptionResult result = new GroupingUpdateDescriptionResult(
                grouperService.groupSaveResults(path, description), updatedDescription);
        if (result.getResultCode().startsWith("SUCCESS")) {
            invalidateGroupingCatalog();
        }
        return result;
    }

    /**
     * A list of all group paths, in which the uhIdentifier is listed..
     */
    public List<String> allGroupPaths(String uhIdentifier) {
        List<Group> groups = grouperService.getGroupsResults(uhIdentifier).getGroups();
        return groups.stream().map(Group::getGroupPath).collect(Collectors.toList());
    }

    /**
     * From a list of grouping paths, the subset that is used as an owner-grouping of some grouping. An owner-grouping
     * is listed as a group member of another grouping's owners group, so a grouping is an owner-grouping when it is
     * listed in a path ending in :owners.
     * <p>
     * If a grouping is a member of any group whose path ends in :owners, then that grouping is an owner-grouping.
     */
    public Set<String> ownerGroupingPaths(List<String> groupingPaths) {
        if (groupingPaths.isEmpty()) {
            return Collections.emptySet();
        }
        Map<String, List<Group>> groupsByGroupingPath =
                grouperService.getGroupsOfGroups(groupingPaths).getGroupsBySubjectName();
        return groupsByGroupingPath.entrySet().stream()
                .filter(entry -> entry.getValue().stream().map(Group::getGroupPath).anyMatch(pathHasOwner()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
