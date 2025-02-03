package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        doReturn(true).when(memberService).isAdmin(TEST_UIDS.get(0));

        groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                TEST_UIDS.get(0), groupingPath, pageNumber, pageSize, sortString, isAscending, null);

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
}
