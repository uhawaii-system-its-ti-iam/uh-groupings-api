//package edu.hawaii.its.api.service;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import edu.hawaii.its.api.configuration.SpringBootWebApplication;
//import edu.hawaii.its.api.exception.AccessDeniedException;
//import edu.hawaii.its.api.groupings.GroupingPrivilegeResult;
//import edu.hawaii.its.api.groupings.GroupingUpdateOptAttributeResult;
//import edu.hawaii.its.api.type.OptRequest;
//import edu.hawaii.its.api.type.OptType;
//import edu.hawaii.its.api.type.PrivilegeType;
//
//@ActiveProfiles("integrationTest")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest(classes = { SpringBootWebApplication.class })
//public class TestGroupingAttributeService {
//
//    @Value("${groupings.api.test.grouping_many}")
//    private String GROUPING;
//
//    @Value("${groupings.api.test.grouping_many_basis}")
//    private String GROUPING_BASIS;
//
//    @Value("${groupings.api.test.grouping_many_include}")
//    private String GROUPING_INCLUDE;
//
//    @Value("${groupings.api.test.grouping_many_exclude}")
//    private String GROUPING_EXCLUDE;
//
//    @Value("${groupings.api.test.grouping_many_owners}")
//    private String GROUPING_OWNERS;
//
//    @Value("Test Many Groups In Basis")
//    private String DEFAULT_DESCRIPTION;
//
//    @Value("${groupings.api.success}")
//    private String SUCCESS;
//
//    @Value("${groupings.api.grouping_admins}")
//    private String GROUPING_ADMINS;
//
//    @Value("${groupings.api.test.admin_user}")
//    private String ADMIN;
//
//    private static final String INVALID_GROUPING_PATH = "invalid:grouping:path";
//
//    @Autowired
//    private GrouperService grouperService;
//
//    @Autowired
//    private GroupingAttributeService groupingAttributeService;
//
//    @Autowired
//    private UpdateMemberService updateMemberService;
//
//    @Autowired
//    private GroupingsService groupingsService;
//
//    @Autowired
//    private UhIdentifierGenerator uhIdentifierGenerator;
//
//    private Map<String, Boolean> attributeMap = new HashMap<>();
//    private final String SUCCESS_NOT_ALLOWED_DIDNT_EXIST = "SUCCESS_NOT_ALLOWED_DIDNT_EXIST";
//    private final String SUCCESS_ALLOWED_ALREADY_EXISTED = "SUCCESS_ALLOWED_ALREADY_EXISTED";
//    private final String SUCCESS_ALLOWED = "SUCCESS_ALLOWED";
//    private final String SUCCESS_NOT_ALLOWED = "SUCCESS_NOT_ALLOWED";
//
//    private String testUid;
//    private List<String> testUidList;
//
//    @BeforeEach
//    public void init() {
//        // Save the starting attribute settings for the test grouping.
//        attributeMap.put(OptType.IN.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
//        attributeMap.put(OptType.OUT.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);
//
//        testUid = uhIdentifierGenerator.getRandomMember().getUid();
//        testUidList = Arrays.asList(testUid);
//        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
//        grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
//        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
//    }
//
//    @AfterAll
//    public void cleanUp() {
//        // Set the test grouping's attribute settings back.
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(),
//                attributeMap.get(OptType.IN.value()));
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(),
//                attributeMap.get(OptType.OUT.value()));
//    }
//
//    @Test
//    public void isGroupingPath() {
//        assertTrue(groupingAttributeService.isGroupingPath(GROUPING));
//        assertFalse(groupingAttributeService.isGroupingPath(GROUPING_BASIS));
//        assertFalse(groupingAttributeService.isGroupingPath(GROUPING_OWNERS));
//        assertFalse(groupingAttributeService.isGroupingPath(GROUPING_INCLUDE));
//        assertFalse(groupingAttributeService.isGroupingPath(GROUPING_EXCLUDE));
//        assertFalse(groupingAttributeService.isGroupingPath(INVALID_GROUPING_PATH));
//    }
//
//    @Test
//    public void changeOptInStatusTest() {
//        // Should throw an exception if current user is not an owner or and admin.
//        OptRequest optInRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        OptRequest optOutRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//            fail("Should throw an exception if current user is not an owner or and admin.");
//        } catch (AccessDeniedException e) {
//            assertEquals("Insufficient Privileges", e.getMessage());
//        }
//        // Should not throw an exception if current user is an owner but not an admin.
//        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an owner but not an admin.");
//        }
//        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);
//
//        // Should not throw an exception if current user is an admin but not an owner.
//        updateMemberService.addAdminMember(ADMIN, testUid);
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an admin but not an owner.");
//        }
//
//        // Should throw an exception if an invalid path is passed.
//        assertThrows(NullPointerException.class, () -> groupingAttributeService.updateOptAttribute(
//                new OptRequest.Builder()
//                        .withUid(testUid)
//                        .withGroupNameRoot("bogus-path")
//                        .withPrivilegeType(PrivilegeType.IN)
//                        .withOptType(OptType.IN)
//                        .withOptValue(false)
//                        .build(),
//                new OptRequest.Builder()
//                        .withUid(testUid)
//                        .withGroupNameRoot("bogus-path")
//                        .withPrivilegeType(PrivilegeType.OUT)
//                        .withOptType(OptType.IN)
//                        .withOptValue(false)
//                        .build()
//        ));
//        updateMemberService.removeAdminMember(ADMIN, testUid);
//
//        // Should return resultCode: SUCCESS_NOT_ALLOWED_DIDNT_EXIST if false was set to false.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        GroupingUpdateOptAttributeResult groupingUpdateOptAttributeResult =
//                groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        GroupingPrivilegeResult optInResult = groupingUpdateOptAttributeResult.getOptInPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optInResult);
//        assertNotNull(optInResult.getSubject());
//        assertEquals(optInResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_NOT_ALLOWED_DIDNT_EXIST, optInResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_ALLOWED if false was set to true.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.IN)
//                .withOptValue(true)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.IN)
//                .withOptValue(true)
//                .build();
//
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optInResult = groupingUpdateOptAttributeResult.getOptInPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optInResult);
//        assertNotNull(optInResult.getSubject());
//        assertEquals(optInResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_ALLOWED, optInResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optInResult = groupingUpdateOptAttributeResult.getOptInPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optInResult);
//        assertNotNull(optInResult.getSubject());
//        assertEquals(optInResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_ALLOWED_ALREADY_EXISTED, optInResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_NOT_ALLOWED if true was set to false.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.IN)
//                .withOptValue(false)
//                .build();
//
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optInResult = groupingUpdateOptAttributeResult.getOptInPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optInResult);
//        assertNotNull(optInResult.getSubject());
//        assertEquals(optInResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_NOT_ALLOWED, optInResult.getResultCode());
//    }
//
//    @Test
//    public void changeOptOutStatusTest() {
//        // Should throw an exception if current user is not an owner or and admin.
//        OptRequest optInRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        OptRequest optOutRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//            fail("Should throw an exception if current user is not an owner or and admin.");
//        } catch (AccessDeniedException e) {
//            assertEquals("Insufficient Privileges", e.getMessage());
//        }
//
//        // Should not throw an exception if current user is an owner but not an admin.
//        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an owner but not an admin.");
//        }
//        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);
//
//        // Should not throw an exception if current user is an admin but not an owner.
//        optInRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(testUid)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        updateMemberService.addAdminMember(ADMIN, testUid);
//        try {
//            groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an admin but not an owner.");
//        }
//
//        // Should throw an exception if an invalid path is passed.
//        assertThrows(NullPointerException.class, () -> groupingAttributeService.updateOptAttribute(
//                new OptRequest.Builder()
//                        .withUid(testUid)
//                        .withGroupNameRoot("bogus-path")
//                        .withPrivilegeType(PrivilegeType.IN)
//                        .withOptType(OptType.OUT)
//                        .withOptValue(false)
//                        .build(),
//                new OptRequest.Builder()
//                        .withUid(testUid)
//                        .withGroupNameRoot("bogus-path")
//                        .withPrivilegeType(PrivilegeType.OUT)
//                        .withOptType(OptType.OUT)
//                        .withOptValue(false)
//                        .build()
//        ));
//
//        updateMemberService.removeAdminMember(ADMIN, testUid);
//
//        // Should return resultCode: SUCCESS_NOT_ALLOWED_DIDNT_EXIST if false was set to false.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        GroupingUpdateOptAttributeResult groupingUpdateOptAttributeResult =
//                groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        GroupingPrivilegeResult optOutResult = groupingUpdateOptAttributeResult.getOptOutPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optOutResult);
//        assertNotNull(optOutResult.getSubject());
//        assertEquals(optOutResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_NOT_ALLOWED_DIDNT_EXIST, optOutResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_ALLOWED if false was set to true.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.OUT)
//                .withOptValue(true)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.OUT)
//                .withOptValue(true)
//                .build();
//
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optOutResult = groupingUpdateOptAttributeResult.getOptOutPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optOutResult);
//        assertNotNull(optOutResult.getSubject());
//        assertEquals(optOutResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_ALLOWED, optOutResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optOutResult = groupingUpdateOptAttributeResult.getOptOutPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optOutResult);
//        assertNotNull(optOutResult.getSubject());
//        assertEquals(optOutResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_ALLOWED_ALREADY_EXISTED, optOutResult.getResultCode());
//
//        // Should return resultCode: SUCCESS_NOT_ALLOWED if true was set to false.
//        optInRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.IN)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        optOutRequest = new OptRequest.Builder()
//                .withUid(ADMIN)
//                .withGroupNameRoot(GROUPING)
//                .withPrivilegeType(PrivilegeType.OUT)
//                .withOptType(OptType.OUT)
//                .withOptValue(false)
//                .build();
//
//        groupingUpdateOptAttributeResult = groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);
//        optOutResult = groupingUpdateOptAttributeResult.getOptOutPrivilegeResult();
//        assertNotNull(groupingUpdateOptAttributeResult);
//        assertNotNull(optOutResult);
//        assertNotNull(optOutResult.getSubject());
//        assertEquals(optOutResult.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(SUCCESS_NOT_ALLOWED, optOutResult.getResultCode());
//    }
//
//    @Test
//    public void changeGroupAttributeStatus() {
//        // Should throw an exception if current user is not an owner or and admin.
//        try {
//            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, OptType.IN.value(), false);
//            fail("Should throw an exception if current user is not an owner or and admin.");
//        } catch (AccessDeniedException e) {
//            assertEquals("Insufficient Privileges", e.getMessage());
//        }
//        // Should not throw an exception if current user is an owner but not an admin.
//        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
//        try {
//            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, OptType.IN.value(), false);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an owner but not an admin.");
//        }
//        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);
//
//        // Should not throw an exception if current user is an admin but not an owner.
//        updateMemberService.addAdminMember(ADMIN, testUid);
//        try {
//            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, OptType.IN.value(), false);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an admin but not an owner.");
//        }
//        updateMemberService.removeAdminMember(ADMIN, testUid);
//
//        // Should throw an exception if an invalid path is passed.
//        assertThrows(NullPointerException.class,
//                () -> groupingAttributeService.changeGroupAttributeStatus("bogus-path", ADMIN, null, false));
//
//        // Should return success no matter what.
//        List<String> optList = new ArrayList<>();
//        optList.add(OptType.IN.value());
//        optList.add(OptType.OUT.value());
//
//        List<Boolean> optSwitches = new ArrayList<>();
//        optSwitches.add(false);
//        optSwitches.add(true);
//        optSwitches.add(true);
//        optSwitches.add(false);
//
//        optSwitches.forEach(bool -> {
//            optList.forEach(opt -> assertTrue(
//                    groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, opt, bool).getResultCode()
//                            .contains(SUCCESS))); // Should always be SUCCESS?
//        });
//
//    }
//
//    @Test
//    public void isGroupAttributeTest() {
//
//        // Attributes should be set to false.
//        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
//        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
//
//        // Should be true if attributes are turned on.
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), true);
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), true);
//        assertTrue(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
//        assertTrue(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
//
//        // Should be false if attributes are turned off.
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
//        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);
//        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
//        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
//
//    }
//
//    @Test
//    public void updateDescriptionTest() {
//        String descriptionOriginal = groupingsService.getGroupingDescription(GROUPING);
//        // Should throw an exception if current user is not an owner or and admin.
//        try {
//            groupingAttributeService.updateDescription(GROUPING, testUid, null);
//            fail("Should throw an exception if current user is not an owner or and admin.");
//        } catch (AccessDeniedException e) {
//            assertEquals("Insufficient Privileges", e.getMessage());
//        }
//        // Should not throw an exception if current user is an owner but not an admin.
//        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
//        try {
//            groupingAttributeService.updateDescription(GROUPING, testUid, DEFAULT_DESCRIPTION);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an owner but not an admin.");
//        }
//        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);
//
//        // Should not throw an exception if current user is an admin but not an owner.
//        updateMemberService.addAdminMember(ADMIN, testUid);
//        try {
//            groupingAttributeService.updateDescription(GROUPING, testUid, DEFAULT_DESCRIPTION);
//        } catch (AccessDeniedException e) {
//            fail("Should not throw an exception if current user is an admin but not an owner.");
//        }
//
//        // Should throw an exception if an invalid path is passed.
//        assertThrows(NullPointerException.class,
//                () -> groupingAttributeService.updateDescription("bogus-path", ADMIN, DEFAULT_DESCRIPTION));
//        updateMemberService.removeAdminMember(ADMIN, testUid);
//
//        // Should be set back to original description.
//        groupingAttributeService.updateDescription(GROUPING, ADMIN, descriptionOriginal);
//        assertEquals(descriptionOriginal, groupingsService.getGroupingDescription(GROUPING));
//    }
//}
