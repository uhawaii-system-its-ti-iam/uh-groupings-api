package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.InvalidDescriptionException;
import edu.hawaii.its.api.groupings.GroupingPrivilegeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdateOptAttributeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateSyncDestResult;
import edu.hawaii.its.api.groupings.GroupingUpdatedAttributeResult;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
class GroupingAttributeServiceTest {

    @Autowired
    private GroupingAttributeService groupingAttributeService;

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @MockitoSpyBean
    private MemberService memberService;

    @MockitoSpyBean
    private GroupingsService groupingsService;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.trio}")
    private String TRIO;

    private final String groupPath = "path:to:grouping";

    @Test
    public void construction() {
        assertNotNull(groupingAttributeService);
        assertNotNull(grouperService);
    }

    @Test
    public void isGroupingPathTest() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();

        doReturn(groupAttributeResults)
                .when(grouperService)
                .groupAttributeResults(eq(TRIO), eq(groupPath));

        doReturn(new GroupAttributeResults())
                .when(grouperService)
                .groupAttributeResults(eq("not-an-attribute"), eq(groupPath));

        assertTrue(groupingAttributeService.isGroupingPath(groupPath));
        assertFalse(groupingAttributeService.isGroupAttribute(groupPath, "not-an-attribute"));
    }

    @Test
    public void changeGroupAttributeStatusTest() {
        AssignAttributesResults assignAttributesResults =
                groupingsTestConfiguration.assignAttributesResultsChangedTrueTestData();

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        doReturn(assignAttributesResults)
                .when(grouperService)
                .assignAttributesResults(
                        eq(ADMIN),
                        eq("group"),
                        eq("assign_attr"),
                        eq(groupPath),
                        eq("testAttribute"),
                        eq(true));

        GroupingUpdatedAttributeResult assignResult =
                groupingAttributeService.changeGroupAttributeStatus(groupPath, ADMIN, "testAttribute", true);

        assertNotNull(assignResult);

        doReturn(assignAttributesResults)
                .when(grouperService)
                .assignAttributesResults(
                        eq(ADMIN),
                        eq("group"),
                        eq("remove_attr"),
                        eq(groupPath),
                        eq("testAttribute"),
                        eq(true));

        GroupingUpdatedAttributeResult removeResult =
                groupingAttributeService.changeGroupAttributeStatus(groupPath, ADMIN, "testAttribute", false);

        assertNotNull(removeResult);

        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAttributeService.changeGroupAttributeStatus(groupPath, ADMIN, "testAttribute", true));
    }

    @Test
    public void updateDescriptionTest() {
        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        GroupingUpdateDescriptionResult expectedResult = new GroupingUpdateDescriptionResult();
        doReturn(expectedResult)
                .when(groupingsService)
                .updateGroupingDescription(eq(groupPath), eq("Valid description"));

        GroupingUpdateDescriptionResult result =
                groupingAttributeService.updateDescription(groupPath, ADMIN, "Valid description");

        assertSame(expectedResult, result);

        doReturn(false)
                .when(memberService)
                .isCurrentUserAdmin();
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                AccessDeniedException.class,
                () -> groupingAttributeService.updateDescription(groupPath, ADMIN, "Valid description"));

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();
        doReturn(false)
                .when(memberService)
                .isOwner(groupPath, ADMIN);

        assertThrows(
                InvalidDescriptionException.class,
                () -> groupingAttributeService.updateDescription(groupPath, ADMIN, "@invalid"));

        String longDescription = "a".repeat(1001);
        assertThrows(
                InvalidDescriptionException.class,
                () -> groupingAttributeService.updateDescription(groupPath, ADMIN, longDescription));;
    }

    @Test
    public void validateAndSanitizeDescriptionTest() {
        assertEquals("Valid description",
                groupingAttributeService.validateAndSanitizeDescription("Valid description"));

        assertThrows(InvalidDescriptionException.class,
                () -> groupingAttributeService.validateAndSanitizeDescription(null));

        assertThrows(InvalidDescriptionException.class,
                () -> groupingAttributeService.validateAndSanitizeDescription(""));

        assertThrows(InvalidDescriptionException.class,
                () -> groupingAttributeService.validateAndSanitizeDescription("@invalid"));

        String longDescription = "a".repeat(1001);
        assertThrows(InvalidDescriptionException.class,
                () -> groupingAttributeService.validateAndSanitizeDescription(longDescription));

    }
    @Test
    public void assignGrouperPrivilegeTest() {
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                groupingsTestConfiguration.assignGrouperPrivilegesResultsSuccessTestData();

        doReturn(assignGrouperPrivilegesResult)
                .when(grouperService)
                .assignGrouperPrivilegesResult(
                        eq(ADMIN),
                        eq(groupPath + GroupType.INCLUDE.value()),
                        eq("optin"),
                        eq("GrouperAll"),
                        eq(true),
                        eq(true));

        GroupingPrivilegeResult result =
                groupingAttributeService.assignGrouperPrivilege(
                        ADMIN,
                        "optin",
                        groupPath + GroupType.INCLUDE.value(),
                        true);

        assertNotNull(result);
        assertEquals(assignGrouperPrivilegesResult.getPrivilegeName(), result.getPrivilegeName());
    }

    @Test
    public void updateGroupingSyncDestTest() {
        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        AssignAttributesResults assignAttributesResults =
                groupingsTestConfiguration.assignAttributesResultsChangedTrueTestData();

        doReturn(assignAttributesResults)
                .when(grouperService)
                .assignAttributesResults(
                        eq(ADMIN),
                        eq("group"),
                        eq("assign_attr"),
                        eq(groupPath),
                        eq("sync-destination"),
                        eq(true));

        GroupingUpdateSyncDestResult result =
                groupingAttributeService.updateGroupingSyncDest(groupPath, ADMIN, "sync-destination", true);

        assertNotNull(result);
        assertTrue(result.getUpdatedStatus());
    }

    @Test
    public void updateOptAttributeTest() {
        OptRequest optInRequest = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withOptValue(true)
                .withGroupNameRoot(groupPath)
                .withPrivilegeType(PrivilegeType.IN)
                .withUid(ADMIN)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withOptValue(true)
                .withGroupNameRoot(groupPath)
                .withPrivilegeType(PrivilegeType.OUT)
                .withUid(ADMIN)
                .build();

        doReturn(true)
                .when(memberService)
                .isCurrentUserAdmin();

        AssignGrouperPrivilegesResult optInPrivResult =
                groupingsTestConfiguration.assignGrouperPrivilegesResultsSuccessTestData();
        AssignGrouperPrivilegesResult optOutPrivResult =
                groupingsTestConfiguration.assignGrouperPrivilegesResultsSuccessTestData();

        doReturn(optInPrivResult)
                .when(grouperService)
                .assignGrouperPrivilegesResult(
                        eq(ADMIN),
                        eq(optInRequest.getGroupName()),
                        eq(optInRequest.getPrivilegeType().value()),
                        eq("GrouperAll"),
                        eq(optInRequest.getOptValue()),
                        eq(true));

        doReturn(optOutPrivResult)
                .when(grouperService)
                .assignGrouperPrivilegesResult(
                        eq(ADMIN),
                        eq(optOutRequest.getGroupName()),
                        eq(optOutRequest.getPrivilegeType().value()),
                        eq("GrouperAll"),
                        eq(optOutRequest.getOptValue()),
                        eq(true));

        AssignAttributesResults assignAttributesResults =
                groupingsTestConfiguration.assignAttributesResultsChangedTrueTestData();

        doReturn(assignAttributesResults)
                .when(grouperService)
                .assignAttributesResults(
                        eq(ADMIN),
                        eq("group"),
                        eq("assign_attr"),
                        eq(groupPath),
                        eq(optInRequest.getOptId()),
                        eq(true));

        GroupingUpdateOptAttributeResult result =
                groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest);

        assertNotNull(result);
        assertNotNull(result.getOptInPrivilegeResult());
        assertNotNull(result.getOptOutPrivilegeResult());
    }
}