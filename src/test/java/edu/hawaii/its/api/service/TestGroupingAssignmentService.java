package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingOwnerMembers;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.OwnerResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingAssignmentService {

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.owner_grouping}")
    private String OWNER_GROUPING;

    @Autowired
    private GroupingAttributeService groupingAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GroupingOwnerService groupingOwnerService;

    @Autowired
    private GrouperService grouperService;

    @MockitoSpyBean
    private GroupingsService groupingsService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private String testUid;
    private List<String> testUidList;

    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));

        testUid = uhIdentifierGenerator.getRandomMember().getUid();
        testUidList = Arrays.asList(testUid);
        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
        grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);

    }

    @Test
    public void groupingAdminsTest() {
        // Should throw an exception if current user is not an admin.
        try {
            groupingAssignmentService.groupingAdmins(testUid);
            fail("Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin.
        updateMemberService.addAdminMember(ADMIN, testUid);
        try {
            groupingAssignmentService.groupingAdmins(testUid);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }
        updateMemberService.removeAdminMember(ADMIN, testUid);

        // Fields in AdminListsHolder should not be null.
        GroupingGroupMembers groupingAdmins = groupingAssignmentService.groupingAdmins(ADMIN);
        assertNotNull(groupingAdmins.getMembers());
    }

    @Test
    public void allGroupingsTest() {
        // Should throw an exception if current user is not an admin.
        try {
            groupingAssignmentService.allGroupingPaths(testUid);
            fail("Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin.
        updateMemberService.addAdminMember(ADMIN, testUid);
        try {
            groupingAssignmentService.allGroupingPaths(testUid);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }
        updateMemberService.removeAdminMember(ADMIN, testUid);

        // Fields in groupingAll should not be null.
        GroupingPaths groupingAll = groupingAssignmentService.allGroupingPaths(ADMIN);
        assertNotNull(groupingAll.getGroupingPaths());
    }

    @Test
    public void getMembersTest() {
        List<String> groupPaths = new ArrayList<>();
        Collections.addAll(groupPaths, GROUPING, GROUPING_BASIS, GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
        Map<String, Group> groupMap = groupingAssignmentService.getMembers(ADMIN, groupPaths);
        assertNotNull(groupMap);
    }

    @Test
    public void optInOutGroupingsPathsTest() {
        // Test both getOptInGroups and getOptOutGroups()
        GroupingPaths optInGroupingsPaths =
                groupingAssignmentService.optInGroupingPaths(ADMIN, testUid);
        List<String> optInPaths = optInGroupingsPaths.getGroupingPaths().stream().map(GroupingPath::getPath).collect(Collectors.toList());
        List<String> optOutPaths = groupingAssignmentService.optOutGroupingsPaths(ADMIN, testUid);
        Set<String> intersection =
                optInPaths.stream().distinct().filter(optOutPaths::contains).collect(Collectors.toSet());
        // Should be no intersection between the two lists.
        assertTrue(intersection.isEmpty());
        // Should have no duplicates.
        Set<String> optInPathsMap = new HashSet<>();
        Set<String> optOutPathsMap = new HashSet<>();
        optInPaths.forEach(path -> {
            assertTrue(optInPathsMap.add(path));
            assertFalse(path.endsWith(GroupType.INCLUDE.value()));
            assertFalse(path.endsWith(GroupType.EXCLUDE.value()));
            assertFalse(path.endsWith(GroupType.BASIS.value()));
            assertFalse(path.endsWith(GroupType.OWNERS.value()));
            assertTrue(groupingAttributeService.isGroupAttribute(path, OptType.IN.value()));
        });
        optOutPaths.forEach(path -> {
            assertTrue(optOutPathsMap.add(path));
            assertFalse(path.endsWith(GroupType.INCLUDE.value()));
            assertFalse(path.endsWith(GroupType.EXCLUDE.value()));
            assertFalse(path.endsWith(GroupType.BASIS.value()));
            assertFalse(path.endsWith(GroupType.OWNERS.value()));
            assertTrue(groupingAttributeService.isGroupAttribute(path, OptType.OUT.value()));
        });

    }

    @Test
    public void noOptInGroupingsPathsTest() {
        given(groupingsService.optInEnabledGroupingPaths()).willReturn(Collections.emptyList());
        GroupingPaths optInGroupingsPaths =
                groupingAssignmentService.optInGroupingPaths(ADMIN, testUid);
        assertEquals(Collections.emptyList(), optInGroupingsPaths.getGroupingPaths());
    }

    @Test
    public void groupingImmediateOwners() {
        //Person
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        GroupingOwnerMembers GroupingOwnerMembers = groupingAssignmentService.groupingImmediateOwners(ADMIN, GROUPING);
        assertNotNull(GroupingOwnerMembers);
        assertTrue(GroupingOwnerMembers.getOwners().getMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(testUid)));

        updateMemberService.removeOwnerships(ADMIN, GROUPING, Collections.singletonList(testUid));

        GroupingOwnerMembers = groupingAssignmentService.groupingImmediateOwners(ADMIN, GROUPING);
        assertNotNull(GroupingOwnerMembers);
        assertFalse(GroupingOwnerMembers.getOwners().getMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(testUid)));

        //Owner-Grouping
        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        GroupingOwnerMembers ownersWithGroup = groupingAssignmentService.groupingImmediateOwners(ADMIN, GROUPING);
        assertNotNull(ownersWithGroup);
        assertTrue(ownersWithGroup.getOwners().getMembers().stream()
                        .anyMatch(member ->OWNER_GROUPING.equals(member.getName())));

        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        ownersWithGroup = groupingAssignmentService.groupingImmediateOwners(ADMIN, GROUPING);
        assertNotNull(ownersWithGroup);
        assertFalse(ownersWithGroup.getOwners().getMembers().stream()
                .anyMatch(member -> OWNER_GROUPING.equals(member.getName())));
    }

    @Test
    public void groupingAllOwners() {
        // Person
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        GroupingOwnerMembers GroupingOwnerMembers = groupingAssignmentService.groupingAllOwners(ADMIN, GROUPING);
        assertNotNull(GroupingOwnerMembers);
        assertTrue(GroupingOwnerMembers.getOwners().getMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(testUid)));

        updateMemberService.removeOwnerships(ADMIN, GROUPING, Collections.singletonList(testUid));

        GroupingOwnerMembers = groupingAssignmentService.groupingAllOwners(ADMIN, GROUPING);
        assertNotNull(GroupingOwnerMembers);
        assertFalse(GroupingOwnerMembers.getOwners().getMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(testUid)));

        //Owner-Grouping
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        GroupingOwnerMembers afterRemove = groupingAssignmentService.groupingAllOwners(ADMIN, GROUPING);
        assertNotNull(afterRemove);
        assertFalse(afterRemove.getOwners().getMembers().stream()
                .anyMatch(member -> OWNER_GROUPING.equals(member.getUid())));

        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        GroupingOwnerMembers ownersWithGroup = groupingAssignmentService.groupingAllOwners(ADMIN, GROUPING);
        assertNotNull(ownersWithGroup);
        assertTrue(ownersWithGroup.getOwners().getMembers().stream()
                .anyMatch(member -> OWNER_GROUPING.equals(member.getName())));
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));

    }

    @Test
    public void numberOfImmediateOwners() {
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
        int initialOwners = groupingAssignmentService.numberOfImmediateOwners(ADMIN, GROUPING, ADMIN);
        //Person
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        assertEquals(initialOwners + 1, groupingAssignmentService.numberOfImmediateOwners(ADMIN, GROUPING, ADMIN));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, Collections.singletonList(testUid));
        assertEquals(initialOwners, groupingAssignmentService.numberOfImmediateOwners(ADMIN, GROUPING, ADMIN));

        //Owner-Grouping
        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        assertEquals(initialOwners + 1, groupingAssignmentService.numberOfImmediateOwners(ADMIN, GROUPING, ADMIN));
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        assertEquals(initialOwners, groupingAssignmentService.numberOfImmediateOwners(ADMIN, GROUPING, ADMIN));
    }

    @Test
    public void numberOfAllOwners() {
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
        int initialOwners = groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING);
        int basisMembers = groupingOwnerService.numberOfGroupingMembers(ADMIN, OWNER_GROUPING + ":basis");
        int includeMembers = groupingOwnerService.numberOfGroupingMembers(ADMIN, OWNER_GROUPING + ":include");
        //Person
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        assertEquals(initialOwners + 1, groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, Collections.singletonList(testUid));
        assertEquals(initialOwners, groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING));

        //Owner-Grouping
        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        int duplicateOwners = groupingAssignmentService.compareOwnerGroupings(ADMIN, GROUPING).size();
        int afterAdd = groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING);
        assertEquals(initialOwners + basisMembers + includeMembers - duplicateOwners, afterAdd);
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        int afterRemove = groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING);
        assertEquals(initialOwners, afterRemove);
    }

    @Test
    public void numberOfDirectOwners() {
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
        int initialOwners = groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING);

        //Person
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        assertEquals(initialOwners + 1, groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, Collections.singletonList(testUid));
        assertEquals(initialOwners, groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING));

        //Owner-Grouping shouldn't affect the count of direct owners.
        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        assertEquals(initialOwners, groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING));
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        assertEquals(initialOwners, groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING));
    }

    @Test
    public void compareOwnerGroupingsTest() {
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));

        GetMembersResult membersResult =
                grouperService.getMembersResult(ADMIN, OWNER_GROUPING);
        List<Subject> subjects = membersResult.getSubjects();
        String duplicateOwnerUhUuid = subjects.get(0).getUhUuid();

        //there must be at least one member in OWNER_GROUPING to run this test
        if (duplicateOwnerUhUuid.isEmpty()) {
            updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
            fail("No valid members found in OWNER_GROUPING; cannot run test.");
        }
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, duplicateOwnerUhUuid);

        updateMemberService.addOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        int initialDuplicatesCount = groupingAssignmentService.compareOwnerGroupings(ADMIN, GROUPING).size();

        // Add the member as a direct owner to create the duplicate (member is already in OWNER_GROUPING).
        grouperService.addMember(ADMIN, GROUPING_OWNERS, duplicateOwnerUhUuid);

        Map<String, OwnerResult> duplicates =
                groupingAssignmentService.compareOwnerGroupings(ADMIN, GROUPING);
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, duplicateOwnerUhUuid);
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, GROUPING, List.of(OWNER_GROUPING));
        assertEquals(initialDuplicatesCount + 1, duplicates.size());
        assertTrue(duplicates.containsKey(duplicateOwnerUhUuid));
        assertEquals(duplicates.get(duplicateOwnerUhUuid).getUhUuid(), duplicateOwnerUhUuid);
        assertTrue(duplicates.get(duplicateOwnerUhUuid).getPaths().contains("DIRECT"));
        assertTrue(duplicates.get(duplicateOwnerUhUuid).getPaths().contains(OWNER_GROUPING));
    }
}