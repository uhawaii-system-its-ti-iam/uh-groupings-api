package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingPagedMembers;
import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingOwnerServiceTest {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    private String groupingPath = "tmp:grouping:path";

    @MockitoSpyBean
    private GrouperService grouperService;

    @MockitoSpyBean
    private MemberService memberService;

    @Autowired
    private GroupingOwnerService groupingOwnerService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @BeforeEach
    public void beforeEach() {
        reset(grouperService, memberService);
    }

    @Test
    public void construction() {
        assertNotNull(groupingOwnerService);
    }

    @Test
    public void numberOfGroupingMembersTest() {
        GetMembersResult getMembersResult = groupingsTestConfiguration.getMembersResultsSuccessTestData()
                .getMembersResults().get(0);

        doReturn(getMembersResult).when(grouperService).getMembersResult(TEST_UIDS.get(0), groupingPath);

        Integer numberOfGroupingMembers = groupingOwnerService.numberOfGroupingMembers(TEST_UIDS.get(0), groupingPath);
        assertNotNull(numberOfGroupingMembers);
        assertEquals(getMembersResult.getSubjects().size(), numberOfGroupingMembers);
    }

    @Test
    public void paginatedGroupingTest() {
        GetMembersResults getMembersResults = groupingsTestConfiguration.getMembersResultsSuccessTestData();
        assertNotNull(getMembersResults);

        List<String> groupPaths = Collections.singletonList(groupingPath);
        Integer pageNumber = 1;
        Integer pageSize = 10;
        String sortString = "name";
        Boolean isAscending = true;

        doReturn(getMembersResults).when(grouperService)
                .getMembersResults(TEST_UIDS.get(0), groupPaths, pageNumber, pageSize, sortString, isAscending);

        GroupingGroupsMembers result = groupingOwnerService.paginatedGrouping(
                TEST_UIDS.get(0), groupPaths, pageNumber, pageSize, sortString, isAscending);

        assertNotNull(result);
        assertEquals(pageNumber, result.getPageNumber());
    }

    @Test
    public void getAllMembersSortBranchesTest() {
        mockAllMembersPages();

        GroupingPagedMembers nameResult = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "name", true);
        assertPagedMembers(nameResult);
        assertSorted(nameResult.getMembers(), GroupingMember::getName, true);

        GroupingPagedMembers uidResult = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "uid", true);
        assertPagedMembers(uidResult);
        assertSorted(uidResult.getMembers(), GroupingMember::getUid, true);

        GroupingPagedMembers uhUuidResult = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "uhUuid", true);
        assertPagedMembers(uhUuidResult);
        assertSorted(uhUuidResult.getMembers(), GroupingMember::getUhUuid, true);

        GroupingPagedMembers whereListedResult = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "whereListed", true);
        assertPagedMembers(whereListedResult);
        assertSorted(whereListedResult.getMembers(), GroupingMember::getWhereListed, true);

        GroupingPagedMembers defaultResult = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "bad-sort", true);
        assertPagedMembers(defaultResult);
        assertSorted(defaultResult.getMembers(), GroupingMember::getName, true);
    }

    @Test
    public void getAllMembersDescendingSortTest() {
        mockAllMembersPages();

        GroupingPagedMembers result = groupingOwnerService.getAllMembers(
                ADMIN, allMemberGroupPaths(), 1, 10, "name", false);

        assertPagedMembers(result);
        assertSorted(result.getMembers(), GroupingMember::getName, false);
    }

    @Test
    public void getAllMembersMissingCompositeThrowsTest() {
        doReturn(emptyGetMembersResults()).when(grouperService).getMembersResults(
                ADMIN,
                allMemberGroupPaths(),
                1,
                10,
                "name",
                true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                groupingOwnerService.getAllMembers(
                        ADMIN,
                        allMemberGroupPaths(),
                        1,
                        10,
                        "name",
                        true));

        assertEquals("Composite group missing", exception.getMessage());
    }

    @Test
    public void getAllMembersEmptyCompositeReturnsEmptyResultTest() {
        GetMembersResults emptyCompositePage = getMembersResults(
                getMembersResult(groupingPath),
                getMembersResult(groupingPath + GroupType.BASIS.value()),
                getMembersResult(groupingPath + GroupType.INCLUDE.value()),
                getMembersResult(groupingPath + GroupType.EXCLUDE.value()),
                getMembersResult(groupingPath + GroupType.OWNERS.value()));

        doReturn(emptyCompositePage).when(grouperService).getMembersResults(
                ADMIN,
                allMemberGroupPaths(),
                1,
                10,
                "name",
                true);

        GroupingPagedMembers result = groupingOwnerService.getAllMembers(
                ADMIN,
                allMemberGroupPaths(),
                1,
                10,
                "name",
                true);

        assertNotNull(result);
        assertNotNull(result.getMembers());
        assertTrue(result.getMembers().isEmpty());
        assertEquals(Integer.valueOf(1), result.getPageNumber());
        assertEquals(Integer.valueOf(0), result.getTotalCount());
    }

    @Test
    public void startAllMembersProgressSuccessTest() throws Exception {
        mockAllMembersPages();

        Map<String, Object> response = groupingOwnerService.startAllMembersProgress(
                ADMIN,
                allMemberGroupPaths(),
                10,
                "name",
                true);

        assertNotNull(response);
        assertNotNull(response.get("requestId"));
        assertEquals(0, response.get("loadedCount"));
        assertEquals(false, response.get("complete"));
        assertEquals(false, response.get("failed"));
        assertEquals("", response.get("message"));

        String requestId = response.get("requestId").toString();

        Map<String, Object> progress = waitForCompletion(requestId);

        assertEquals(requestId, progress.get("requestId"));
        assertEquals(true, progress.get("complete"));
        assertEquals(false, progress.get("failed"));
        assertEquals("", progress.get("message"));

        GroupingPagedMembers result = groupingOwnerService.getAllMembersResult(requestId);

        assertPagedMembers(result);
        assertEquals(result.getTotalCount(), progress.get("loadedCount"));
    }

    @Test
    public void startAllMembersProgressFailureWithMessageTest() throws Exception {
        doThrow(new RuntimeException("boom")).when(grouperService).getMembersResults(
                anyString(),
                anyList(),
                anyInt(),
                anyInt(),
                eq("name"),
                eq(true));

        Map<String, Object> response = groupingOwnerService.startAllMembersProgress(
                ADMIN,
                allMemberGroupPaths(),
                10,
                "name",
                true);

        String requestId = response.get("requestId").toString();

        Map<String, Object> progress = waitForCompletion(requestId);

        assertEquals(requestId, progress.get("requestId"));
        assertEquals(false, progress.get("complete"));
        assertEquals(true, progress.get("failed"));
        assertEquals("boom", progress.get("message"));
        assertNull(groupingOwnerService.getAllMembersResult(requestId));
    }

    @Test
    public void startAllMembersProgressFailureWithDefaultMessageTest() throws Exception {
        doThrow(new RuntimeException()).when(grouperService).getMembersResults(
                anyString(),
                anyList(),
                anyInt(),
                anyInt(),
                eq("name"),
                eq(true));

        Map<String, Object> response = groupingOwnerService.startAllMembersProgress(
                ADMIN,
                allMemberGroupPaths(),
                10,
                "name",
                true);

        String requestId = response.get("requestId").toString();

        Map<String, Object> progress = waitForCompletion(requestId);

        assertEquals(requestId, progress.get("requestId"));
        assertEquals(false, progress.get("complete"));
        assertEquals(true, progress.get("failed"));
        assertEquals("Unable to load all members.", progress.get("message"));
        assertNull(groupingOwnerService.getAllMembersResult(requestId));
    }

    @Test
    public void getAllMembersProgressRequestNotFoundTest() {
        String requestId = "missing-request-id";

        Map<String, Object> response = groupingOwnerService.getAllMembersProgress(requestId);

        assertEquals(requestId, response.get("requestId"));
        assertEquals(0, response.get("loadedCount"));
        assertEquals(false, response.get("complete"));
        assertEquals(true, response.get("failed"));
        assertEquals("Request not found.", response.get("message"));
    }

    @Test
    public void getAllMembersResultRequestNotFoundTest() {
        assertNull(groupingOwnerService.getAllMembersResult("missing-request-id"));
    }

    @Test
    public void getGroupingMembersTest() {
        GetMembersResult getMembersResult = groupingsTestConfiguration.getMembersResultsSuccessTestData()
                .getMembersResults().get(0);

        Integer pageNumber = 1;
        Integer pageSize = 10;
        String sortString = "name";
        Boolean isAscending = true;

        doReturn(getMembersResult).when(grouperService)
                .getMembersResult(TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending);

        GroupingGroupMembers groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();

        String searchString = "test";

        doReturn(true).when(memberService).isAdmin(TEST_UIDS.get(0));

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, null);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, "");

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(subjectsResults).when(grouperService).getSubjects(groupingPath, searchString);

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, searchString);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(false).when(memberService).isAdmin(TEST_UIDS.get(0));
        doReturn(true).when(memberService).isOwner(groupingPath, TEST_UIDS.get(0));

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, searchString);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(false).when(memberService).isAdmin(TEST_UIDS.get(0));
        doReturn(false).when(memberService).isOwner(groupingPath, TEST_UIDS.get(0));

        assertThrows(AccessDeniedException.class,
                () -> groupingOwnerService.getGroupingMembers(
                        TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, searchString));
    }

    @Test
    public void getGroupingMembersWhereListedTest() {
        HasMembersResults basis = groupingsTestConfiguration.hasMemberResultsIsMembersBasisTestData();
        HasMembersResults include = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
        HasMembersResults notMembersBasis = groupingsTestConfiguration.hasMemberResultsIsNotMembersBasisTestData();
        HasMembersResults notMembersInclude = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();

        doReturn(basis).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);
        doReturn(include).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        GroupingMembers groupingMembers = groupingOwnerService.getGroupingMembersWhereListed(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("Basis & Include")));

        doReturn(basis).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);
        doReturn(notMembersInclude).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getGroupingMembersWhereListed(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("Basis")));

        doReturn(notMembersBasis).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);
        doReturn(include).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getGroupingMembersWhereListed(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("Include")));

        doReturn(notMembersBasis).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);
        doReturn(notMembersInclude).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getGroupingMembersWhereListed(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("")));
    }

    @Test
    public void getGroupingMembersIsBasisTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersBasisTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);

        GroupingMembers groupingMembers = groupingOwnerService.getGroupingMembersIsBasis(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("Basis")));

        hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.BASIS.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getGroupingMembersIsBasis(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().stream().allMatch(member -> member.getWhereListed().equals("")));
    }

    @Test
    public void groupingOptAttributesTest() {
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        assertNotNull(groupAttributeResults);

        doReturn(groupAttributeResults).when(grouperService).groupAttributeResult(TEST_UIDS.get(0), groupingPath);

        GroupingOptAttributes result = groupingOwnerService.groupingOptAttributes(TEST_UIDS.get(0), groupingPath);
        assertNotNull(result);
    }

    @Test
    public void groupingsDescriptionTest() {
        FindGroupsResults findGroupsResults = groupingsTestConfiguration.findGroupsResultsDescriptionTestData();
        assertNotNull(findGroupsResults);

        doReturn(findGroupsResults).when(grouperService).findGroupsResults(TEST_UIDS.get(0), groupingPath);

        GroupingDescription result = groupingOwnerService.groupingsDescription(TEST_UIDS.get(0), groupingPath);
        assertNotNull(result);
    }

    @Test
    public void groupingsSyncDestinationsTest() {
        List<AttributesResult> attributesResultList = new ArrayList<>();
        AttributesResult mockAttribute = mock(AttributesResult.class);
        attributesResultList.add(mockAttribute);

        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        GroupingSyncDestination groupingSyncDestination = groupingsTestConfiguration.attributeDescriptionTestData();

        assertNotNull(findAttributesResults);
        assertNotNull(groupAttributeResults);
        assertNotNull(groupingSyncDestination);

        when(findAttributesResults.getResults()).thenReturn(attributesResultList);
        when(mockAttribute.getDescription()).thenReturn(JsonUtil.asJson(groupingSyncDestination));
        when(mockAttribute.getName()).thenReturn("test-attribute");

        doReturn(findAttributesResults).when(grouperService)
                .findAttributesResults(TEST_UIDS.get(0), SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);
        doReturn(groupAttributeResults).when(grouperService)
                .groupAttributeResults(eq(TEST_UIDS.get(0)), any(), eq(groupingPath));

        GroupingSyncDestinations result =
                groupingOwnerService.groupingsSyncDestinations(TEST_UIDS.get(0), groupingPath);
        assertNotNull(result);
    }

    @Test
    public void createGroupingSyncDestinationListTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult attributesResult = mock(AttributesResult.class);
        List<AttributesResult> attributesList = new ArrayList<>();
        attributesList.add(attributesResult);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        List<Group> groups = Collections.singletonList(group);

        GroupAttribute groupAttribute = mock(GroupAttribute.class);
        when(groupAttribute.getAttributeName()).thenReturn("test-attribute");
        List<GroupAttribute> groupAttributes = Collections.singletonList(groupAttribute);

        when(findAttributesResults.getResults()).thenReturn(attributesList);
        when(groupAttributeResults.getGroups()).thenReturn(groups);
        when(groupAttributeResults.getGroupAttributes()).thenReturn(groupAttributes);

        GroupingSyncDestination mockDestination = new GroupingSyncDestination();
        mockDestination.setDescription("Test description with ${srhfgs}");
        when(attributesResult.getDescription()).thenReturn(JsonUtil.asJson(mockDestination));
        when(attributesResult.getName()).thenReturn("test-attribute");

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        assertNotNull(result);
        assertEquals(1, result.size());
        GroupingSyncDestination destination = result.get(0);
        assertEquals("test-attribute", destination.getName());
        assertEquals("Test description with test-group", destination.getDescription());
    }

    @Test
    public void createGroupingSyncDestinationListNoGroupAndNotSyncedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult attributesResult = mock(AttributesResult.class);
        List<AttributesResult> attributesList = Collections.singletonList(attributesResult);

        GroupAttribute groupAttribute = mock(GroupAttribute.class);
        when(groupAttribute.getAttributeName()).thenReturn("different-attribute");

        GroupingSyncDestination mockDestination = new GroupingSyncDestination();
        mockDestination.setDescription("Test description with ${srhfgs}");

        when(findAttributesResults.getResults()).thenReturn(attributesList);
        when(groupAttributeResults.getGroups()).thenReturn(new ArrayList<>());
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.singletonList(groupAttribute));
        when(attributesResult.getDescription()).thenReturn(JsonUtil.asJson(mockDestination));
        when(attributesResult.getName()).thenReturn("test-attribute");

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-attribute", result.get(0).getName());
        assertEquals("Test description with ", result.get(0).getDescription());
    }

    @Test
    public void createGroupingSyncDestinationListSortsDescriptionsTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult secondAttribute = mock(AttributesResult.class);
        AttributesResult firstAttribute = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");

        when(findAttributesResults.getResults()).thenReturn(List.of(secondAttribute, firstAttribute));
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(new ArrayList<>());

        GroupingSyncDestination secondDestination = new GroupingSyncDestination();
        secondDestination.setDescription("Zulu ${srhfgs}");
        when(secondAttribute.getDescription()).thenReturn(JsonUtil.asJson(secondDestination));
        when(secondAttribute.getName()).thenReturn("second-attribute");

        GroupingSyncDestination firstDestination = new GroupingSyncDestination();
        firstDestination.setDescription("Alpha ${srhfgs}");
        when(firstAttribute.getDescription()).thenReturn(JsonUtil.asJson(firstDestination));
        when(firstAttribute.getName()).thenReturn("first-attribute");

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alpha test-group", result.get(0).getDescription());
        assertEquals("Zulu test-group", result.get(1).getDescription());
    }

    @Test
    public void getMembersExistInIncludeTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        GroupingMembers groupingMembers = groupingOwnerService.getMembersExistInInclude(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertFalse(groupingMembers.getMembers().isEmpty());
        assertEquals(TEST_UIDS.size(), groupingMembers.getMembers().size());

        hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.INCLUDE.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getMembersExistInInclude(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().isEmpty());
    }

    @Test
    public void getMembersExistInExcludeTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.EXCLUDE.value(), TEST_UIDS);

        GroupingMembers groupingMembers = groupingOwnerService.getMembersExistInExclude(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertFalse(groupingMembers.getMembers().isEmpty());
        assertEquals(TEST_UIDS.size(), groupingMembers.getMembers().size());

        hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.EXCLUDE.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getMembersExistInExclude(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().isEmpty());
    }

    @Test
    public void getMembersExistInOwnersTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.OWNERS.value(), TEST_UIDS);

        GroupingMembers groupingMembers = groupingOwnerService.getMembersExistInOwners(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertFalse(groupingMembers.getMembers().isEmpty());
        assertEquals(TEST_UIDS.size(), groupingMembers.getMembers().size());

        hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
        doReturn(hasMembersResults).when(grouperService)
                .hasMembersResults(ADMIN, groupingPath + GroupType.OWNERS.value(), TEST_UIDS);

        groupingMembers = groupingOwnerService.getMembersExistInOwners(ADMIN, groupingPath, TEST_UIDS);
        assertNotNull(groupingMembers);
        assertTrue(groupingMembers.getMembers().isEmpty());
    }

    private void mockAllMembersPages() {
        GetMembersResults pageOne = allMembersPage();
        GetMembersResults emptyPage = emptyGetMembersResults();

        doReturn(pageOne).when(grouperService).getMembersResults(
                ADMIN,
                allMemberGroupPaths(),
                1,
                10,
                "name",
                true);

        doReturn(emptyPage).when(grouperService).getMembersResults(
                ADMIN,
                allMemberGroupPaths(),
                2,
                10,
                "name",
                true);
    }

    private List<String> allMemberGroupPaths() {
        return List.of(
                groupingPath,
                groupingPath + GroupType.BASIS.value(),
                groupingPath + GroupType.INCLUDE.value(),
                groupingPath + GroupType.EXCLUDE.value(),
                groupingPath + GroupType.OWNERS.value());
    }

    private GetMembersResults allMembersPage() {
        Subject basisOnly = new Subject("b-uid", "Bob", "b-uuid");
        Subject includeOnly = new Subject("i-uid", "Ivy", "i-uuid");
        Subject basisAndInclude = new Subject("bi-uid", "Adam", "bi-uuid");

        return getMembersResults(
                getMembersResult(groupingPath, basisOnly, includeOnly, basisAndInclude),
                getMembersResult(groupingPath + GroupType.BASIS.value(), basisOnly, basisAndInclude),
                getMembersResult(groupingPath + GroupType.INCLUDE.value(), includeOnly, basisAndInclude),
                getMembersResult(groupingPath + GroupType.EXCLUDE.value()),
                getMembersResult(groupingPath + GroupType.OWNERS.value()));
    }

    private GetMembersResults emptyGetMembersResults() {
        GetMembersResults getMembersResults = mock(GetMembersResults.class);

        when(getMembersResults.getResultCode()).thenReturn("SUCCESS");
        when(getMembersResults.getMembersResults()).thenReturn(new ArrayList<>());

        return getMembersResults;
    }

    private GetMembersResults getMembersResults(GetMembersResult... results) {
        GetMembersResults getMembersResults = mock(GetMembersResults.class);

        when(getMembersResults.getResultCode()).thenReturn("SUCCESS");
        when(getMembersResults.getMembersResults()).thenReturn(List.of(results));

        return getMembersResults;
    }

    private GetMembersResult getMembersResult(String groupPath, Subject... subjects) {
        GetMembersResult getMembersResult = mock(GetMembersResult.class);
        Group group = mock(Group.class);

        when(group.getGroupPath()).thenReturn(groupPath);
        when(getMembersResult.getResultCode()).thenReturn("SUCCESS");
        when(getMembersResult.getGroup()).thenReturn(group);
        when(getMembersResult.getSubjects()).thenReturn(List.of(subjects));

        return getMembersResult;
    }

    private void assertPagedMembers(GroupingPagedMembers groupingPagedMembers) {
        assertNotNull(groupingPagedMembers);
        assertNotNull(groupingPagedMembers.getMembers());
        assertEquals(Integer.valueOf(1), groupingPagedMembers.getPageNumber());
        assertEquals(Integer.valueOf(groupingPagedMembers.getMembers().size()), groupingPagedMembers.getTotalCount());
    }

    private void assertSorted(List<GroupingMember> members,
                              Function<GroupingMember, String> keyExtractor,
                              boolean isAscending) {
        List<String> values = members.stream()
                .map(member -> safeString(keyExtractor.apply(member)))
                .toList();

        for (int i = 1; i < values.size(); i++) {
            int comparison = String.CASE_INSENSITIVE_ORDER.compare(values.get(i - 1), values.get(i));

            if (isAscending) {
                assertTrue(comparison <= 0);
            } else {
                assertTrue(comparison >= 0);
            }
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private Map<String, Object> waitForCompletion(String requestId) throws InterruptedException {
        Map<String, Object> progress = groupingOwnerService.getAllMembersProgress(requestId);
        int attempts = 0;

        while (attempts < 100
                && !Boolean.TRUE.equals(progress.get("complete"))
                && !Boolean.TRUE.equals(progress.get("failed"))) {
            Thread.sleep(25);
            progress = groupingOwnerService.getAllMembersProgress(requestId);
            attempts++;
        }

        return progress;
    }
}