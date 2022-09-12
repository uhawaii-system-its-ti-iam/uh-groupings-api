package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class GroupingAssignmentServiceTest {

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
    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_INCLUDE = PATH_ROOT + GroupType.INCLUDE.value();
    private static final String GROUPING_EXCLUDE = PATH_ROOT + GroupType.EXCLUDE.value();
    private static final String GROUPING_BASIS = PATH_ROOT + GroupType.BASIS.value();
    private static final String GROUPING_OWNERS = PATH_ROOT + GroupType.OWNERS.value();

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @MockBean
    private GrouperApiService grouperApiService;

    @Test
    public void construction() {
        assertNotNull(groupingAssignmentService);
        assertNotNull(grouperApiService);
    }

    @Test
    public void parentGroupingPathTest() {
        List<String> groupPaths = new ArrayList<>();
        groupPaths.add(GROUPING_OWNERS);
        groupPaths.add(GROUPING_BASIS);
        groupPaths.add(GROUPING_INCLUDE);
        groupPaths.add(GROUPING_EXCLUDE);
        groupPaths.forEach(groupPath -> {
            assertEquals(PATH_ROOT, groupingAssignmentService.parentGroupingPath(groupPath));
        });
        assertEquals("", groupingAssignmentService.parentGroupingPath(null));
    }

    @Test
    public void nameGroupingPathTest() {
        assertEquals("grouping-test-path", groupingAssignmentService.nameGroupingPath("test:grouping-test-path:include"));
        assertEquals("", groupingAssignmentService.nameGroupingPath(""));
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

        Map<String, Group> groups = groupingAssignmentService.makeGroups(getMembersResults);

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

        Person person = groupingAssignmentService.makePerson(subject, attributeNames);

        assertEquals(name, person.getName());
        assertEquals(id, person.getUhUuid());
        assertEquals(identifier, person.getUsername());

        assertNotNull(groupingAssignmentService.makePerson(new WsSubject(), new String[] {}));
    }

    @Test
    public void makePaths() {
        assertThat(groupingAssignmentService.makePaths(null), hasSize(0));

        List<String> names = new ArrayList<>();
        assertThat(groupingAssignmentService.makePaths(names), hasSize(0));

        names = Arrays.asList(
                GroupType.INCLUDE.value(), GroupType.EXCLUDE.value(), GroupType.OWNERS.value(), GroupType.BASIS.value());
        List<GroupingPath> paths = groupingAssignmentService.makePaths(names);
        assertThat(paths, hasSize(4));

        List<String> expectedPaths = names.stream()
                .map(n -> PATH_ROOT + n)
                .collect(Collectors.toList());

        List<GroupingPath> groupingPaths = groupingAssignmentService.makePaths(expectedPaths);
        assertThat(paths, hasSize(4));

        for (int i = 0; i < expectedPaths.size(); i++) {
            assertThat(groupingPaths.get(i).getPath(),
                    equalTo(expectedPaths.get(i)));
        }
    }
}
