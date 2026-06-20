package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingGroupMembersTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void testDefaultConstructor() {
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers();
        assertNotNull(groupingGroupMembers);
        assertEquals("", groupingGroupMembers.getResultCode());
        assertEquals("", groupingGroupMembers.getGroupPath());
        assertEquals(new ArrayList<>(), groupingGroupMembers.getMembers());
        assertEquals(0, groupingGroupMembers.getSize());
    }

    @Test
    public void testConstructorWithGetMembersResults() {
        GetMembersResults getMembersResults =
                groupingsTestConfiguration.getMembersResultsSuccessTestData();
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResults.getMembersResults()
                .get(0));
        assertNotNull(groupingGroupMembers);
        assertEquals("grouping-path:include", groupingGroupMembers.getGroupPath());
        assertEquals("SUCCESS", groupingGroupMembers.getResultCode());
        List<GroupingGroupMember> results = groupingGroupMembers.getMembers();
        assertNotNull(results);
        assertEquals(groupingGroupMembers.getSize(), results.size());
    }

    @Test
    public void testConstructorWithSubjectsResults() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(subjectsResults);
        assertNotNull(groupingGroupMembers);
        assertEquals("SUCCESS", groupingGroupMembers.getResultCode());
        List<GroupingGroupMember> results = groupingGroupMembers.getMembers();
        assertNotNull(results);
        assertEquals(groupingGroupMembers.getSize(), results.size());
    }

    @Test
    public void testSort() {
        GetMembersResults getMembersResults =
                groupingsTestConfiguration.getMembersResultsSuccessTestData();
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResults.getMembersResults()
                .get(0));
        assertNotNull(groupingGroupMembers);
        List<GroupingGroupMember> members = groupingGroupMembers.getMembers();
        GroupingGroupMember firstMember = members.get(0);
        GroupingGroupMember secondMember = members.get(1);

        groupingGroupMembers = groupingGroupMembers.sort("name", false);
        assertEquals(firstMember.getName(), groupingGroupMembers.getMembers().get(1).getName());
        assertEquals(secondMember.getName(), groupingGroupMembers.getMembers().get(0).getName());

        groupingGroupMembers = groupingGroupMembers.sort("search_string0", true);
        assertEquals(firstMember.getUid(), groupingGroupMembers.getMembers().get(0).getUid());
        assertEquals(secondMember.getUid(), groupingGroupMembers.getMembers().get(1).getUid());

        groupingGroupMembers = groupingGroupMembers.sort("subjectId", false);
        assertEquals(firstMember.getUhUuid(), groupingGroupMembers.getMembers().get(1).getUhUuid());
        assertEquals(secondMember.getUhUuid(), groupingGroupMembers.getMembers().get(0).getUhUuid());
    }

    @Test
    public void testPaginate() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(subjectsResults);
        assertNotNull(groupingGroupMembers);
        List<Subject> subjects = subjectsResults.getSubjects();
        for (int i = 0; i < 3; i++) {
            List<Subject> duplicateSubjects = new ArrayList<>(subjects);
            subjects.addAll(duplicateSubjects);
        }
        groupingGroupMembers.setMembers(subjects);

        assertEquals(20, groupingGroupMembers.paginate(1, 20).getMembers().size());
        assertEquals(12, groupingGroupMembers.paginate(2, 20).getMembers().size());
        assertEquals(0, groupingGroupMembers.paginate(3, 20).getMembers().size());
        assertEquals(0, groupingGroupMembers.paginate(10, 20).getMembers().size());
        assertThrows(IndexOutOfBoundsException.class, () -> groupingGroupMembers.paginate(-1, 20));
        assertThrows(IndexOutOfBoundsException.class, () -> groupingGroupMembers.paginate(0, 20));
    }
}
