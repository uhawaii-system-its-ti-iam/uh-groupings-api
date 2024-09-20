package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.CommandException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.type.OptType;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateMemberService {

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingAttributeService groupingAttributeService;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private List<String> testUids;
    private List<String> testUhUuids;
    private Map<String, Boolean> attributeMap = new HashMap<>();

    @BeforeAll
    public void init() {
        GroupingMembers testGroupingMembers = uhIdentifierGenerator.getRandomMembers(5);
        testUids = testGroupingMembers.getUids();
        testUhUuids = testGroupingMembers.getUhUuids();

        // Initial setting for testing opt-in opt-out related functions in update member service
        attributeMap.put(OptType.IN.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        attributeMap.put(OptType.OUT.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);

        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUids.get(0));
        grouperService.removeMembers(ADMIN, GROUPING_INCLUDE, testUids);
        grouperService.removeMembers(ADMIN, GROUPING_EXCLUDE, testUids);
    }

    @Test
    public void addRemoveAdminTest() {
        // With uh number.
        assertFalse(memberService.isAdmin(testUhUuids.get(0)));
        updateMemberService.addAdminMember(ADMIN, testUhUuids.get(0));
        assertTrue(memberService.isAdmin(testUhUuids.get(0)));
        updateMemberService.removeAdminMember(ADMIN, testUhUuids.get(0));
        assertFalse(memberService.isAdmin(testUhUuids.get(0)));

        // With uh uid.
        assertFalse(memberService.isAdmin(testUids.get(0)));
        updateMemberService.addAdminMember(ADMIN, testUids.get(0));
        assertTrue(memberService.isAdmin(testUids.get(0)));
        updateMemberService.removeAdminMember(ADMIN, testUids.get(0));
        assertFalse(memberService.isAdmin(testUids.get(0)));

        try {
            updateMemberService.addAdminMember(ADMIN, "bogus-admin-to-add");
            fail("Should throw an exception if an invalid adminToAdd is passed.");
        } catch (UhMemberNotFoundException e) {
            assertNull(e.getCause());
        }

        try {
            updateMemberService.removeAdminMember(ADMIN, "bogus-admin-to-remove");
            fail("Should throw an exception if an invalid adminToRemove is passed.");
        } catch (UhMemberNotFoundException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void checkIfAdminUserTest() {
        try {
            updateMemberService.checkIfAdminUser(testUhUuids.get(0));
            fail("Should throw an exception if identifier is not an admin.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }
        try {
            updateMemberService.checkIfAdminUser(testUids.get(0));
            fail("Should throw an exception if identifier is not an admin.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }

        try {
            updateMemberService.checkIfAdminUser(ADMIN);
        } catch (AccessDeniedException e) {
            fail("Should not throw exception if current user is admin.");
        }
    }

    @Test
    public void uidAddRemoveIncludeExcludeMembersTest() {
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids);
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    public void uidAddRemoveIncludeExcludeMembersAsyncTest() {
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembersAsync(ADMIN, GROUPING, testUids).join();
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addExcludeMembersAsync(ADMIN, GROUPING, testUids).join();
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembersAsync(ADMIN, GROUPING, testUids).join();
        updateMemberService.addExcludeMembersAsync(ADMIN, GROUPING, testUids).join();
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembersAsync(ADMIN, GROUPING, testUids).join();
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    public void uidAddRemoveIncludeExcludeMemberTest() {
        String uid = testUids.get(0);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.addIncludeMember(ADMIN, GROUPING, uid);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.removeIncludeMember(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.addExcludeMember(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.removeExcludeMember(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.addIncludeMember(ADMIN, GROUPING, uid);
        updateMemberService.addExcludeMember(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.addIncludeMember(ADMIN, GROUPING, uid);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));

        updateMemberService.removeIncludeMember(ADMIN, GROUPING, uid);
    }

    @Test
    public void optTest() {
        String num = testUhUuids.get(0);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, num));

        updateMemberService.optIn(ADMIN, GROUPING, num);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, num));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, num));

        updateMemberService.optOut(ADMIN, GROUPING, num);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, num));
        assertTrue(memberService.isMember(GROUPING_EXCLUDE, num));

        updateMemberService.optIn(ADMIN, GROUPING, num);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, num));
        assertFalse(memberService.isMember(GROUPING_EXCLUDE, num));

        removeGroupMember(GROUPING_INCLUDE, num);
    }

    @Test
    public void removeFromGroupsTest() {
        String uhNum = testUhUuids.get(0);

        updateMemberService.addOwnership(ADMIN, GROUPING, uhNum);
        updateMemberService.addIncludeMember(ADMIN, GROUPING, uhNum);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, uhNum));
        assertTrue(memberService.isMember(GROUPING_OWNERS, uhNum));

        String[] array = { GROUPING_OWNERS, GROUPING_INCLUDE, GROUPING_EXCLUDE };
        List<String> groupPaths = Arrays.asList(array);

        updateMemberService.removeFromGroups(ADMIN, uhNum, groupPaths);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, uhNum));
        assertFalse(memberService.isMember(GROUPING_OWNERS, uhNum));

        array = new String[] { GROUPING_OWNERS, GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING };
        groupPaths = Arrays.asList(array);

        try {
            updateMemberService.removeFromGroups(ADMIN, uhNum, groupPaths);
            fail("Should throw an exception an invalid groupPath is in the group list");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }
    }

    @Test
    public void resetGroupTest() {
        List<String> includes = testUids.subList(0, 2);
        List<String> excludes = testUids.subList(3, 5);
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, includes);
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, excludes);

        GroupingReplaceGroupMembersResult resultInclude = updateMemberService.resetIncludeGroup(ADMIN, GROUPING);
        GroupingReplaceGroupMembersResult resultExclude = updateMemberService.resetExcludeGroup(ADMIN, GROUPING);

        assertEquals(SUCCESS, resultInclude.getResultCode());
        assertEquals(SUCCESS, resultExclude.getResultCode());
        for (String str : excludes) {
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, str));
        }
        for (String str : includes) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, str));
        }
    }

    @Test
    public void resetGroupAsyncTest() {
        List<String> includes = testUids.subList(0, 2);
        List<String> excludes = testUids.subList(3, 5);
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, includes);
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, excludes);

        GroupingReplaceGroupMembersResult resultInclude =
                updateMemberService.resetIncludeGroupAsync(ADMIN, GROUPING).join();
        GroupingReplaceGroupMembersResult resultExclude =
                updateMemberService.resetExcludeGroupAsync(ADMIN, GROUPING).join();

        assertEquals(SUCCESS, resultInclude.getResultCode());
        assertEquals(SUCCESS, resultExclude.getResultCode());
        for (String str : excludes) {
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, str));
        }
        for (String str : includes) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, str));
        }
    }

    @Test
    public void addRemoveOwnershipsTest() {
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUids);
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_OWNERS, uid));
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUids);

        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_OWNERS, uid));
        }
    }

    @Test
    public void addRemoveOwnershipTest() {
        String uid = testUids.get(0);
        updateMemberService.addOwnership(ADMIN, GROUPING, uid);
        assertTrue(memberService.isMember(GROUPING_OWNERS, uid));
        updateMemberService.removeOwnership(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_OWNERS, uid));
        updateMemberService.addAdminMember(ADMIN, uid);
        updateMemberService.removeOwnership(ADMIN, GROUPING, uid);
        updateMemberService.removeAdminMember(ADMIN, uid);
    }

    @Test
    public void checkIfOwnerOrAdminUserTest() {
        // Should not throw an exception if current user is an admin and an owner.
        try {
            updateMemberService.checkIfOwnerOrAdminUser(ADMIN, GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }

        // Should not throw an exception if current user is an owner of grouping.
        addGroupMember(GROUPING_OWNERS, testUhUuids.get(0));
        try {
            updateMemberService.checkIfOwnerOrAdminUser(testUhUuids.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner of grouping.");
        }

        // Should not throw an exception if current user is an owner of grouping and an admin.
        addGroupMember(GROUPING_ADMINS, testUhUuids.get(0));
        try {
            updateMemberService.checkIfOwnerOrAdminUser(testUhUuids.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner of grouping and an admin.");
        }
        removeGroupMember(GROUPING_OWNERS, testUhUuids.get(0));

        // Should not throw an exception if current user an admin but not an owner of grouping.
        try {
            updateMemberService.checkIfOwnerOrAdminUser(testUhUuids.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user an admin but not an owner of grouping.");
        }
        removeGroupMember(GROUPING_ADMINS, testUhUuids.get(0));

        // Should throw is not an admin or an owner of grouping.
        try {
            updateMemberService.checkIfOwnerOrAdminUser(testUhUuids.get(0), GROUPING);
            fail("Should throw an exception is not an admin or an owner of grouping.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void checkIfSelfOptOrAdminTest() {
        try {
            updateMemberService.checkIfSelfOptOrAdmin(testUhUuids.get(0), testUhUuids.get(1));
            fail("Should throw an exception if currentUser is not admin and currentUser is not self opting.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(ADMIN, testUhUuids.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is admin but currentUser is not self opting.");
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(ADMIN, ADMIN);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is admin and currentUser is self opting.");
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(testUhUuids.get(0), testUhUuids.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is not admin but currentUser is self opting.");
        }

    }

//    @Test
//    public void addRemovePathOwnershipsTest(){
//        // Todo integration test for updating grouping owners with path owners
//    }

//    @Test
//    public void checkAllMembersAfterAddRemovePathOwnershipsTest(){
//        // Todo integration test for checking the changes of members' group after adding and removing path owners
//    }

    @Test
    public void validateOptInActionForAlreadyOptedUser() {
        String testUid = testUhUuids.get(0);
        updateMemberService.addAdminMember(ADMIN, testUid);
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUids);

        try {
            updateMemberService.optIn(ADMIN, GROUPING, testUid);
            updateMemberService.optIn(ADMIN, GROUPING, testUid);
        } catch (Exception e) {
            fail("should not throw an exception if user does self opt in when user already opted in");
        }

         try {
            updateMemberService.removeIncludeMember(ADMIN, GROUPING, testUid);
            updateMemberService.optIn(ADMIN, GROUPING, testUid);
            assertTrue(memberService.isMember(GROUPING_INCLUDE, testUid));
        } catch (CommandException e) {
            fail("Should not throw an exception because opt in operation can be executed after removing include member");
        }
         updateMemberService.removeIncludeMember(ADMIN, GROUPING, testUid);
    }

    @Test
    public void validateOptOutActionForAlreadyOptedUser() {
        String testUid = testUhUuids.get(0);
        updateMemberService.addAdminMember(ADMIN, testUid);
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUids);
        try {
            updateMemberService.optOut(ADMIN, GROUPING, testUid);
            updateMemberService.optOut(ADMIN, GROUPING, testUid);
        } catch (CommandException e) {
            fail("should not throw an exception if user does self opt out when user already opted out");
        }

        updateMemberService.removeExcludeMember(ADMIN, GROUPING, testUid);

        try {
            updateMemberService.removeIncludeMember(ADMIN, GROUPING, testUid);
            updateMemberService.optOut(ADMIN, GROUPING, testUid);
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
        } catch (CommandException e) {
            fail("Should not throw an exception because opt out operation is not executed after removing include member");
        }
    }

    private void addGroupMember(String groupPath, String uhIdentifier) {
        grouperService.addMember(ADMIN, groupPath, uhIdentifier);
    }

    private void removeGroupMember(String groupPath, String uhIdentifier) {
        grouperService.removeMember(ADMIN, groupPath, uhIdentifier);
    }

}
