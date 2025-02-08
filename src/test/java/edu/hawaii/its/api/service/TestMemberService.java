//package edu.hawaii.its.api.service;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import edu.hawaii.its.api.configuration.SpringBootWebApplication;
//
//@ActiveProfiles("integrationTest")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest(classes = { SpringBootWebApplication.class })
//public class TestMemberService {
//
//    @Value("${groupings.api.test.grouping_many}")
//    private String GROUPING;
//
//    @Value("${groupings.api.test.admin_user}")
//    private String ADMIN;
//
//    @Autowired
//    private MemberService memberService;
//
//    @Autowired
//    private UpdateMemberService updateMemberService;
//
//    @Autowired
//    private UhIdentifierGenerator uhIdentifierGenerator;
//
//    private static String testUhUuid;
//
//    private static final String BOGUS = "bogus-string";
//
//    @BeforeEach
//    public void beforeAll() {
//        testUhUuid = uhIdentifierGenerator.getRandomMember().getUhUuid();
//    }
//
//    @Test
//    public void isOwner() {
//        updateMemberService.removeOwnership(ADMIN, GROUPING, testUhUuid);
//        assertFalse(memberService.isOwner(GROUPING, testUhUuid));
//        updateMemberService.addOwnership(ADMIN, GROUPING, testUhUuid);
//        assertTrue(memberService.isOwner(GROUPING, testUhUuid));
//    }
//
//    @Test
//    public void isAdmin() {
//        assertTrue(memberService.isAdmin(ADMIN));
//
//        updateMemberService.addAdminMember(ADMIN, testUhUuid);
//        assertTrue(memberService.isAdmin(testUhUuid));
//        updateMemberService.removeAdminMember(ADMIN, testUhUuid);
//        assertFalse(memberService.isAdmin(testUhUuid));
//
//        assertFalse(memberService.isAdmin(BOGUS));
//    }
//
//    @Test
//    public void isInclude() {
//        updateMemberService.removeIncludeMember(ADMIN, GROUPING, testUhUuid);
//        assertFalse(memberService.isInclude(GROUPING, testUhUuid));
//        updateMemberService.addIncludeMember(ADMIN, GROUPING, testUhUuid);
//        assertTrue(memberService.isInclude(GROUPING, testUhUuid));
//        updateMemberService.removeIncludeMember(ADMIN, GROUPING, testUhUuid);
//
//        assertFalse(memberService.isInclude(GROUPING, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isInclude(BOGUS, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isInclude(BOGUS, testUhUuid));
//    }
//
//    @Test
//    public void isExclude() {
//        updateMemberService.removeExcludeMember(ADMIN, GROUPING, testUhUuid);
//        assertFalse(memberService.isExclude(GROUPING, testUhUuid));
//        updateMemberService.addExcludeMember(ADMIN, GROUPING, testUhUuid);
//        assertTrue(memberService.isExclude(GROUPING, testUhUuid));
//        updateMemberService.removeExcludeMember(ADMIN, GROUPING, testUhUuid);
//
//        assertFalse(memberService.isExclude(GROUPING, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isExclude(BOGUS, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isExclude(BOGUS, testUhUuid));
//    }
//
//    @Test
//    public void isMember() {
//        updateMemberService.addIncludeMember(ADMIN, GROUPING, testUhUuid);
//        assertTrue(memberService.isMember(GROUPING, testUhUuid));
//        updateMemberService.addExcludeMember(ADMIN, GROUPING, testUhUuid);
//        assertFalse(memberService.isMember(GROUPING, testUhUuid));
//        assertTrue(memberService.isExclude(GROUPING, testUhUuid));
//
//        assertFalse(memberService.isMember(GROUPING, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isMember(BOGUS, BOGUS));
//        assertThrows(RuntimeException.class, () -> memberService.isMember(BOGUS, testUhUuid));
//    }
//}
