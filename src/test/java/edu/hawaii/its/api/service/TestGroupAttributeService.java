package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.hawaii.its.api.type.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Booleans;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupAttributeService {

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

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    public Environment env; // Just for the settings check.

    private Map<String, Boolean> attributeMap = new HashMap<>();
    private final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";

    private final String SUCCESS_NOT_ALLOWED_DIDNT_EXIST = "SUCCESS_NOT_ALLOWED_DIDNT_EXIST";
    private final String SUCCESS_ALLOWED_ALREADY_EXISTED = "SUCCESS_ALLOWED_ALREADY_EXISTED";
    private final String SUCCESS_ALLOWED = "SUCCESS_ALLOWED";
    private final String SUCCESS_NOT_ALLOWED = "SUCCESS_NOT_ALLOWED";

    @BeforeAll
    public void init() {
        // Save the starting attribute settings for the test grouping.
        attributeMap.put(OptType.IN.value(), groupAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        attributeMap.put(OptType.OUT.value(), groupAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);

        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUsername);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_OWNERS, testUsername);

            assertFalse(memberAttributeService.isOwner(GROUPING, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isAdmin(testUsername));
        });
    }

    @AfterAll
    public void cleanUp() {
        // Set the test grouping's attribute settings back.
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), attributeMap.get(OptType.IN.value()));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), attributeMap.get(OptType.OUT.value()));
    }

    @Test
    public void getAllSyncDestinationsTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        // Should throw an exception if current user is not an owner or and admin.
        try {
            groupAttributeService.getAllSyncDestinations(iamtst01, GROUPING);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupAttributeService.getAllSyncDestinations(iamtst01, GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupAttributeService.getAllSyncDestinations(iamtst01, GROUPING);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Should throw an exception if an invalid path is passed.
        try {
            groupAttributeService.getAllSyncDestinations(ADMIN, "bogus-path");
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should return sync destinations.
        List<SyncDestination> syncDestinations = groupAttributeService.getAllSyncDestinations(ADMIN, GROUPING);
        assertNotNull(syncDestinations);
        assertFalse(syncDestinations.isEmpty());
    }

    @Test
    public void getSyncDestinationsTest() {
        // Should throw and exception if a grouping with an invalid path is passed.
        try {
            groupAttributeService.getSyncDestinations(new Grouping("bogus-path"));
            fail("Should throw and exception if a grouping with an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should return a list of sync destinations with the proper fields set.
        List<SyncDestination> syncDestinations = groupAttributeService.getSyncDestinations(new Grouping(GROUPING));
        assertNotNull(syncDestinations);
        syncDestinations.forEach(syncDestination -> {
            assertNotNull(syncDestination.getName());
            assertNotNull(syncDestination.getDescription());
            assertNotNull(syncDestination.getTooltip());
            assertNotNull(syncDestination.isSynced());
            assertNotNull(syncDestination.isHidden());
        });
    }

    @Test
    public void changeOptInStatusTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        // Should throw an exception if current user is not an owner or and admin.
        OptRequest optInRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Should throw an exception if an invalid path is passed.
        optInRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot("bogus-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot("bogus-path")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.IN)
                .withOptValue(false)
                .build();

        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

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
                groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
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

        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_ALLOWED, optInResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
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

        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optInResult = groupingsServiceResults.get(0);
        assertNotNull(optInResult);
        assertTrue(optInResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optInResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED, optInResult.getResultCode());
    }

    @Test
    public void changeOptOutStatusTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        // Should throw an exception if current user is not an owner or and admin.
        OptRequest optInRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not throw an exception if current user is an admin but not an owner.
        optInRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot(GROUPING)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Should throw an exception if an invalid path is passed.
        optInRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot("bogus-path")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withUsername(iamtst01)
                .withGroupNameRoot("bogus-path")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(OptType.OUT)
                .withOptValue(false)
                .build();

        try {
            groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

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
                groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
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

        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_ALLOWED, optOutResult.getResultCode());

        // Should return resultCode: SUCCESS_ALLOWED_ALREADY_EXISTED if true was set to true.
        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
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

        groupingsServiceResults = groupAttributeService.changeOptStatus(optInRequest, optOutRequest);
        optOutResult = groupingsServiceResults.get(1);
        assertNotNull(optOutResult);
        assertTrue(optOutResult.getAction().contains(GROUPING_INCLUDE));
        assertNull(optOutResult.getPerson());
        assertEquals(SUCCESS_NOT_ALLOWED, optOutResult.getResultCode());
    }

    @Test
    public void changeGroupAttributeStatus() {

        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        // Should throw an exception if current user is not an owner or and admin.
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, iamtst01, null, false);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, iamtst01, null, false);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, iamtst01, null, false);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Should throw an exception if an invalid path is passed.
        try {
            groupAttributeService.changeGroupAttributeStatus("bogus-path", ADMIN, null, false);
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should return success no matter what.
        List<String> optList = new ArrayList<>();
        boolean[] arr = { false, true, true, false };
        List<Boolean> optSwitches = Booleans.asList(arr);
        optList.add(OptType.IN.value());
        optList.add(OptType.OUT.value());
        optSwitches.forEach(bool -> {
            optList.forEach(opt -> assertTrue(
                    groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, opt, bool).getResultCode()
                            .contains(SUCCESS))); // Should always be SUCCESS?
        });

    }

    @Test
    public void isGroupAttributeTest() {
        // Should throw an exception if an invalid path is passed.
        try {
            groupAttributeService.isGroupAttribute("bogus-path", OptType.IN.value());
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
        // Should throw an exception if an invalid attribute is passed.
        try {
            groupAttributeService.isGroupAttribute(GROUPING, "bogus-attribute");
            fail("Should throw an exception if an invalid attribute is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains("ATTRIBUTE_DEF_NAME_NOT_FOUND"));
        }

        // Attributes should be set to false.
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

        // Should be true if attributes are turned on.
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), true);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

        // Should be false if attributes are turned off.
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));

    }

    @Test
    public void updateDescriptionTest() {
        String descriptionOriginal = grouperApiService.descriptionOf(GROUPING);
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        // Should throw an exception if current user is not an owner or and admin.
        try {
            groupAttributeService.updateDescription(GROUPING, iamtst01, null);
            fail("Should throw an exception if current user is not an owner or and admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupAttributeService.updateDescription(GROUPING, iamtst01, DEFAULT_DESCRIPTION);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupAttributeService.updateDescription(GROUPING, iamtst01, DEFAULT_DESCRIPTION);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Should throw an exception if an invalid path is passed.
        try {
            groupAttributeService.updateDescription("bogus-path", ADMIN, DEFAULT_DESCRIPTION);
            fail("Should throw an exception if an invalid path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should be set to default description
        GroupingsServiceResult groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION);
        assertEquals(DEFAULT_DESCRIPTION, grouperApiService.descriptionOf(GROUPING));
        assertTrue(groupingsServiceResult.getResultCode().contains(SUCCESS));

        // Should be set back to original description.
        groupAttributeService.updateDescription(GROUPING, ADMIN, descriptionOriginal);
        assertEquals(descriptionOriginal, grouperApiService.descriptionOf(GROUPING));
    }
}