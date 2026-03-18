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
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        assertEquals("", g.getGroupPath());
        assertEquals("", g.getResultCode());
        assertNotNull(g.getAllMembers());
        assertTrue(g.getAllMembers().getMembers().isEmpty());

        assertFalse(g.isBasis());
        assertFalse(g.isInclude());
        assertFalse(g.isExclude());
        assertFalse(g.isOwners());

        assertEquals(0, g.getPageNumber());
        assertTrue(g.isPaginationComplete());
    }

    @Test
    public void emptyResult() {
        WsGetMembersResults ws = new WsGetMembersResults();
        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));

        assertEquals("FAILURE", g.getResultCode());
        assertTrue(g.getAllMembers().getMembers().isEmpty());

        assertNotNull(g.getGroupingBasis());
        assertNotNull(g.getGroupingInclude());
        assertNotNull(g.getGroupingExclude());
        assertNotNull(g.getGroupingOwners());
    }

    @Test
    public void successfulResultBasic() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults ws = JsonUtil.asObject(json, WsGetMembersResults.class);

        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));

        assertEquals("SUCCESS", g.getResultCode());
        assertFalse(g.getAllMembers().getMembers().isEmpty());

        assertTrue(g.isBasis());
        assertTrue(g.isInclude());
        assertTrue(g.isExclude());
        assertTrue(g.isOwners());
    }

    @Test
    public void compositeNeverNull() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        assertNotNull(g.getCompositeGrouping());
    }

    @Test
    public void fallbackWhenCompositeEmpty() throws Exception {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults ws = JsonUtil.asObject(json, WsGetMembersResults.class);

        GroupingGroupsMembers g = new GroupingGroupsMembers(new GetMembersResults(ws));
        g.getCompositeGrouping().getMembers().clear();

        invokeSetAllMembers(g);

        assertFalse(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void fallbackWhenNoData() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        invokeSetAllMembers(g);

        assertTrue(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void compositeBasisInclude() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject("u1", "name", "1001");
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group");
        composite.getMembers().add(member);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group:basis");
        basis.getMembers().add(member);

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group:include");
        include.getMembers().add(member);

        setGroups(g, List.of(composite, basis, include));

        invokeSetAllMembers(g);

        assertEquals("Basis & Include",
                g.getAllMembers().getMembers().get(0).getWhereListed());
    }

    @Test
    public void compositeBasisOnly() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject("u1", "name", "1001");
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group");
        composite.getMembers().add(member);

        GroupingGroupMembers basis = new GroupingGroupMembers();
        basis.setGroupPath("group:basis");
        basis.getMembers().add(member);

        setGroups(g, List.of(composite, basis));

        invokeSetAllMembers(g);

        assertEquals("Basis",
                g.getAllMembers().getMembers().get(0).getWhereListed());
    }

    @Test
    public void compositeIncludeOnly() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject("u1", "name", "1001");
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group");
        composite.getMembers().add(member);

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group:include");
        include.getMembers().add(member);

        setGroups(g, List.of(composite, include));

        invokeSetAllMembers(g);

        assertEquals("Include",
                g.getAllMembers().getMembers().get(0).getWhereListed());
    }

    @Test
    public void compositeUnknownBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject("u1", "name", "1001");
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group");
        composite.getMembers().add(member);

        setGroups(g, List.of(composite));

        invokeSetAllMembers(g);

        assertEquals("Unknown",
                g.getAllMembers().getMembers().get(0).getWhereListed());
    }

    @Test
    public void nullUuidBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject(null, null, null);
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers composite = new GroupingGroupMembers();
        composite.setGroupPath("group");
        composite.getMembers().add(member);

        setGroups(g, List.of(composite));

        invokeSetAllMembers(g);

        assertEquals("Unknown",
                g.getAllMembers().getMembers().get(0).getWhereListed());
    }

    @Test
    public void fallbackExcludeWins() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();

        Subject s = new Subject("u1", "name", "1001");
        GroupingGroupMember member = new GroupingGroupMember(s);

        GroupingGroupMembers include = new GroupingGroupMembers();
        include.setGroupPath("group:include");
        include.getMembers().add(member);

        GroupingGroupMembers exclude = new GroupingGroupMembers();
        exclude.setGroupPath("group:exclude");
        exclude.getMembers().add(member);

        setGroups(g, List.of(include, exclude));

        invokeSetAllMembers(g);

        assertTrue(g.getAllMembers().getMembers().isEmpty());
    }

    @Test
    public void paginationCompleteFalse() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        g.setBasis(true);
        g.setPaginationComplete();

        assertFalse(g.isPaginationComplete());
    }

    @Test
    public void setPaginationCompleteTrue() {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        g.setPaginationCompleteTrue();

        assertTrue(g.isPaginationComplete());
    }

    private void setGroups(GroupingGroupsMembers g, List<GroupingGroupMembers> list) throws Exception {
        Field f = GroupingGroupsMembers.class.getDeclaredField("groupsMembersList");
        f.setAccessible(true);
        f.set(g, list);
    }

    private void invokeSetAllMembers(GroupingGroupsMembers g) throws Exception {
        Method m = GroupingGroupsMembers.class.getDeclaredMethod("setAllMembers");
        m.setAccessible(true);
        m.invoke(g);
    }

    @Test
    public void determineWhereListedUuidNullBranch() throws Exception {
        GroupingGroupsMembers g = new GroupingGroupsMembers();
        Subject s = new Subject(null, null, null);
        GroupingGroupMember member = new GroupingGroupMember(s);

        Method method = GroupingGroupsMembers.class.getDeclaredMethod(
                "determineWhereListed",
                GroupingGroupMember.class,
                java.util.Set.class,
                java.util.Set.class
        );
        method.setAccessible(true);

        String result = (String) method.invoke(
                g,
                member,
                java.util.Collections.emptySet(),
                java.util.Collections.emptySet()
        );

        assertEquals("Unknown", result);
    }
}