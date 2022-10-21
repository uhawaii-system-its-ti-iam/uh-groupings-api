package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.wrapper.AddMemberCommand;
import edu.hawaii.its.api.wrapper.RemoveMemberCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMemberService {

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    private static String INVALID_PATH = "invalid-path:exclude";
    private static String INVALID_UH_IDENT = "invalid-ident";

    @Autowired
    MemberService memberService;

    @Test
    public void testIsAdmin() {
        String uid = TEST_UH_USERNAMES.get(0);
        String uhUuid = TEST_UH_NUMBERS.get(1);

        removeMember(GROUPING_ADMINS, uid);
        removeMember(GROUPING_ADMINS, uhUuid);
        assertFalse(memberService.isAdmin(uid));
        assertFalse(memberService.isAdmin(uhUuid));

        addMember(GROUPING_ADMINS, uid);
        addMember(GROUPING_ADMINS, uhUuid);
        assertTrue(memberService.isAdmin(uid));
        assertTrue(memberService.isAdmin(uhUuid));

        removeMember(GROUPING_ADMINS, uid);
        removeMember(GROUPING_ADMINS, uhUuid);
        assertFalse(memberService.isAdmin(uid));
        assertFalse(memberService.isAdmin(uhUuid));
    }

    @Test
    public void testIsMember() {
        String uid = TEST_UH_USERNAMES.get(0);
        String uhUuid = TEST_UH_NUMBERS.get(1);

        removeMember(GROUPING_BASIS, uid);
        removeMember(GROUPING_BASIS, uhUuid);
        assertFalse(memberService.isMember(GROUPING, uid));
        assertFalse(memberService.isMember(GROUPING, uhUuid));

        addMember(GROUPING_BASIS, uid);
        addMember(GROUPING_BASIS, uhUuid);
        assertTrue(memberService.isMember(GROUPING, uid));
        assertTrue(memberService.isMember(GROUPING, uhUuid));

        removeMember(GROUPING_BASIS, uid);
        removeMember(GROUPING_BASIS, uhUuid);
        assertFalse(memberService.isMember(GROUPING, uid));
        assertFalse(memberService.isMember(GROUPING, uhUuid));

        assertFalse(memberService.isMember(GROUPING, INVALID_UH_IDENT));
    }

    @Test
    public void testIsIncludeMember() {
        String uid = TEST_UH_USERNAMES.get(0);
        String uhUuid = TEST_UH_NUMBERS.get(1);

        removeMember(GROUPING_INCLUDE, uid);
        removeMember(GROUPING_INCLUDE, uhUuid);
        assertFalse(memberService.isIncludeMember(GROUPING, uid));
        assertFalse(memberService.isIncludeMember(GROUPING, uhUuid));

        addMember(GROUPING_INCLUDE, uid);
        addMember(GROUPING_INCLUDE, uhUuid);
        assertTrue(memberService.isIncludeMember(GROUPING, uid));
        assertTrue(memberService.isIncludeMember(GROUPING, uhUuid));

        removeMember(GROUPING_INCLUDE, uid);
        removeMember(GROUPING_INCLUDE, uhUuid);
        assertFalse(memberService.isIncludeMember(GROUPING, uid));
        assertFalse(memberService.isIncludeMember(GROUPING, uhUuid));

        try {
            memberService.isIncludeMember(INVALID_PATH, uid);
            fail("Should throw an exception.");
        } catch (InvalidGroupPathException e) {
            assertEquals(INVALID_PATH, e.getReason());
        }
    }

    @Test
    public void testIsExcludeMember() {
        String uid = TEST_UH_USERNAMES.get(0);
        String uhUuid = TEST_UH_NUMBERS.get(1);

        removeMember(GROUPING_EXCLUDE, uid);
        removeMember(GROUPING_EXCLUDE, uhUuid);
        assertFalse(memberService.isExcludeMember(GROUPING, uid));
        assertFalse(memberService.isExcludeMember(GROUPING, uhUuid));

        addMember(GROUPING_EXCLUDE, uid);
        addMember(GROUPING_EXCLUDE, uhUuid);
        assertTrue(memberService.isExcludeMember(GROUPING, uid));
        assertTrue(memberService.isExcludeMember(GROUPING, uhUuid));

        removeMember(GROUPING_EXCLUDE, uid);
        removeMember(GROUPING_EXCLUDE, uhUuid);
        assertFalse(memberService.isExcludeMember(GROUPING, uid));
        assertFalse(memberService.isExcludeMember(GROUPING, uhUuid));
    }

    @Test
    public void testIsOwner() {
        String uid = TEST_UH_USERNAMES.get(0);
        String uhUuid = TEST_UH_NUMBERS.get(1);

        removeMember(GROUPING_OWNERS, uid);
        removeMember(GROUPING_OWNERS, uhUuid);
        assertFalse(memberService.isOwner(GROUPING, uid));
        assertFalse(memberService.isOwner(GROUPING, uhUuid));

        addMember(GROUPING_OWNERS, uid);
        addMember(GROUPING_OWNERS, uhUuid);
        assertTrue(memberService.isOwner(GROUPING, uid));
        assertTrue(memberService.isOwner(GROUPING, uhUuid));

        removeMember(GROUPING_OWNERS, uid);
        removeMember(GROUPING_OWNERS, uhUuid);
        assertFalse(memberService.isOwner(GROUPING, uid));
        assertFalse(memberService.isOwner(GROUPING, uhUuid));
    }

    private void addMember(String groupPath, String uhIdentifier) {
        new AddMemberCommand(groupPath, uhIdentifier).execute();
    }

    private void removeMember(String groupPath, String uhIdentifier) {
        new RemoveMemberCommand(groupPath, uhIdentifier).execute();
    }

}
