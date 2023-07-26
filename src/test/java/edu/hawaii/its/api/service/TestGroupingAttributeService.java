package edu.hawaii.its.api.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingAttributeService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupingAttributeService groupingAttributeService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private Map<String, Boolean> attributeMap = new HashMap<>();
    private final String SUCCESS_NOT_ALLOWED_DIDNT_EXIST = "SUCCESS_NOT_ALLOWED_DIDNT_EXIST";
    private final String SUCCESS_ALLOWED_ALREADY_EXISTED = "SUCCESS_ALLOWED_ALREADY_EXISTED";
    private final String SUCCESS_ALLOWED = "SUCCESS_ALLOWED";
    private final String SUCCESS_NOT_ALLOWED = "SUCCESS_NOT_ALLOWED";

    private String testUid;
    private List<String> testUidList;

    @BeforeEach
    public void init() {
        // Save the starting attribute settings for the test grouping.
        attributeMap.put(OptType.IN.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        attributeMap.put(OptType.OUT.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);

        testUid = uhIdentifierGenerator.getRandomMember().getUid();
        testUidList = Arrays.asList(testUid);
        grouperApiService.removeMember(GROUPING_ADMINS, testUid);
        grouperApiService.removeMember(GROUPING_INCLUDE, testUid);
        grouperApiService.removeMember(GROUPING_EXCLUDE, testUid);
        grouperApiService.removeMember(GROUPING_OWNERS, testUid);
    }

    @AfterAll
    public void cleanUp() {
        // Set the test grouping's attribute settings back.
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(),
                attributeMap.get(OptType.IN.value()));
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(),
                attributeMap.get(OptType.OUT.value()));
    }

    @Test
    public void changeOptInStatusTest() {
        // Should throw an exception if current user is not an owner or and admin.
        OptRequest optInRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);

        // Should not throw an exception if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUid);
        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should throw an exception if an invalid path is passed.
        assertThrows(NullPointerException.class, () -> groupingAttributeService.changeOptStatus(
                new OptRequest.Builder()
                        .withUsername(testUid)
                        .withGroupNameRoot("bogus-path")
                        .withPrivilegeType(PrivilegeType.IN)
                        .withOptType(OptType.IN)
                        .withOptValue(false)
                        .build(),
                new OptRequest.Builder()
                        .withUsername(testUid)
                        .withGroupNameRoot("bogus-path")
                        .withPrivilegeType(PrivilegeType.OUT)
                        .withOptType(OptType.IN)
                        .withOptValue(false)
                        .build()
        ));
        updateMemberService.removeAdmin(ADMIN, testUid);

        // Should return resultCode: SUCCESS_NOT_ALLOWED_DIDNT_EXIST if false was set to false.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        List<GroupingsServiceResult> groupingsServiceResults =
                groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        GroupingsServiceResult optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED_DIDNT_EXIST, optInResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED if false was set to true.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(true)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(true)
                .build();

        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_ALLOWED, optInResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_ALLOWED_ALREADY_EXISTED, optInResult.getResultCode());

        // Should return resultCode: SUCCESS_NOT_ALLOWED if true was set to false.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED, optInResult.getResultCode());
    }

    @Test
    public void changeOptOutStatusTest() {
        // Should throw an exception if current user is not an owner or and admin.
        OptRequest optInRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);

        // Should not throw an exception if current user is an admin but not an owner.
        optInRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(testUid)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        updateMemberService.addAdmin(ADMIN, testUid);
        try {
            groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should throw an exception if an invalid path is passed.
        assertThrows(NullPointerException.class, () -> groupingAttributeService.changeOptStatus(
                new OptRequest.Builder()
                        .withUsername(testUid)
                        .withGroupNameRoot("bogus-path")
                        .withPrivilegeType(PrivilegeType.IN)
                        .withOptType(OptType.OUT)
                        .withOptValue(false)
                        .build(),
                new OptRequest.Builder()
                        .withUsername(testUid)
                        .withGroupNameRoot("bogus-path")
                        .withPrivilegeType(PrivilegeType.OUT)
                        .withOptType(OptType.OUT)
                        .withOptValue(false)
                        .build()
        ));

        updateMemberService.removeAdmin(ADMIN, testUid);

        // Should return resultCode: SUCCESS_NOT_ALLOWED_DIDNT_EXIST if false was set to false.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        List<GroupingsServiceResult> groupingsServiceResults =
                groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        GroupingsServiceResult optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED_DIDNT_EXIST, optOutResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED if false was set to true.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(true)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(true)
                .build();

        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_ALLOWED, optOutResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_ALLOWED_ALREADY_EXISTED, optOutResult.getResultCode());

        // Should return resultCode: SUCCESS_NOT_ALLOWED if true was set to false.
        optInRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(ADMIN)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        groupingsServiceResults = groupingAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED, optOutResult.getResultCode());
    }

    @Test
    public void changeGroupAttributeStatus() {
        // Should throw an exception if current user is not an owner or and admin.
        try {
            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, null, false);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
        try {
            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, null, false);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);

        // Should not throw an exception if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUid);
        try {
            groupingAttributeService.changeGroupAttributeStatus(GROUPING, testUid, null, false);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        updateMemberService.removeAdmin(ADMIN, testUid);

        // Should throw an exception if an invalid path is passed.
        assertThrows(NullPointerException.class,
                () -> groupingAttributeService.changeGroupAttributeStatus("bogus-path", ADMIN, null, false));

        // Should return success no matter what.
        List<String> optList = new ArrayList<>();
        optList.add(OptType.IN.value());
        optList.add(OptType.OUT.value());

        List<Boolean> optSwitches = new ArrayList<>();
        optSwitches.add(false);
        optSwitches.add(true);
        optSwitches.add(true);
        optSwitches.add(false);

        optSwitches.forEach(bool -> {
            optList.forEach(opt -> assertTrue(
                    groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, opt, bool).getResultCode()
                            .contains(SUCCESS))); // Should always be SUCCESS?
        });

    }

    @Test
    public void isGroupAttributeTest() {

        // Attributes should be set to false.
        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

        // Should be true if attributes are turned on.
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), true);
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), true);
        assertTrue(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertTrue(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

        // Should be false if attributes are turned off.
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);
        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertFalse(groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

    }

    @Test
    public void updateDescriptionTest() {
        String descriptionOriginal = groupingsService.getGroupingDescription(GROUPING);
        // Should throw an exception if current user is not an owner or and admin.
        try {
            groupingAttributeService.updateDescription(GROUPING, testUid, null);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUidList);
        try {
            groupingAttributeService.updateDescription(GROUPING, testUid, DEFAULT_DESCRIPTION);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUidList);

        // Should not throw an exception if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUid);
        try {
            groupingAttributeService.updateDescription(GROUPING, testUid, DEFAULT_DESCRIPTION);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should throw an exception if an invalid path is passed.
        assertThrows(NullPointerException.class,
                () -> groupingAttributeService.updateDescription("bogus-path", ADMIN, DEFAULT_DESCRIPTION));
        updateMemberService.removeAdmin(ADMIN, testUid);

        // Should be set back to original description.
        groupingAttributeService.updateDescription(GROUPING, ADMIN, descriptionOriginal);
        assertEquals(descriptionOriginal, groupingsService.getGroupingDescription(GROUPING));
    }
}