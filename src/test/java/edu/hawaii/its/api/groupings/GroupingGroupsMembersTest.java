package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupingGroupsMembersTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void constructor() {
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers();
        assertEquals("", groupingGroupsMembers.getGroupPath());
        assertEquals("", groupingGroupsMembers.getResultCode());
        assertNotNull(groupingGroupsMembers.getAllMembers());
        assertFalse(groupingGroupsMembers.isBasis());
        assertFalse(groupingGroupsMembers.isInclude());
        assertFalse(groupingGroupsMembers.isExclude());
        assertFalse(groupingGroupsMembers.isOwners());
        assertEquals(Integer.valueOf(0), groupingGroupsMembers.getPageNumber());
        assertTrue(groupingGroupsMembers.isPaginationComplete());
    }

    @Test
    public void successfulResult() {
        String onlyInclude = "testiwta";
        String basisAndInclude = "testiwtb";
        String onlyExclude = "testiwtc";
        String basisAndExclude = "testiwtd";
        String owner = "testiwte";
        String onlyBasis = "testiwtf";

        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        assertNotNull(wsGetMembersResults);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertNotNull(getMembersResults);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);

        assertNotNull(groupingGroupsMembers);
        assertEquals("SUCCESS", groupingGroupsMembers.getResultCode());
        assertEquals(0, groupingGroupsMembers.getPageNumber());
        assertFalse(groupingGroupsMembers.isPaginationComplete());

        assertTrue(groupingGroupsMembers.isBasis());
        assertTrue(groupingGroupsMembers.isInclude());
        assertTrue(groupingGroupsMembers.isExclude());
        assertTrue(groupingGroupsMembers.isOwners());

        GroupingGroupMembers basis = groupingGroupsMembers.getGroupingBasis();
        GroupingGroupMembers include = groupingGroupsMembers.getGroupingInclude();
        GroupingGroupMembers exclude = groupingGroupsMembers.getGroupingExclude();
        GroupingGroupMembers owners = groupingGroupsMembers.getGroupingOwners();
        GroupingMembers allMembers = groupingGroupsMembers.getAllMembers();

        assertNotNull(basis);
        assertNotNull(include);
        assertNotNull(exclude);
        assertNotNull(owners);
        assertNotNull(allMembers);

        assertEquals("group-path:basis", basis.getGroupPath());
        assertEquals("group-path:include", include.getGroupPath());
        assertEquals("group-path:exclude", exclude.getGroupPath());
        assertEquals("group-path:owners", owners.getGroupPath());

        List<GroupingGroupMember> basisMembers = basis.getMembers();
        List<GroupingGroupMember> includeMembers = include.getMembers();
        List<GroupingGroupMember> excludeMembers = exclude.getMembers();
        List<GroupingGroupMember> ownersMembers = owners.getMembers();
        List<GroupingMember> allGroupingMembers = allMembers.getMembers();

        assertNotNull(basisMembers);
        assertNotNull(includeMembers);
        assertNotNull(excludeMembers);
        assertNotNull(ownersMembers);
        assertNotNull(allGroupingMembers);

        assertEquals(3, basisMembers.size());
        assertEquals(2, includeMembers.size());
        assertEquals(2, excludeMembers.size());
        assertEquals(1, ownersMembers.size());
        assertEquals(3, allGroupingMembers.size());

        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(basisMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndExclude)));
        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
        assertTrue(basisMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));

        assertTrue(includeMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyInclude)));
        assertTrue(includeMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));
        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(includeMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));

        assertTrue(excludeMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyExclude)));
        assertTrue(excludeMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndExclude)));
        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(excludeMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));

        assertTrue(ownersMembers.stream().anyMatch(member -> member.getUhUuid().equals(owner)));
        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyInclude)));
        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(ownersMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));

        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(onlyInclude)));
        assertTrue(allGroupingMembers.stream().anyMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(basisAndExclude)));
        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(owner)));
        assertTrue(allGroupingMembers.stream().noneMatch(member -> member.getUhUuid().equals(onlyExclude)));
        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Basis & Include"))
                .allMatch(member -> member.getUhUuid().equals(basisAndInclude)));
        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Basis"))
                .allMatch(member -> member.getUhUuid().equals(onlyBasis)));
        assertTrue(allGroupingMembers.stream().filter(member -> member.getWhereListed().equals("Include"))
                .allMatch(member -> member.getUhUuid().equals(onlyInclude)));
    }

    @Test
    public void emptyResult() {
        WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        assertNotNull(groupingGroupsMembers);
        assertEquals("FAILURE", groupingGroupsMembers.getResultCode());
        assertEquals(0, groupingGroupsMembers.getPageNumber());
        assertTrue(groupingGroupsMembers.isPaginationComplete());
        assertFalse(groupingGroupsMembers.isBasis());
        assertFalse(groupingGroupsMembers.isInclude());
        assertFalse(groupingGroupsMembers.isExclude());
        assertFalse(groupingGroupsMembers.isOwners());

        assertNotNull(groupingGroupsMembers.getGroupingBasis());
        assertNotNull(groupingGroupsMembers.getGroupingBasis().getMembers());
        assertTrue(groupingGroupsMembers.getGroupingBasis().getMembers().isEmpty());
        assertEquals("", groupingGroupsMembers.getGroupingBasis().getResultCode());
        assertEquals("", groupingGroupsMembers.getGroupingBasis().getGroupPath());

        assertNotNull(groupingGroupsMembers.getGroupingInclude());
        assertNotNull(groupingGroupsMembers.getGroupingInclude().getMembers());
        assertTrue(groupingGroupsMembers.getGroupingInclude().getMembers().isEmpty());
        assertEquals("", groupingGroupsMembers.getGroupingInclude().getResultCode());
        assertEquals("", groupingGroupsMembers.getGroupingInclude().getGroupPath());

        assertNotNull(groupingGroupsMembers.getGroupingExclude());
        assertNotNull(groupingGroupsMembers.getGroupingExclude().getMembers());
        assertTrue(groupingGroupsMembers.getGroupingExclude().getMembers().isEmpty());
        assertEquals("", groupingGroupsMembers.getGroupingExclude().getResultCode());
        assertEquals("", groupingGroupsMembers.getGroupingExclude().getGroupPath());

        assertNotNull(groupingGroupsMembers.getGroupingOwners());
        assertNotNull(groupingGroupsMembers.getGroupingOwners().getMembers());
        assertTrue(groupingGroupsMembers.getGroupingOwners().getMembers().isEmpty());
        assertEquals("", groupingGroupsMembers.getGroupingOwners().getResultCode());
        assertEquals("", groupingGroupsMembers.getGroupingOwners().getGroupPath());

        assertNotNull(groupingGroupsMembers.getAllMembers());
        assertNotNull(groupingGroupsMembers.getAllMembers().getMembers());
        assertTrue(groupingGroupsMembers.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void fallbackAllMembersCalculation() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults ws = JsonUtil.asObject(json, WsGetMembersResults.class);
        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));
        g.getCompositeGrouping().getMembers().clear();
        g = new GroupingGroupsMembers(new GetMembersResults(ws));
        assertNotNull(g.getAllMembers());
        assertFalse(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void setPaginationCompleteTrueTest() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        g.setPaginationCompleteTrue();
        assertTrue(g.isPaginationComplete());
    }

    @Test
    public void compositeGroupingEmptyPathBranch() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        GroupingGroupMembers result = g.getCompositeGrouping();

        assertNotNull(result);
        assertTrue(result.getMembers().isEmpty());
    }

    @Test
    public void determineWhereListedAllBranches() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults ws = JsonUtil.asObject(json, WsGetMembersResults.class);

        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));

        List<GroupingMember> members = g.getAllMembers().getMembers();

        assertTrue(members.stream().anyMatch(m -> m.getWhereListed().equals("Basis & Include")));
        assertTrue(members.stream().anyMatch(m -> m.getWhereListed().equals("Basis")));
        assertTrue(members.stream().anyMatch(m -> m.getWhereListed().equals("Include")));
    }

    @Test
    public void setAllMembersFallbackActuallyUsed() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults ws = JsonUtil.asObject(json, WsGetMembersResults.class);

        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));

        GroupingGroupMembers composite = g.getCompositeGrouping();

        assertTrue(composite.getMembers().isEmpty());
        assertFalse(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void setAllMembersFallbackBranch() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        assertNotNull(g.getAllMembers());
        assertTrue(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void forceCompositeBranchFullCoverage() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject subject = new Subject();
        subject.setUhUuid("u1");

        GroupingGroupMember m = new GroupingGroupMember(subject);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group-path");
        composite.getMembers().add(m);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group-path:basis");
        basis.getMembers().add(m);

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group-path:include");
        include.getMembers().add(m);

        List<GroupingGroupMembers> list = new ArrayList<>();
        list.add(composite);
        list.add(basis);
        list.add(include);

        Field f = GroupingGroupsMembers.class.getDeclaredField("groupsMembersList");
        f.setAccessible(true);
        f.set(g, list);

        Method mtd = GroupingGroupsMembers.class.getDeclaredMethod("setAllMembers");
        mtd.setAccessible(true);
        mtd.invoke(g);

        assertFalse(g.getAllMembers().getMembers().isEmpty());

        assertTrue(g.getAllMembers().getMembers().stream()
                .anyMatch(x -> x.getWhereListed().equals("Basis & Include")));
    }

    @Test
    public void determineWhereListedDefaultBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject subject = new Subject("u1", "u1", "person");
        GroupingGroupMember member = new GroupingGroupMember(subject);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group-path");
        composite.getMembers().add(member);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group-path:basis");

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group-path:include");

        List<GroupingGroupMembers> list = new ArrayList<>();
        list.add(composite);
        list.add(basis);
        list.add(include);

        Field field = GroupingGroupsMembers.class.getDeclaredField("groupsMembersList");
        field.setAccessible(true);
        field.set(g, list);

        Method method = GroupingGroupsMembers.class.getDeclaredMethod("setAllMembers");
        method.setAccessible(true);
        method.invoke(g);

        assertTrue(g.getAllMembers().getMembers().stream()
                .anyMatch(x -> x.getWhereListed().equals("Basis")));
    }

    @Test
    public void determineWhereListedIncludeOnlyBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject subject = new Subject("u1", "u1", "person");
        GroupingGroupMember member = new GroupingGroupMember(subject);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group-path");
        composite.getMembers().add(member);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group-path:basis");

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group-path:include");
        include.getMembers().add(member);

        List<GroupingGroupMembers> list = new ArrayList<>();
        list.add(composite);
        list.add(basis);
        list.add(include);

        Field field = GroupingGroupsMembers.class.getDeclaredField("groupsMembersList");
        field.setAccessible(true);
        field.set(g, list);

        Method method = GroupingGroupsMembers.class.getDeclaredMethod("setAllMembers");
        method.setAccessible(true);
        method.invoke(g);

        assertTrue(g.getAllMembers().getMembers().stream()
                .anyMatch(x -> x.getWhereListed().equals("Include")));
    }

    @Test
    public void determineWhereListedBasisOnlyBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject subject = new Subject("u1", "u1", "person");
        GroupingGroupMember member = new GroupingGroupMember(subject);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group-path");
        composite.getMembers().add(member);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group-path:basis");
        basis.getMembers().add(member);

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group-path:include");

        List<GroupingGroupMembers> list = new ArrayList<>();
        list.add(composite);
        list.add(basis);
        list.add(include);

        Field field = GroupingGroupsMembers.class.getDeclaredField("groupsMembersList");
        field.setAccessible(true);
        field.set(g, list);

        Method method = GroupingGroupsMembers.class.getDeclaredMethod("setAllMembers");
        method.setAccessible(true);
        method.invoke(g);

        assertTrue(g.getAllMembers().getMembers().stream()
                .anyMatch(x -> x.getWhereListed().equals("Basis")));
    }

}
