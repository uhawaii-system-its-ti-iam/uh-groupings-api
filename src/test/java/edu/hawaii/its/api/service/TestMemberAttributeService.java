package edu.hawaii.its.api.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MemberResult;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.GroupingPaths;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMemberAttributeService {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private List<String> testUids;
    private List<String> testUhUuids;


    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));

        GroupingMembers testGroupingMembers = uhIdentifierGenerator.getRandomMembers(5);
        testUids = testGroupingMembers.getUids();
        testUhUuids = testGroupingMembers.getUhUuids();

        testUids.forEach(testUid -> {
            grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
            grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);

            assertFalse(memberService.isOwner(GROUPING, testUid));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
            assertFalse(memberService.isAdmin(testUid));
        });
    }

    @Test
    public void memberAttributeResultsTest() {
        MemberAttributeResults results = memberAttributeService.getMemberAttributeResults(ADMIN, testUids);
        assertNotNull(results);
        HashSet<String> testUidsSet = new HashSet(testUids);
        HashSet<String> testUhUuidsSet = new HashSet(testUhUuids);

        for (MemberResult memberResult : results.getResults()) {
            assertTrue(testUidsSet.contains(memberResult.getUid()));
            assertTrue(testUhUuidsSet.contains(memberResult.getUhUuid()));
        }

        String testUid = testUids.get(0);
        List<String> testList = new ArrayList<>();
        testList.add(testUid);

        // Should not contain any invalid uhIdentifiers.
        List<String> uhIdentifiers = new ArrayList<>();
        uhIdentifiers.add("bogus-user");
        results = memberAttributeService.getMemberAttributeResults(ADMIN, uhIdentifiers);
        assertEquals(Collections.emptyList(), results.getResults());
        assertEquals(uhIdentifiers, results.getInvalid());

        // Should throw AccessDeniedException if current user is not an admin or owner.
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMemberAttributeResults("bogus-owner-admin", null));

        // Should not return an empty array of subjects if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        results = memberAttributeService.getMemberAttributeResults(testUid, testList);
        assertNotEquals(0, results.getResults().size());
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should not return an empty array if current user is an admin but not an owner.
        updateMemberService.addAdminMember(ADMIN, testUid);
        results = memberAttributeService.getMemberAttributeResults(testUid, testList);
        assertNotEquals(0, results.getResults().size());
        updateMemberService.removeAdminMember(ADMIN, testUid);
    }

    @Test
    public void memberAttributeResultsAsyncTest() {
        CompletableFuture<MemberAttributeResults> results = memberAttributeService.getMemberAttributeResultsAsync(ADMIN, testUids);
        assertNotNull(results);
        HashSet<String> testUidsSet = new HashSet(testUids);
        HashSet<String> testUhUuidsSet = new HashSet(testUhUuids);

        for (MemberResult memberResult : results.join().getResults()) {
            assertTrue(testUidsSet.contains(memberResult.getUid()));
            assertTrue(testUhUuidsSet.contains(memberResult.getUhUuid()));
        }

        String testUid = testUids.get(0);
        List<String> testList = new ArrayList<>();
        testList.add(testUid);

        // Should not contain any invalid uhIdentifiers.
        List<String> uhIdentifiers = new ArrayList<>();
        uhIdentifiers.add("bogus-user");
        results = memberAttributeService.getMemberAttributeResultsAsync(ADMIN, uhIdentifiers);
        assertNotNull(results.join().getResults());
        assertTrue(results.join().getResults().isEmpty());

        // Should throw AccessDeniedException if current user is not an admin or owner.
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMemberAttributeResults("bogus-owner-admin", null));

        // Should not return an empty array of subjects if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        results = memberAttributeService.getMemberAttributeResultsAsync(testUid, testList);
        assertNotEquals(0, results.join().getResults().size());
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should not return an empty array if current user is an admin but not an owner.
        updateMemberService.addAdminMember(ADMIN, testUid);
        results = memberAttributeService.getMemberAttributeResultsAsync(testUid, testList);
        assertNotEquals(0, results.join().getResults().size());
        updateMemberService.removeAdminMember(ADMIN, testUid);
    }

    @Test
    public void getOwnedGroupingsTest() {
        // Groupings owned by current admin should complement
        // the list of memberships that the current admin is in.
        GroupingPaths groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, ADMIN);
        ManageSubjectResults manageSubjectResults = membershipService.manageSubjectResults(ADMIN, ADMIN);
        assertNotNull(groupingsOwned);
        groupingsOwned.getGroupingPaths().forEach(groupingPath -> {
            assertTrue(
                    manageSubjectResults.getResults().stream()
                            .anyMatch(membership -> membership
                                    .getPath().equals(groupingPath.getPath())));
        });

        // Should contain grouping path if user is added as and owner.
        List<String> testList = new ArrayList<>();
        String testUid = testUids.get(0);
        testList.add(testUid);
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, testUid);
        assertFalse(
                groupingsOwned.getGroupingPaths().stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, testUid);
        assertTrue(
                groupingsOwned.getGroupingPaths().stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);
    }

    @Test
    public void getNumberOfGroupingsTest() {
        String testUid = testUids.get(0);
        List<String> testList = new ArrayList<>();
        testList.add(testUid);
        Integer numberOfGroupings = memberAttributeService.numberOfGroupings(ADMIN, testUid);
        assertNotNull(numberOfGroupings);

        // Should equal the size of the list returned from getOwnedGroupings().
        assertEquals(memberAttributeService.getOwnedGroupings(ADMIN, testUid).getGroupingPaths().size(), numberOfGroupings);
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);

        // Should increase by one if user is added as owner to a grouping.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        assertEquals(numberOfGroupings + 1, memberAttributeService.numberOfGroupings(ADMIN, testUid));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should decrease by one if user is added as owner to a grouping.
        assertEquals(numberOfGroupings, memberAttributeService.numberOfGroupings(ADMIN, testUid));
    }
}
