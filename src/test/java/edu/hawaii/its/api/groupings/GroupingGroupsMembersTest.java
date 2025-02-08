//package edu.hawaii.its.api.groupings;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import edu.hawaii.its.api.util.JsonUtil;
//import edu.hawaii.its.api.util.PropertyLocator;
//import edu.hawaii.its.api.wrapper.GetMembersResults;
//
//import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
//
//public class GroupingGroupsMembersTest {
//    private PropertyLocator propertyLocator;
//
//    @BeforeEach
//    public void beforeEach() {
//        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
//    }
//
//    @Test
//    public void constructor() {
//        String ownerLimit = propertyLocator.find("");
//        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers();
//        assertEquals("", groupingGroupsMembers.getGroupPath());
//        assertEquals("", groupingGroupsMembers.getResultCode());
//        assertNotNull(groupingGroupsMembers.getAllMembers());
//        assertFalse(groupingGroupsMembers.isBasis());
//        assertFalse(groupingGroupsMembers.isInclude());
//        assertFalse(groupingGroupsMembers.isExclude());
//        assertFalse(groupingGroupsMembers.isOwners());
//        assertEquals(Integer.valueOf(0), groupingGroupsMembers.getPageNumber());
//        assertTrue(groupingGroupsMembers.isPaginationComplete());
//    }
//
//    @Test
//    public void successfulResult() {
//        String onlyInclude = "testiwta";
//        String basisAndInclude = "testiwtb";
//        String onlyExclude = "testiwtc";
//        String basisAndExclude = "testiwtd";
//        String owner = "testiwte";
//        String onlyBasis = "testiwtf";
//
//        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
//        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
//        assertNotNull(wsGetMembersResults);
//        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
//        assertNotNull(getMembersResults);
//        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
//
//        assertNotNull(groupingGroupsMembers);
//        assertEquals("SUCCESS", groupingGroupsMembers.getResultCode());
//        assertEquals(0, groupingGroupsMembers.getPageNumber());
//        assertFalse(groupingGroupsMembers.isPaginationComplete());
//
//        assertTrue(groupingGroupsMembers.isBasis());
//        assertTrue(groupingGroupsMembers.isInclude());
//        assertTrue(groupingGroupsMembers.isExclude());
//        assertTrue(groupingGroupsMembers.isOwners());
//
//        GroupingGroupMembers basis = groupingGroupsMembers.getGroupingBasis();
//        GroupingGroupMembers include = groupingGroupsMembers.getGroupingInclude();
//        GroupingGroupMembers exclude = groupingGroupsMembers.getGroupingExclude();
//        GroupingGroupMembers owners = groupingGroupsMembers.getGroupingOwners();
//        GroupingMembers allMembers = groupingGroupsMembers.getAllMembers();
//
//        assertNotNull(basis);
//        assertNotNull(include);
//        assertNotNull(exclude);
//        assertNotNull(owners);
//        assertNotNull(allMembers);
//
//        assertEquals("group-path:basis", basis.getGroupPath());
//        assertEquals("group-path:include", include.getGroupPath());
//        assertEquals("group-path:exclude", exclude.getGroupPath());
//        assertEquals("group-path:owners", owners.getGroupPath());
//
//        List<GroupingGroupMember> basisMembers = basis.getMembers();
//        List<GroupingGroupMember> includeMembers = include.getMembers();
//        List<GroupingGroupMember> excludeMembers = exclude.getMembers();
//        List<GroupingGroupMember> ownersMembers = owners.getMembers();
//        List<GroupingMember> allGroupingMembers = allMembers.getMembers();
//
//        assertNotNull(basisMembers);
//        assertNotNull(includeMembers);
//        assertNotNull(excludeMembers);
//        assertNotNull(ownersMembers);
//        assertNotNull(allGroupingMembers);
//
//        assertEquals(3, basisMembers.size());
//        assertEquals(2, includeMembers.size());
//        assertEquals(2, excludeMembers.size());
//        assertEquals(1, ownersMembers.size());
//        assertEquals(3, allGroupingMembers.size());
//
//        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndExclude)));
//        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
//        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
//        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));
//
//        assertTrue(includeMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyInclude)));
//        assertTrue(includeMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
//        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));
//        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));
//
//        assertTrue(excludeMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyExclude)));
//        assertTrue(excludeMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndExclude)));
//        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
//        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));
//
//        assertTrue(ownersMembers.stream().anyMatch(member -> member.getUhUuid().equals(owner)));
//        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
//        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
//        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));
//
//        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyInclude)));
//        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));
//        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));
//        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
//        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Basis & Include"))
//                .allMatch(member -> member.getUhUuid().equals(basisAndInclude)));
//        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Basis"))
//                .allMatch(member -> member.getUhUuid().equals(onlyBasis)));
//        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Include"))
//                .allMatch(member -> member.getUhUuid().equals(onlyInclude)));
//    }
//
//    @Test
//    public void emptyResult() {
//        WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
//        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
//        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
//        assertNotNull(groupingGroupsMembers);
//        assertEquals("FAILURE", groupingGroupsMembers.getResultCode());
//        assertEquals(0, groupingGroupsMembers.getPageNumber());
//        assertTrue(groupingGroupsMembers.isPaginationComplete());
//        assertFalse(groupingGroupsMembers.isBasis());
//        assertFalse(groupingGroupsMembers.isInclude());
//        assertFalse(groupingGroupsMembers.isExclude());
//        assertFalse(groupingGroupsMembers.isOwners());
//
//        assertNotNull(groupingGroupsMembers.getGroupingBasis());
//        assertNotNull(groupingGroupsMembers.getGroupingBasis().getMembers());
//        assertTrue(groupingGroupsMembers.getGroupingBasis().getMembers().isEmpty());
//        assertEquals("", groupingGroupsMembers.getGroupingBasis().getResultCode());
//        assertEquals("", groupingGroupsMembers.getGroupingBasis().getGroupPath());
//
//        assertNotNull(groupingGroupsMembers.getGroupingInclude());
//        assertNotNull(groupingGroupsMembers.getGroupingInclude().getMembers());
//        assertTrue(groupingGroupsMembers.getGroupingInclude().getMembers().isEmpty());
//        assertEquals("", groupingGroupsMembers.getGroupingInclude().getResultCode());
//        assertEquals("", groupingGroupsMembers.getGroupingInclude().getGroupPath());
//
//        assertNotNull(groupingGroupsMembers.getGroupingExclude());
//        assertNotNull(groupingGroupsMembers.getGroupingExclude().getMembers());
//        assertTrue(groupingGroupsMembers.getGroupingExclude().getMembers().isEmpty());
//        assertEquals("", groupingGroupsMembers.getGroupingExclude().getResultCode());
//        assertEquals("", groupingGroupsMembers.getGroupingExclude().getGroupPath());
//
//        assertNotNull(groupingGroupsMembers.getGroupingOwners());
//        assertNotNull(groupingGroupsMembers.getGroupingOwners().getMembers());
//        assertTrue(groupingGroupsMembers.getGroupingOwners().getMembers().isEmpty());
//        assertEquals("", groupingGroupsMembers.getGroupingOwners().getResultCode());
//        assertEquals("", groupingGroupsMembers.getGroupingOwners().getGroupPath());
//
//        assertNotNull(groupingGroupsMembers.getAllMembers());
//        assertNotNull(groupingGroupsMembers.getAllMembers().getMembers());
//        assertTrue(groupingGroupsMembers.getAllMembers().getMembers().isEmpty());
//    }
//
//}
