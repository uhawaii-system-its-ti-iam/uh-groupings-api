package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GetGroupsResultsTest {
    final static private String SUCCESS = "SUCCESS";

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        assertNotNull(new GetGroupsResults(null));
        GetGroupsResults getGroupsResults =
                groupingsTestConfiguration.getGroupsResultsSuccessTestData();
        assertNotNull(getGroupsResults);
    }

    @Test
    public void successfulResults() {
        GetGroupsResults getGroupsResults =
                groupingsTestConfiguration.getGroupsResultsSuccessTestData();
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertEquals(2, getGroupsResults.getGroups().size());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals(SUCCESS, getGroupsResults.getResultCode());
    }

    @Test
    public void emptyGroups() {
        GetGroupsResults getGroupsResults =
                groupingsTestConfiguration.getGroupsResultsEmptyGroupsTestData();
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals("FAILURE", getGroupsResults.getResultCode());
    }

    @Test
    public void emptyResults() {
        GetGroupsResults getGroupsResults =
                groupingsTestConfiguration.getGroupsResultsEmptyResultsTestData();
        assertNotNull(getGroupsResults);
        assertNotNull(getGroupsResults.getGroups());
        assertNotNull(getGroupsResults.getSubject());
        assertEquals("FAILURE", getGroupsResults.getResultCode());
    }

    @Test
    public void groupsBySubjectName() {
        GetGroupsResults getGroupsResults =
                groupingsTestConfiguration.getGroupsResultsGroupsOfGroupsTestData();
        Map<String, List<Group>> groupsBySubjectName = getGroupsResults.getGroupsBySubjectName();

        // The result without a subject is skipped.
        assertEquals(3, groupsBySubjectName.size());

        assertEquals(List.of("grouping-1:owners", "grouping-2:include"),
                groupsBySubjectName.get("owner-grouping-path").stream().map(Group::getGroupPath).toList());
        assertEquals(List.of("grouping-3:include"),
                groupsBySubjectName.get("plain-grouping-path").stream().map(Group::getGroupPath).toList());
        assertTrue(groupsBySubjectName.get("orphan-grouping-path").isEmpty());
    }

    @Test
    public void groupsBySubjectNameWhenResultsAreEmpty() {
        assertTrue(new GetGroupsResults(null).getGroupsBySubjectName().isEmpty());
        assertTrue(groupingsTestConfiguration.getGroupsResultsEmptyResultsTestData()
                .getGroupsBySubjectName().isEmpty());
    }
}
