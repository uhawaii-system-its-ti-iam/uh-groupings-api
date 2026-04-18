package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
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
import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.groupings.GroupingPagedMembers;
import edu.hawaii.its.api.groupings.GroupingGroupMember;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.Strings;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
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

    private static final int ALL_MEMBERS_THREAD_POOL_SIZE = 2;

    private static final long ALL_MEMBERS_REQUEST_TTL_MILLIS = TimeUnit.MINUTES.toMillis(30);

    private static final long ALL_MEMBERS_SHUTDOWN_TIMEOUT_SECONDS = 30L;

    private final ExecutorService allMembersExecutor = Executors.newFixedThreadPool(ALL_MEMBERS_THREAD_POOL_SIZE);

    private final ConcurrentMap<String, Long> allMembersCreatedAtMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Integer> allMembersLoadedCountMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Boolean> allMembersCompleteMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Boolean> allMembersFailedMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, String> allMembersMessageMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, GroupingPagedMembers> allMembersResultMap = new ConcurrentHashMap<>();

    public GroupingOwnerService(GrouperService grouperService, MemberService memberService) {
        this.grouperService = grouperService;
        this.memberService = memberService;
    }

    /**
     * Get the number of grouping members: Basis + Include - Exclude.
     */
    public Integer numberOfGroupingMembers(String currentUser, String groupingPath) {
        log.debug(String.format("numberOfGroupingMembers; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        GetMembersResult getMembersResult = grouperService.getMembersResult(currentUser, groupingPath);
        return getMembersResult.getSubjects().size();
    }

    /**
     * Get one paginated page of members from the requested grouping paths.
     * This method returns the raw members for each requested group path, such as composite,
     * basis, include, exclude, and owners. It does not compute or populate allMembers.
     * Use getAllMembers() or the All Members progress/result flow when the final computed
     * All Members list is needed.
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

        return groupingGroupsMembers;
    }

    public GroupingPagedMembers getAllMembers(String currentUser,
                                              List<String> groupPaths,
                                              Integer pageNumber,
                                              Integer pageSize,
                                              String sortString,
                                              Boolean isAscending) {
        return buildAllMembersResult(currentUser, groupPaths, pageSize, sortString, isAscending, null);
    }

    public Map<String, Object> startAllMembersProgress(String currentUser,
                                                       List<String> groupPaths,
                                                       Integer pageSize,
                                                       String sortString,
                                                       Boolean isAscending) {

        cleanupExpiredAllMembersRequests();

        String requestId = UUID.randomUUID().toString();

        allMembersCreatedAtMap.put(requestId, System.currentTimeMillis());
        allMembersLoadedCountMap.put(requestId, 0);
        allMembersCompleteMap.put(requestId, false);
        allMembersFailedMap.put(requestId, false);
        allMembersMessageMap.put(requestId, "");
        allMembersResultMap.remove(requestId);

        allMembersExecutor.submit(() -> {
            try {
                GroupingPagedMembers result = buildAllMembersResult(
                        currentUser,
                        groupPaths,
                        pageSize,
                        sortString,
                        isAscending,
                        requestId
                );

                allMembersCreatedAtMap.put(requestId, System.currentTimeMillis());
                allMembersResultMap.put(requestId, result);
                allMembersLoadedCountMap.put(requestId, result.getTotalCount());
                allMembersCompleteMap.put(requestId, true);
                allMembersFailedMap.put(requestId, false);
                allMembersMessageMap.put(requestId, "");
            } catch (Exception e) {
                allMembersCreatedAtMap.put(requestId, System.currentTimeMillis());
                allMembersFailedMap.put(requestId, true);
                allMembersCompleteMap.put(requestId, false);
                allMembersMessageMap.put(requestId, e.getMessage() == null ? "Unable to load all members." : e.getMessage());
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("loadedCount", 0);
        response.put("complete", false);
        response.put("failed", false);
        response.put("message", "");
        return response;
    }

    public Map<String, Object> getAllMembersProgress(String requestId) {
        cleanupExpiredAllMembersRequests();
        Map<String, Object> response = new HashMap<>();

        if (!allMembersLoadedCountMap.containsKey(requestId)) {
            response.put("requestId", requestId);
            response.put("loadedCount", 0);
            response.put("complete", false);
            response.put("failed", true);
            response.put("message", "Request not found.");
            return response;
        }

        response.put("requestId", requestId);
        response.put("loadedCount", allMembersLoadedCountMap.getOrDefault(requestId, 0));
        response.put("complete", allMembersCompleteMap.getOrDefault(requestId, false));
        response.put("failed", allMembersFailedMap.getOrDefault(requestId, false));
        response.put("message", allMembersMessageMap.getOrDefault(requestId, ""));
        return response;
    }

    private void cleanupExpiredAllMembersRequests() {
        long cutoffTime = System.currentTimeMillis() - ALL_MEMBERS_REQUEST_TTL_MILLIS;

        List<String> expiredRequestIds = allMembersCreatedAtMap.entrySet().stream()
                .filter(entry -> entry.getValue() < cutoffTime)
                .filter(entry -> isAllMembersRequestFinished(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        expiredRequestIds.forEach(this::removeAllMembersRequest);
    }

    private boolean isAllMembersRequestFinished(String requestId) {
        return Boolean.TRUE.equals(allMembersCompleteMap.get(requestId))
                || Boolean.TRUE.equals(allMembersFailedMap.get(requestId));
    }

    private void removeAllMembersRequest(String requestId) {
        allMembersCreatedAtMap.remove(requestId);
        allMembersLoadedCountMap.remove(requestId);
        allMembersCompleteMap.remove(requestId);
        allMembersFailedMap.remove(requestId);
        allMembersMessageMap.remove(requestId);
        allMembersResultMap.remove(requestId);
    }

    @PreDestroy
    public void shutdownAllMembersExecutor() {
        allMembersExecutor.shutdown();

        try {
            if (!allMembersExecutor.awaitTermination(ALL_MEMBERS_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                allMembersExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            allMembersExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public GroupingPagedMembers getAllMembersResult(String requestId) {
        cleanupExpiredAllMembersRequests();
        return allMembersResultMap.get(requestId);
    }

    private GroupingPagedMembers buildAllMembersResult(String currentUser,
                                                       List<String> groupPaths,
                                                       Integer pageSize,
                                                       String sortString,
                                                       Boolean isAscending,
                                                       String requestId) {

        log.debug(String.format(
                "buildAllMembersResult; currentUser: %s; groupPaths: %s; pageSize: %d; sortString: %s; isAscending: %b;",
                currentUser, groupPaths, pageSize, sortString, isAscending));

        Map<String, List<GroupingGroupMember>> groupedMembersMap = new LinkedHashMap<>();
        Set<String> compositeMemberKeys = new LinkedHashSet<>();

        fetchAllMembersPages(
                currentUser,
                groupPaths,
                pageSize,
                groupedMembersMap,
                compositeMemberKeys,
                requestId
        );

        List<GroupingMember> finalMembers = finalizeAllMembers(groupedMembersMap);
        sortAllMembers(finalMembers, sortString, isAscending);

        return createGroupingPagedMembers(finalMembers);
    }

    private void fetchAllMembersPages(String currentUser,
                                      List<String> groupPaths,
                                      Integer pageSize,
                                      Map<String, List<GroupingGroupMember>> groupedMembersMap,
                                      Set<String> compositeMemberKeys,
                                      String requestId) {
        int fetchPageNumber = 1;
        boolean hasMoreMembers = true;

        while (hasMoreMembers) {
            GroupingGroupsMembers pageResult = getAllMembersPage(currentUser, groupPaths, fetchPageNumber, pageSize);

            addPageMembers(groupedMembersMap, pageResult);
            addCompositeMemberKeys(compositeMemberKeys, pageResult);
            updateAllMembersLoadedCount(requestId, compositeMemberKeys.size());

            if (!hasAnyMembers(pageResult)) {
                hasMoreMembers = false;
            } else {
                fetchPageNumber++;
            }
        }
    }

    private GroupingGroupsMembers getAllMembersPage(String currentUser,
                                                    List<String> groupPaths,
                                                    Integer fetchPageNumber,
                                                    Integer pageSize) {
        GetMembersResults getMembersResults = grouperService.getMembersResults(
                currentUser,
                groupPaths,
                fetchPageNumber,
                pageSize,
                "name",
                true
        );

        return new GroupingGroupsMembers(getMembersResults);
    }

    private boolean hasAnyMembers(GroupingGroupsMembers pageResult) {
        return pageResult.getGroupsMembersList().stream()
                .anyMatch(this::hasMembers);
    }

    private boolean hasMembers(GroupingGroupMembers group) {
        return group != null
                && group.getMembers() != null
                && !group.getMembers().isEmpty();
    }

    private void addPageMembers(Map<String, List<GroupingGroupMember>> groupedMembersMap,
                                GroupingGroupsMembers pageResult) {
        for (GroupingGroupMembers group : pageResult.getGroupsMembersList()) {
            addGroupMembers(groupedMembersMap, group);
        }
    }

    private void addGroupMembers(Map<String, List<GroupingGroupMember>> groupedMembersMap,
                                 GroupingGroupMembers group) {
        if (group == null || group.getGroupPath() == null || group.getGroupPath().isEmpty()) {
            return;
        }

        List<GroupingGroupMember> members = groupedMembersMap.computeIfAbsent(
                group.getGroupPath(),
                k -> new ArrayList<>());

        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            return;
        }

        members.addAll(group.getMembers());
    }

    private void addCompositeMemberKeys(Set<String> compositeMemberKeys,
                                        GroupingGroupsMembers pageResult) {
        GroupingGroupMembers compositeGroup = pageResult.getCompositeGrouping();

        if (compositeGroup == null || compositeGroup.getMembers() == null) {
            return;
        }

        for (GroupingGroupMember member : compositeGroup.getMembers()) {
            addCompositeMemberKey(compositeMemberKeys, member);
        }
    }

    private void addCompositeMemberKey(Set<String> compositeMemberKeys,
                                       GroupingGroupMember member) {
        if (member == null) {
            return;
        }

        if (member.getUhUuid() != null && !member.getUhUuid().isEmpty()) {
            compositeMemberKeys.add(member.getUhUuid());
            return;
        }

        if (member.getUid() != null && !member.getUid().isEmpty()) {
            compositeMemberKeys.add(member.getUid());
        }
    }

    private List<GroupingMember> finalizeAllMembers(Map<String, List<GroupingGroupMember>> groupedMembersMap) {
        GroupingGroupsMembers finalResult = new GroupingGroupsMembers();

        finalResult.getGroupsMembersList().addAll(createMergedGroups(groupedMembersMap));
        finalResult.finalizeMembers();

        return new ArrayList<>(finalResult.getAllMembers().getMembers());
    }

    private List<GroupingGroupMembers> createMergedGroups(Map<String, List<GroupingGroupMember>> groupedMembersMap) {
        List<GroupingGroupMembers> mergedGroups = new ArrayList<>();

        for (Map.Entry<String, List<GroupingGroupMember>> entry : groupedMembersMap.entrySet()) {
            GroupingGroupMembers mergedGroup = new GroupingGroupMembers();
            mergedGroup.setGroupPath(entry.getKey());
            mergedGroup.setGroupingGroupMembers(entry.getValue());
            mergedGroups.add(mergedGroup);
        }

        return mergedGroups;
    }

    private void sortAllMembers(List<GroupingMember> finalMembers,
                                String sortString,
                                Boolean isAscending) {
        Comparator<GroupingMember> comparator = allMembersComparator(sortString);

        if (!isAscending) {
            comparator = comparator.reversed();
        }

        finalMembers.sort(comparator);
    }

    private Comparator<GroupingMember> allMembersComparator(String sortString) {
        switch (sortString) {
            case "uhUuid":
                return Comparator.comparing(
                        m -> m.getUhUuid() == null ? "" : m.getUhUuid(),
                        String.CASE_INSENSITIVE_ORDER
                );
            case "uid":
                return Comparator.comparing(
                        m -> m.getUid() == null ? "" : m.getUid(),
                        String.CASE_INSENSITIVE_ORDER
                );
            case "whereListed":
                return Comparator.comparing(
                        m -> m.getWhereListed() == null ? "" : m.getWhereListed(),
                        String.CASE_INSENSITIVE_ORDER
                );
            case "name":
            default:
                return Comparator.comparing(
                        m -> m.getName() == null ? "" : m.getName(),
                        String.CASE_INSENSITIVE_ORDER
                );
        }
    }

    private GroupingPagedMembers createGroupingPagedMembers(List<GroupingMember> finalMembers) {
        GroupingPagedMembers result = new GroupingPagedMembers();

        result.setMembers(new ArrayList<>(finalMembers));
        result.setPageNumber(1);
        result.setTotalCount(finalMembers.size());

        return result;
    }

    private void updateAllMembersLoadedCount(String requestId, int loadedCount) {
        if (requestId == null) {
            return;
        }
        allMembersLoadedCountMap.put(requestId, loadedCount);
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

    public GroupingMembers getMembersExistInInclude(String currentUser, String groupingPath,
                                                    List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.INCLUDE.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);

    }

    public GroupingMembers getMembersExistInExclude(String currentUser, String groupingPath,
                                                    List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.EXCLUDE.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);
    }

    public GroupingMembers getMembersExistInOwners(String currentUser, String groupingPath,
                                                   List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.OWNERS.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);
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