package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
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

    @Autowired
    private GroupingOwnerService ownerService;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

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
    public void getGroupingMembers() {
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
    public void groupingMembersBySearch() {
        String uhIdentifier = uhIdentifierGenerator.getRandomMember().getUhUuid();

        updateMemberService.removeOwnership(ADMIN, GROUPING, uhIdentifier);
        assertThrows(AccessDeniedException.class,
                () -> ownerService.groupingMembersBySearchString(uhIdentifier, GROUPING, uhIdentifier));

        updateMemberService.addIncludeMember(ADMIN, GROUPING, uhIdentifier);
        updateMemberService.addOwnership(ADMIN, GROUPING, uhIdentifier);

        GroupingGroupMembers groupingGroupMembers = ownerService.groupingMembersBySearchString(uhIdentifier, GROUPING, uhIdentifier);
        assertNotNull(groupingGroupMembers);
        assertNotNull(groupingGroupMembers.getMembers());
        assertFalse(groupingGroupMembers.getMembers().isEmpty());
        assertNotNull(groupingGroupMembers.getGroupPath());

        groupingGroupMembers = ownerService.groupingMembersBySearchString(ADMIN, GROUPING, uhIdentifier);
        assertNotNull(groupingGroupMembers);
        assertNotNull(groupingGroupMembers.getMembers());
        assertFalse(groupingGroupMembers.getMembers().isEmpty());
        assertNotNull(groupingGroupMembers.getGroupPath());

        updateMemberService.removeIncludeMember(ADMIN, GROUPING, uhIdentifier);
        updateMemberService.removeOwnership(ADMIN, GROUPING, uhIdentifier);
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
