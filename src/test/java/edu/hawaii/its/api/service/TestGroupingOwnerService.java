package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingPagedMembers;
import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.type.Grouping;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingOwnerService {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Autowired
    private GroupingOwnerService ownerService;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    @Autowired
    private MemberService memberService;

    private List<String> allMemberGroupPaths() {
        List<String> groupPaths = new ArrayList<>();

        groupPaths.add(GROUPING);
        groupPaths.add(GROUPING_BASIS);
        groupPaths.add(GROUPING_INCLUDE);
        groupPaths.add(GROUPING_EXCLUDE);
        groupPaths.add(GROUPING_OWNERS);

        return groupPaths;
    }

    private void assertPagedMembersResult(GroupingPagedMembers groupingPagedMembers) {
        assertNotNull(groupingPagedMembers);
        assertNotNull(groupingPagedMembers.getMembers());
        assertEquals(Integer.valueOf(1), groupingPagedMembers.getPageNumber());
        assertEquals(Integer.valueOf(groupingPagedMembers.getMembers().size()), groupingPagedMembers.getTotalCount());
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private void assertSorted(List<GroupingMember> members,
                              Function<GroupingMember, String> keyExtractor,
                              boolean isAscending) {
        List<String> values = members.stream()
                .map(member -> safeString(keyExtractor.apply(member)))
                .collect(Collectors.toList());

        for (int i = 1; i < values.size(); i++) {
            int comparison = String.CASE_INSENSITIVE_ORDER.compare(values.get(i - 1), values.get(i));

            if (isAscending) {
                assertTrue(comparison <= 0);
            } else {
                assertTrue(comparison >= 0);
            }
        }
    }

    private Map<String, Object> waitForAllMembersRequest(String requestId) throws InterruptedException {
        Map<String, Object> progress = ownerService.getAllMembersProgress(requestId);
        int attempts = 0;

        while (attempts < 60
                && !Boolean.TRUE.equals(progress.get("complete"))
                && !Boolean.TRUE.equals(progress.get("failed"))) {
            Thread.sleep(500);
            progress = ownerService.getAllMembersProgress(requestId);
            attempts++;
        }

        return progress;
    }

    private String nonAdminNonOwnerUid() {
        String uid = uhIdentifierGenerator.getRandomMember().getUid();
        int attempts = 0;

        while (attempts < 10 && (memberService.isAdmin(uid) || memberService.isOwner(GROUPING, uid))) {
            uid = uhIdentifierGenerator.getRandomMember().getUid();
            attempts++;
        }

        if (memberService.isAdmin(uid) || memberService.isOwner(GROUPING, uid)) {
            fail("Could not find a non-admin and non-owner test user.");
        }

        return uid;
    }

    @Test
    public void getNumberOfGroupingMembersTest() {
        int initialCount = ownerService.numberOfGroupingMembers(ADMIN, GROUPING_INCLUDE);

        grouperService.addMember(ADMIN, GROUPING_INCLUDE, TEST_UH_UUIDS.get(1));
        int countAfterAddition = ownerService.numberOfGroupingMembers(ADMIN, GROUPING_INCLUDE);
        assertEquals(initialCount + 1, countAfterAddition);

        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, TEST_UH_UUIDS.get(1));
        int countAfterRemoval = ownerService.numberOfGroupingMembers(ADMIN, GROUPING_INCLUDE);
        assertEquals(initialCount, countAfterRemoval);

        grouperService.resetGroupMembers(GROUPING_INCLUDE);
        int emptyCount = ownerService.numberOfGroupingMembers(ADMIN, GROUPING_INCLUDE);
        assertEquals(0, emptyCount);
    }

    @Test
    public void paginatedGrouping() {
        GroupingGroupsMembers groupingGroupsMembers = ownerService.paginatedGrouping(
                ADMIN,
                groupPathService.getGroupPaths(GROUPING),
                1,
                700,
                "name",
                true);

        assertNotNull(groupingGroupsMembers);
        assertEquals(SUCCESS, groupingGroupsMembers.getResultCode());
        assertEquals(1, groupingGroupsMembers.getPageNumber());
        assertNotNull(groupingGroupsMembers.getGroupingBasis());
        assertNotNull(groupingGroupsMembers.getGroupingInclude());
        assertNotNull(groupingGroupsMembers.getGroupingExclude());
        assertNotNull(groupingGroupsMembers.getGroupingOwners());
        assertNotNull(groupingGroupsMembers.getAllMembers());
    }

    @Test
    public void getAllMembersSortOptions() {
        List<String> groupPaths = allMemberGroupPaths();

        GroupingPagedMembers nameResult = ownerService.getAllMembers(
                ADMIN,
                groupPaths,
                1,
                700,
                "name",
                true);
        assertPagedMembersResult(nameResult);
        assertSorted(nameResult.getMembers(), GroupingMember::getName, true);

        GroupingPagedMembers uidResult = ownerService.getAllMembers(
                ADMIN,
                groupPaths,
                1,
                700,
                "uid",
                true);
        assertPagedMembersResult(uidResult);
        assertSorted(uidResult.getMembers(), GroupingMember::getUid, true);

        GroupingPagedMembers uhUuidResult = ownerService.getAllMembers(
                ADMIN,
                groupPaths,
                1,
                700,
                "uhUuid",
                true);
        assertPagedMembersResult(uhUuidResult);
        assertSorted(uhUuidResult.getMembers(), GroupingMember::getUhUuid, true);

        GroupingPagedMembers whereListedResult = ownerService.getAllMembers(
                ADMIN,
                groupPaths,
                1,
                700,
                "whereListed",
                true);
        assertPagedMembersResult(whereListedResult);
        assertSorted(whereListedResult.getMembers(), GroupingMember::getWhereListed, true);
    }

    @Test
    public void getAllMembersSortsDescending() {
        GroupingPagedMembers groupingPagedMembers = ownerService.getAllMembers(
                ADMIN,
                allMemberGroupPaths(),
                1,
                700,
                "name",
                false);

        assertPagedMembersResult(groupingPagedMembers);
        assertSorted(groupingPagedMembers.getMembers(), GroupingMember::getName, false);
    }

    @Test
    public void startAllMembersProgressAndResult() throws InterruptedException {
        Map<String, Object> startResponse = ownerService.startAllMembersProgress(
                ADMIN,
                allMemberGroupPaths(),
                700,
                "name",
                true);

        assertNotNull(startResponse);
        assertNotNull(startResponse.get("requestId"));
        assertEquals(0, startResponse.get("loadedCount"));
        assertEquals(false, startResponse.get("complete"));
        assertEquals(false, startResponse.get("failed"));
        assertEquals("", startResponse.get("message"));

        String requestId = startResponse.get("requestId").toString();

        Map<String, Object> progress = waitForAllMembersRequest(requestId);

        assertNotNull(progress);
        assertEquals(requestId, progress.get("requestId"));
        assertFalse(Boolean.TRUE.equals(progress.get("failed")), String.valueOf(progress.get("message")));
        assertTrue(Boolean.TRUE.equals(progress.get("complete")));

        GroupingPagedMembers result = ownerService.getAllMembersResult(requestId);

        assertPagedMembersResult(result);
        assertEquals(result.getTotalCount(), progress.get("loadedCount"));
    }

    @Test
    public void getAllMembersProgressRequestNotFound() {
        String requestId = "missing-request-id";

        Map<String, Object> progress = ownerService.getAllMembersProgress(requestId);

        assertNotNull(progress);
        assertEquals(requestId, progress.get("requestId"));
        assertEquals(0, progress.get("loadedCount"));
        assertEquals(false, progress.get("complete"));
        assertEquals(true, progress.get("failed"));
        assertEquals("Request not found.", progress.get("message"));
    }

    @Test
    public void getAllMembersResultRequestNotFound() {
        assertNull(ownerService.getAllMembersResult("missing-request-id"));
    }

    @Test
    public void getGroupingMembers() {
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, TEST_UH_UUIDS.get(1));

        GroupingGroupMembers groupingGroupMembers = ownerService.getGroupingMembers(
                ADMIN,
                GROUPING,
                1,
                20,
                "name",
                true);

        assertNotNull(groupingGroupMembers);
        assertEquals(SUCCESS, groupingGroupMembers.getResultCode());
        assertEquals(GROUPING, groupingGroupMembers.getGroupPath());
        assertNotNull(groupingGroupMembers.getMembers());

        groupingGroupMembers = ownerService.getGroupingMembers(
                ADMIN,
                GROUPING,
                1,
                20,
                "name",
                true,
                null);

        assertNotNull(groupingGroupMembers);
        assertEquals(SUCCESS, groupingGroupMembers.getResultCode());
        assertEquals(GROUPING, groupingGroupMembers.getGroupPath());
        assertNotNull(groupingGroupMembers.getMembers());

        groupingGroupMembers = ownerService.getGroupingMembers(
                ADMIN,
                GROUPING,
                1,
                20,
                "name",
                true,
                "test");

        assertNotNull(groupingGroupMembers);
        assertEquals(SUCCESS, groupingGroupMembers.getResultCode());
        assertEquals(GROUPING, groupingGroupMembers.getGroupPath());
        assertNotNull(groupingGroupMembers.getMembers());

        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, TEST_UH_UUIDS.get(1));
    }

    @Test
    public void getGroupingMembersThrowsAccessDenied() {
        String uid = nonAdminNonOwnerUid();

        assertThrows(AccessDeniedException.class, () -> ownerService.getGroupingMembers(
                uid,
                GROUPING,
                1,
                20,
                "name",
                true,
                "test"));
    }

    @Test
    public void getGroupingMembersWhereListed() {
        List<String> uids = uhIdentifierGenerator.getRandomMembers(5).getUids();
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, uids);

        GroupingMembers groupingMembers = ownerService.getGroupingMembersWhereListed(ADMIN, GROUPING, uids);

        assertNotNull(groupingMembers);
        assertFalse(groupingMembers.getMembers().isEmpty());
        assertFalse(groupingMembers.getUids().isEmpty());
        assertFalse(groupingMembers.getUhUuids().isEmpty());
        assertTrue(groupingMembers.getMembers().stream().allMatch(groupingMember ->
                Set.of("Basis & Include", "Basis", "Include").contains(groupingMember.getWhereListed())));

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, uids);
    }

    @Test
    public void getGroupingMembersIsBasis() {
        List<String> uids = uhIdentifierGenerator.getRandomMembers(5).getUids();
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, uids);

        GroupingMembers groupingMembers = ownerService.getGroupingMembersIsBasis(ADMIN, GROUPING, uids);

        assertNotNull(groupingMembers);
        assertFalse(groupingMembers.getMembers().isEmpty());
        assertFalse(groupingMembers.getUids().isEmpty());
        assertFalse(groupingMembers.getUhUuids().isEmpty());
        assertTrue(groupingMembers.getMembers().stream().allMatch(groupingMember ->
                Set.of("Basis", "").contains(groupingMember.getWhereListed())));

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, uids);
    }

    @Test
    public void getMembersExistInInclude() {
        List<String> uids = uhIdentifierGenerator.getRandomMembers(2).getUids();

        try {
            updateMemberService.addIncludeMembers(ADMIN, GROUPING, uids);

            GroupingMembers groupingMembers = ownerService.getMembersExistInInclude(ADMIN, GROUPING, uids);

            assertNotNull(groupingMembers);
            assertFalse(groupingMembers.getMembers().isEmpty());
            assertTrue(groupingMembers.getMembers().stream()
                    .anyMatch(member -> uids.contains(member.getUid())));
        } finally {
            updateMemberService.removeIncludeMembers(ADMIN, GROUPING, uids);
        }
    }

    @Test
    public void getMembersExistInExclude() {
        List<String> uids = uhIdentifierGenerator.getRandomMembers(2).getUids();

        try {
            updateMemberService.addExcludeMembers(ADMIN, GROUPING, uids);

            GroupingMembers groupingMembers = ownerService.getMembersExistInExclude(ADMIN, GROUPING, uids);

            assertNotNull(groupingMembers);
            assertFalse(groupingMembers.getMembers().isEmpty());
            assertTrue(groupingMembers.getMembers().stream()
                    .anyMatch(member -> uids.contains(member.getUid())));
        } finally {
            updateMemberService.removeExcludeMembers(ADMIN, GROUPING, uids);
        }
    }

    @Test
    public void getMembersExistInOwners() {
        List<String> uids = List.of(uhIdentifierGenerator.getRandomMember().getUid());

        try {
            updateMemberService.addOwnerships(ADMIN, GROUPING, uids);

            GroupingMembers groupingMembers = ownerService.getMembersExistInOwners(ADMIN, GROUPING, uids);

            assertNotNull(groupingMembers);
            assertFalse(groupingMembers.getMembers().isEmpty());
            assertTrue(groupingMembers.getMembers().stream()
                    .anyMatch(member -> uids.contains(member.getUid())));
        } finally {
            updateMemberService.removeOwnerships(ADMIN, GROUPING, uids);
        }
    }

    @Test
    public void groupingOptAttributes() {
        GroupingOptAttributes groupingOptAttributes = ownerService.groupingOptAttributes(
                ADMIN,
                GROUPING);

        assertNotNull(groupingOptAttributes);
        assertEquals(SUCCESS, groupingOptAttributes.getResultCode());
        assertEquals(GROUPING, groupingOptAttributes.getGroupPath());
    }

    @Test
    public void groupingsDescription() {
        GroupingDescription groupingDescription = ownerService.groupingsDescription(
                ADMIN,
                GROUPING);

        assertNotNull(groupingDescription);
        assertEquals(SUCCESS, groupingDescription.getResultCode());
        assertEquals(GROUPING, groupingDescription.getGroupPath());
        assertNotNull(groupingDescription.getDescription());
    }

    @Test
    public void groupingsSyncDestinations() {
        GroupingSyncDestinations groupingSyncDestinations = ownerService.groupingsSyncDestinations(
                ADMIN,
                GROUPING);

        assertNotNull(groupingSyncDestinations);
        assertEquals(SUCCESS, groupingSyncDestinations.getResultCode());
        assertNotNull(groupingSyncDestinations.getSyncDestinations());

        assertEquals(groupingSyncDestinations.getSyncDestinations(),
                groupingSyncDestinations.getSyncDestinations().stream()
                        .sorted(Comparator.comparing(GroupingSyncDestination::getDescription))
                        .collect(Collectors.toList()));

        assertTrue(groupingSyncDestinations.getSyncDestinations().stream()
                .filter(syncDestination -> !syncDestination.getDescription().contains("uhReleasedGrouping"))
                .allMatch(e -> e.getDescription().contains(new Grouping(GROUPING).getName())));
    }

}