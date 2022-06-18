package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class HelperServiceTest {

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID_KEY;

    @Value("${groupings.api.person_attributes.username}")
    private String UID_KEY;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME_KEY;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME_KEY;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME_KEY;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = ":include";
    private static final String EXCLUDE = ":exclude";
    private static final String OWNERS = ":owners";
    private static final String BASIS = ":basis";
    private static final String BASIS_PLUS_INCLUDE = ":basis+include";

    private static final String GROUPING_INCLUDE = PATH_ROOT + INCLUDE;
    private static final String GROUPING_EXCLUDE = PATH_ROOT + EXCLUDE;
    private static final String GROUPING_BASIS = PATH_ROOT + BASIS;
    private static final String GROUPING_OWNERS = PATH_ROOT + OWNERS;
    private static final String GROUPING_BASIS_PLUS_INCLUDE = PATH_ROOT + BASIS_PLUS_INCLUDE;

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;

    @Autowired
    private HelperService helperService;

    @Test
    public void construction() {
        //autowired
        assertNotNull(helperService);
    }

    @Test
    public void isUhUuid() {
        assertTrue(helperService.isUhUuid("111111"));
        assertFalse(helperService.isUhUuid("111-111"));
        assertFalse(helperService.isUhUuid("iamtst01"));
        assertFalse(helperService.isUhUuid(null));
    }

    @Test
    public void extractFirstMembershipIDTest() {
        WsGetMembershipsResults membershipsResults = null;
        String firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("", firstMembershipId);

        membershipsResults = new WsGetMembershipsResults();
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("", firstMembershipId);

        WsMembership[] memberships = null;
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("", firstMembershipId);

        memberships = new WsMembership[] { null };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("", firstMembershipId);

        WsMembership membership = new WsMembership();
        memberships = new WsMembership[] { membership };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("", firstMembershipId);

        membership.setMembershipId("1234");
        memberships = new WsMembership[] { membership };
        membershipsResults.setWsMemberships(memberships);
        firstMembershipId = helperService.extractFirstMembershipID(membershipsResults);
        assertEquals("1234", firstMembershipId);
    }

    @Test
    public void makeGroupingsServiceResultTest() {
        String resultCode = "resultCode";
        String action = "action";
        GroupingsServiceResult groupingsServiceResult = helperService.makeGroupingsServiceResult(resultCode, action);
        assertNotNull(groupingsServiceResult);
        assertEquals(resultCode, groupingsServiceResult.getResultCode());
        assertEquals(action, groupingsServiceResult.getAction());
    }

    @Test
    public void makePathsTest() {
        List<String> strPaths = new ArrayList<>();
        assertEquals(0, helperService.makePaths(strPaths).size());
        String[] testPaths = { INCLUDE, EXCLUDE, OWNERS, BASIS };
        for (String testPath : testPaths) {
            strPaths.add(PATH_ROOT + testPath);
        }

        List<GroupingPath> groupingPaths = helperService.makePaths(strPaths);
        assertTrue(groupingPaths.size() > 0);
        Iterator<String> stringIterator = strPaths.iterator();
        Iterator<GroupingPath> groupingPathIterator = groupingPaths.iterator();
        while (groupingPathIterator.hasNext() && stringIterator.hasNext()) {
            assertEquals(stringIterator.next(), groupingPathIterator.next().getPath());
        }
    }

    @Test
    public void parentGroupingPathTest() {
        List<String> groupPaths = new ArrayList<>();
        groupPaths.add(GROUPING_OWNERS);
        groupPaths.add(GROUPING_BASIS);
        groupPaths.add(GROUPING_INCLUDE);
        groupPaths.add(GROUPING_EXCLUDE);
        groupPaths.add(GROUPING_BASIS_PLUS_INCLUDE);
        groupPaths.forEach(groupPath -> {
            assertEquals(PATH_ROOT, helperService.parentGroupingPath(groupPath));
        });
        assertEquals("", helperService.parentGroupingPath(null));
    }

    @Test
    public void nameGroupingPathTest() {
        assertEquals("grouping-test-path", helperService.nameGroupingPath("test:grouping-test-path:include"));
        assertEquals("", helperService.nameGroupingPath(""));
    }

    @Test
    public void makeGroupsTest() {
        WsGetMembersResults getMembersResults = new WsGetMembersResults();
        String[] attributeNames =
                new String[] { UID_KEY, UHUUID_KEY, LAST_NAME_KEY, COMPOSITE_NAME_KEY, FIRST_NAME_KEY };

        // We create an array here because getMembersResults.setResults() only takes an array
        WsGetMembersResult[] getMembersResult = new WsGetMembersResult[1];
        WsGetMembersResult subGetMembersResult = new WsGetMembersResult();

        WsGroup wsGroup = new WsGroup();
        wsGroup.setName(GROUPING_0_PATH);

        WsSubject[] list = new WsSubject[3];
        for (int i = 0; i < 3; i++) {
            list[i] = new WsSubject();
            list[i].setName("testSubject_" + i);
            list[i].setId("testSubject_uuid_" + i);
            // Attribute values need to match names in order (uuid is set seperately, so it can be blank here
            list[i].setAttributeValues(new String[] { "testSubject_username_" + i, "", "", "testSubject_" + i, "" });
        }

        subGetMembersResult.setWsSubjects(list);
        subGetMembersResult.setWsGroup(wsGroup);
        getMembersResult[0] = subGetMembersResult;

        getMembersResults.setResults(getMembersResult);
        getMembersResults.setSubjectAttributeNames(attributeNames);

        Map<String, Group> groups = helperService.makeGroups(getMembersResults);

        assertFalse(groups.isEmpty());
        Group resultGroup = groups.get(GROUPING_0_PATH);

        for (int i = 0; i < resultGroup.getMembers().size(); i++) {
            assertEquals("testSubject_" + i, resultGroup.getMembers().get(i).getName());
            assertTrue(resultGroup.getNames().contains("testSubject_" + i));
            assertEquals("testSubject_uuid_" + i, resultGroup.getMembers().get(i).getUhUuid());
            assertTrue(resultGroup.getUhUuids().contains("testSubject_uuid_" + i));
            assertEquals("testSubject_username_" + i, resultGroup.getMembers().get(i).getUsername());
            assertTrue(resultGroup.getUsernames().contains("testSubject_username_" + i));
        }
    }

    @Test
    public void makePersonTest() {
        String name = "name";
        String id = "uuid";
        String identifier = "username";
        String[] attributeNames =
                new String[] { UID_KEY, UHUUID_KEY, LAST_NAME_KEY, COMPOSITE_NAME_KEY, FIRST_NAME_KEY };
        String[] attributeValues = new String[] { identifier, id, null, name, null };

        WsSubject subject = new WsSubject();
        subject.setName(name);
        subject.setId(id);
        subject.setAttributeValues(attributeValues);

        Person person = helperService.makePerson(subject, attributeNames);

        assertEquals(name, person.getName());
        assertEquals(id, person.getUhUuid());
        assertEquals(identifier, person.getUsername());

        assertNotNull(helperService.makePerson(new WsSubject(), new String[] {}));
    }

    @Test
    public void extractGroupPaths() {
        List<String> groupNames = helperService.extractGroupPaths(null);
        assertTrue(groupNames.isEmpty());

        List<WsGroup> groups = new ArrayList<>();
        final int size = 300;

        for (int i = 0; i < size; i++) {
            WsGroup w = new WsGroup();
            w.setName("testName_" + i);
            groups.add(w);
        }
        assertEquals(size, groups.size());

        groupNames = helperService.extractGroupPaths(groups);
        for (int i = 0; i < size; i++) {
            assertTrue(groupNames.contains("testName_" + i));
        }
        assertEquals(size, groupNames.size());

        // Create some duplicates.
        groups = new ArrayList<>();
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < size; i++) {
                WsGroup w = new WsGroup();
                w.setName("testName_" + i);
                groups.add(w);
            }
        }
        assertEquals(size * 3, groups.size());

        // Duplicates should not be in groupNames list.
        groupNames = helperService.extractGroupPaths(groups);
        assertEquals(size, groupNames.size());
        for (int i = 0; i < size; i++) {
            assertTrue(groupNames.contains("testName_" + i));
        }
    }
}
