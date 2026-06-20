package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
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

    @MockitoSpyBean
    private EmailService emailService;

    @Autowired
    private GroupingOwnerService groupingOwnerService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

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

        SubjectsResults subjectsResults = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();;

        String searchString = "test";

        doReturn(true).when(memberService).isCurrentUserAdmin();

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, null);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(subjectsResults).when(grouperService).getSubjects(groupingPath, searchString);

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, searchString);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(false).when(memberService).isCurrentUserAdmin();
        doReturn(true).when(memberService).isOwner(groupingPath, TEST_UIDS.get(0));

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, searchString);

        assertNotNull(groupingGroupMembers);
        assertFalse(groupingGroupMembers.getMembers().isEmpty());

        doReturn(false).when(memberService).isCurrentUserAdmin();
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

        FindAttributesResults findAttributesResults =
                mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults =
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData();
        GroupingSyncDestination groupingSyncDestination = groupingsTestConfiguration.attributeDescriptionTestData();

        assertNotNull(findAttributesResults);
        assertNotNull(groupAttributeResults);
        assertNotNull(groupingSyncDestination);

        when(findAttributesResults.getResults()).thenReturn(attributesResultList);
        when(mockAttribute.getDescription()).thenReturn(JsonUtil.asJson(groupingSyncDestination));
        doReturn(findAttributesResults).when(grouperService)
                .findAttributesResults(TEST_UIDS.get(0), SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);
        doReturn(groupAttributeResults).when(grouperService)
                .groupAttributeResults(eq(TEST_UIDS.get(0)), any(), eq(groupingPath));

        GroupingSyncDestinations result =
                groupingOwnerService.groupingsSyncDestinations(TEST_UIDS.get(0), groupingPath);
        assertNotNull(result);
    }

    @Test
    public void groupingsSyncDestinationsGrouperErrorReturnsEmptyResultTest() {
        doThrow(new RuntimeException("Grouper unavailable"))
                .when(grouperService)
                .findAttributesResults(TEST_UIDS.get(0), SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);

        clearInvocations(emailService);

        GroupingSyncDestinations result =
                groupingOwnerService.groupingsSyncDestinations(TEST_UIDS.get(0), groupingPath);

        assertNotNull(result);
        assertNotNull(result.getSyncDestinations());
        assertTrue(result.getSyncDestinations().isEmpty());
        assertEquals("FAILURE", result.getResultCode());
        verify(emailService).sendWithStack(any(Exception.class), eq("Sync Destination Error"), anyString());
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
        mockDestination.setTooltip("Test tooltip for ${srhfgs}");
        when(attributesResult.getDescription()).thenReturn(JsonUtil.asJson(mockDestination));
        when(attributesResult.getName()).thenReturn("test-attribute");

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        assertNotNull(result);
        assertEquals(1, result.size());
        GroupingSyncDestination destination = result.get(0);
        assertEquals("test-attribute", destination.getName());
        assertEquals("Test description with test-group", destination.getDescription());
        assertEquals("Test tooltip for test-group", destination.getTooltip());
    }

    @Test
    public void createGroupingSyncDestinationListNullDescriptionSkippedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        // Bad entry: null description — simulates a Grouper attribute with no description set.
        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:bad");
        when(badAttributesResult.getDescription()).thenReturn(null);

        // Good entry: valid JSON description.
        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid description");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        // The bad entry is skipped; the good entry is returned without error.
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());
    }

    @Test
    public void createGroupingSyncDestinationListBlankDescriptionSkippedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:blank");
        when(badAttributesResult.getDescription()).thenReturn("   ");

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());
    }

    @Test
    public void createGroupingSyncDestinationListMalformedJsonSkippedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult1 = mock(AttributesResult.class);
        AttributesResult goodAttributesResult2 = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        // Bad entry: malformed JSON that cannot be deserialized into a GroupingSyncDestination.
        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:bad");
        when(badAttributesResult.getDescription()).thenReturn("{NOT_VALID_JSON}");

        // Good entries: valid JSON descriptions.
        GroupingSyncDestination goodDestination1 = new GroupingSyncDestination();
        goodDestination1.setDescription("Sync to Google");
        when(goodAttributesResult1.getName()).thenReturn("uh-grouping:sync:google");
        when(goodAttributesResult1.getDescription()).thenReturn(JsonUtil.asJson(goodDestination1));

        GroupingSyncDestination goodDestination2 = new GroupingSyncDestination();
        goodDestination2.setDescription("Sync to Listserv");
        when(goodAttributesResult2.getName()).thenReturn("uh-grouping:sync:listserv");
        when(goodAttributesResult2.getDescription()).thenReturn(JsonUtil.asJson(goodDestination2));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(goodAttributesResult1, badAttributesResult, goodAttributesResult2));

        List<GroupingSyncDestination> result = groupingOwnerService.createGroupingSyncDestinationList(
                findAttributesResults, groupAttributeResults);

        // The bad entry is skipped; both good entries are returned correctly.
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getName().equals("uh-grouping:sync:google")));
        assertTrue(result.stream().anyMatch(d -> d.getName().equals("uh-grouping:sync:listserv")));
        assertFalse(result.stream().anyMatch(d -> d.getName().equals("uh-grouping:sync:bad")));
    }

    @Test
    public void createGroupingSyncDestinationListNullJsonResultSkippedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:null-json");
        when(badAttributesResult.getDescription()).thenReturn("null");

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());
    }

    @Test
    public void createGroupingSyncDestinationListMissingDestinationDescriptionSkippedTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:missing-description");
        when(badAttributesResult.getDescription()).thenReturn("{\"tooltip\":\"Missing description\"}");

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());
    }

    @Test
    public void createGroupingSyncDestinationListWithoutRequestUsesUnknownPathForErrorEmailTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:bad");
        when(badAttributesResult.getDescription()).thenReturn(null);

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        clearInvocations(emailService);
        RequestContextHolder.resetRequestAttributes();

        List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());
        verify(emailService).sendWithStack(any(Exception.class), eq("Sync Destination Error"), eq("unknown"));
    }

    @Test
    public void createGroupingSyncDestinationListWithRequestUsesRequestPathForErrorEmailTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:bad");
        when(badAttributesResult.getDescription()).thenReturn(null);

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/groupings/tmp:grouping:path/groupings-sync-destinations");

        clearInvocations(emailService);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                    groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("uh-grouping:sync:good", result.get(0).getName());
            verify(emailService).sendWithStack(any(Exception.class), eq("Sync Destination Error"),
                    eq("/groupings/tmp:grouping:path/groupings-sync-destinations"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void createGroupingSyncDestinationListMultipleErrorsSendsOneAggregateEmailTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult1 = mock(AttributesResult.class);
        AttributesResult badAttributesResult2 = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult1.getName()).thenReturn("uh-grouping:sync:bad-null");
        when(badAttributesResult1.getDescription()).thenReturn(null);

        when(badAttributesResult2.getName()).thenReturn("uh-grouping:sync:bad-json");
        when(badAttributesResult2.getDescription()).thenReturn("{NOT_VALID_JSON}");

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult1, goodAttributesResult, badAttributesResult2));

        clearInvocations(emailService);

        List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uh-grouping:sync:good", result.get(0).getName());

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(emailService, times(1)).sendWithStack(exceptionCaptor.capture(), eq("Sync Destination Error"),
                anyString());
        assertTrue(exceptionCaptor.getValue().getMessage().contains("Skipped 2 malformed sync destination(s)"));
        assertTrue(exceptionCaptor.getValue().getMessage().contains("uh-grouping:sync:bad-null"));
        assertTrue(exceptionCaptor.getValue().getMessage().contains("uh-grouping:sync:bad-json"));
        assertEquals(2, exceptionCaptor.getValue().getSuppressed().length);
    }

    @Test
    public void createGroupingSyncDestinationListEmailErrorDoesNotStopValidDestinationsTest() {
        FindAttributesResults findAttributesResults = mock(FindAttributesResults.class);
        GroupAttributeResults groupAttributeResults = mock(GroupAttributeResults.class);

        AttributesResult badAttributesResult = mock(AttributesResult.class);
        AttributesResult goodAttributesResult = mock(AttributesResult.class);

        Group group = mock(Group.class);
        when(group.getExtension()).thenReturn("test-group");
        when(groupAttributeResults.getGroups()).thenReturn(Collections.singletonList(group));
        when(groupAttributeResults.getGroupAttributes()).thenReturn(Collections.emptyList());

        when(badAttributesResult.getName()).thenReturn("uh-grouping:sync:bad");
        when(badAttributesResult.getDescription()).thenReturn(null);

        GroupingSyncDestination goodDestination = new GroupingSyncDestination();
        goodDestination.setDescription("Valid destination");
        when(goodAttributesResult.getName()).thenReturn("uh-grouping:sync:good");
        when(goodAttributesResult.getDescription()).thenReturn(JsonUtil.asJson(goodDestination));

        when(findAttributesResults.getResults())
                .thenReturn(Arrays.asList(badAttributesResult, goodAttributesResult));

        clearInvocations(emailService);
        doThrow(new RuntimeException("Email failed"))
                .when(emailService)
                .sendWithStack(any(Exception.class), eq("Sync Destination Error"), anyString());

        try {
            List<GroupingSyncDestination> result = assertDoesNotThrow(() ->
                    groupingOwnerService.createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults));

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("uh-grouping:sync:good", result.get(0).getName());
            verify(emailService).sendWithStack(any(Exception.class), eq("Sync Destination Error"), anyString());
        } finally {
            reset(emailService);
        }
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
}
