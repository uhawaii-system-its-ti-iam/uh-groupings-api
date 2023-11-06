package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMemberService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

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

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UpdateMemberService updateMemberService;

    private static final String SUCCESS = "SUCCESS";

    private static String UH_UUID;

    private static final String BOGUS = "bogus-string";

    @BeforeEach
    public void beforeAll() {
        UH_UUID = TEST_UH_NUMBERS.get(0);
    }

    @Test
    public void isOwner() {
        updateMemberService.removeOwnership(ADMIN, GROUPING, UH_UUID);
        assertFalse(memberService.isOwner(GROUPING, UH_UUID));
        updateMemberService.addOwnership(ADMIN, GROUPING, UH_UUID);
        assertTrue(memberService.isOwner(GROUPING, UH_UUID));
    }

    @Test
    public void isAdmin() {
        assertTrue(memberService.isAdmin(ADMIN));

        updateMemberService.addAdminMember(ADMIN, UH_UUID);
        assertTrue(memberService.isAdmin(UH_UUID));
        updateMemberService.removeAdminMember(ADMIN, UH_UUID);
        assertFalse(memberService.isAdmin(UH_UUID));

        assertFalse(memberService.isAdmin(BOGUS));
    }

    @Test
    public void isInclude() {
        updateMemberService.removeIncludeMember(ADMIN, GROUPING, UH_UUID);
        assertFalse(memberService.isInclude(GROUPING, UH_UUID));
        updateMemberService.addIncludeMember(ADMIN, GROUPING, UH_UUID);
        assertTrue(memberService.isInclude(GROUPING, UH_UUID));
        updateMemberService.removeIncludeMember(ADMIN, GROUPING, UH_UUID);

        assertFalse(memberService.isInclude(GROUPING, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isInclude(BOGUS, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isInclude(BOGUS, UH_UUID));
    }

    @Test
    public void isExclude() {
        updateMemberService.removeExcludeMember(ADMIN, GROUPING, UH_UUID);
        assertFalse(memberService.isExclude(GROUPING, UH_UUID));
        updateMemberService.addExcludeMember(ADMIN, GROUPING, UH_UUID);
        assertTrue(memberService.isExclude(GROUPING, UH_UUID));
        updateMemberService.removeExcludeMember(ADMIN, GROUPING, UH_UUID);

        assertFalse(memberService.isExclude(GROUPING, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isExclude(BOGUS, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isExclude(BOGUS, UH_UUID));
    }

    @Test
    public void isMember() {
        updateMemberService.addIncludeMember(ADMIN, GROUPING, UH_UUID);
        assertTrue(memberService.isMember(GROUPING, UH_UUID));
        updateMemberService.addExcludeMember(ADMIN, GROUPING, UH_UUID);
        assertFalse(memberService.isMember(GROUPING, UH_UUID));
        assertTrue(memberService.isExclude(GROUPING, UH_UUID));

        assertFalse(memberService.isMember(GROUPING, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isMember(BOGUS, BOGUS));
        assertThrows(RuntimeException.class, () -> memberService.isMember(BOGUS, UH_UUID));
    }
}
