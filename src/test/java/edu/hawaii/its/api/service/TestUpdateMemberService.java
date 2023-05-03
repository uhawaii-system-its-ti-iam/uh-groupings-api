package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateMemberService {

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

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_UH_USERNAMES;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GrouperApiService grouperApiService;

    private final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    private final String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";
    private final String SUCCESS_WASNT_IMMEDIATE = "SUCCESS_WASNT_IMMEDIATE";

    @BeforeAll
    public void init() {
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_UH_NUMBERS.get(0));
        grouperApiService.removeMembers(GROUPING_INCLUDE, TEST_UH_NUMBERS);
        grouperApiService.removeMembers(GROUPING_EXCLUDE, TEST_UH_NUMBERS);

        grouperApiService.removeMember(GROUPING_ADMINS, TEST_UH_USERNAMES.get(0));
        grouperApiService.removeMembers(GROUPING_INCLUDE, TEST_UH_USERNAMES);
        grouperApiService.removeMembers(GROUPING_EXCLUDE, TEST_UH_USERNAMES);
    }

    @Test
    public void addRemoveAdminTest() {
        // With uh number.
        assertFalse(memberService.isAdmin(TEST_UH_NUMBERS.get(0)));
        updateMemberService.addAdmin(ADMIN, TEST_UH_NUMBERS.get(0));
        assertTrue(memberService.isAdmin(TEST_UH_NUMBERS.get(0)));
        updateMemberService.removeAdmin(ADMIN, TEST_UH_NUMBERS.get(0));
        assertFalse(memberService.isAdmin(TEST_UH_NUMBERS.get(0)));

        // With uh username.
        assertFalse(memberService.isAdmin(TEST_UH_USERNAMES.get(0)));
        updateMemberService.addAdmin(ADMIN, TEST_UH_USERNAMES.get(0));
        assertTrue(memberService.isAdmin(TEST_UH_USERNAMES.get(0)));
        updateMemberService.removeAdmin(ADMIN, TEST_UH_USERNAMES.get(0));
        assertFalse(memberService.isAdmin(TEST_UH_USERNAMES.get(0)));

        try {
            updateMemberService.addAdmin(ADMIN, "bogus-admin-to-add");
            fail("Should throw an exception if an invalid adminToAdd is passed.");
        } catch (UhMemberNotFoundException e) {
            assertNull(e.getCause());
        }

        try {
            updateMemberService.removeAdmin(ADMIN, "bogus-admin-to-remove");
            fail("Should throw an exception if an invalid adminToRemove is passed.");
        } catch (UhMemberNotFoundException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void checkIfAdminUserTest() {
        try {
            updateMemberService.checkIfAdminUser(TEST_UH_NUMBERS.get(0));
            fail("Should throw an exception if identifier is not an admin.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }
        try {
            updateMemberService.checkIfAdminUser(TEST_UH_USERNAMES.get(0));
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
        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addExcludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
            assertTrue(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.addIncludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, uid));
        }

        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, TEST_UH_USERNAMES);
    }

    @Test
    public void uidAddRemoveIncludeExcludeMemberTest() {
        String uid = TEST_UH_USERNAMES.get(0);
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
        String num = TEST_UH_NUMBERS.get(0);
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
        String uhNum = TEST_UH_NUMBERS.get(0);

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
        List<String> includes = TEST_UH_USERNAMES.subList(0, 2);
        List<String> excludes = TEST_UH_USERNAMES.subList(3, 5);
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
    public void addRemoveOwnershipsTest() {
        updateMemberService.addOwnerships(ADMIN, GROUPING, TEST_UH_USERNAMES);
        for (String uid : TEST_UH_USERNAMES) {
            assertTrue(memberService.isMember(GROUPING_OWNERS, uid));
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, TEST_UH_USERNAMES);

        for (String uid : TEST_UH_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_OWNERS, uid));
        }
    }

    @Test
    public void addRemoveOwnershipTest() {
        String uid = TEST_UH_USERNAMES.get(0);
        updateMemberService.addOwnership(ADMIN, GROUPING, uid);
        assertTrue(memberService.isMember(GROUPING_OWNERS, uid));
        updateMemberService.removeOwnership(ADMIN, GROUPING, uid);
        assertFalse(memberService.isMember(GROUPING_OWNERS, uid));
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
        addGroupMember(GROUPING_OWNERS, TEST_UH_NUMBERS.get(0));
        try {
            updateMemberService.checkIfOwnerOrAdminUser(TEST_UH_NUMBERS.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner of grouping.");
        }

        // Should not throw an exception if current user is an owner of grouping and an admin.
        addGroupMember(GROUPING_ADMINS, TEST_UH_NUMBERS.get(0));
        try {
            updateMemberService.checkIfOwnerOrAdminUser(TEST_UH_NUMBERS.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner of grouping and an admin.");
        }
        removeGroupMember(GROUPING_OWNERS, TEST_UH_NUMBERS.get(0));

        // Should not throw an exception if current user an admin but not an owner of grouping.
        try {
            updateMemberService.checkIfOwnerOrAdminUser(TEST_UH_NUMBERS.get(0), GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user an admin but not an owner of grouping.");
        }
        removeGroupMember(GROUPING_ADMINS, TEST_UH_NUMBERS.get(0));

        // Should throw is not an admin or an owner of grouping.
        try {
            updateMemberService.checkIfOwnerOrAdminUser(TEST_UH_NUMBERS.get(0), GROUPING);
            fail("Should throw an exception is not an admin or an owner of grouping.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void checkIfSelfOptOrAdminTest() {
        try {
            updateMemberService.checkIfSelfOptOrAdmin(TEST_UH_NUMBERS.get(0), TEST_UH_NUMBERS.get(1));
            fail("Should throw an exception if currentUser is not admin and currentUser is not self opting.");
        } catch (AccessDeniedException e) {
            assertNull(e.getCause());
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(ADMIN, TEST_UH_NUMBERS.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is admin but currentUser is not self opting.");
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(ADMIN, ADMIN);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is admin and currentUser is self opting.");
        }

        try {
            updateMemberService.checkIfSelfOptOrAdmin(TEST_UH_NUMBERS.get(0), TEST_UH_NUMBERS.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if currentUser is not admin but currentUser is self opting.");
        }

    }

    private void addGroupMember(String groupPath, String uhIdentifier) {
        grouperApiService.addMember(groupPath, uhIdentifier);
    }

    private void removeGroupMember(String groupPath, String uhIdentifier) {
        grouperApiService.removeMember(groupPath, uhIdentifier);
    }

}
