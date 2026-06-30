package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupsResultsTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        GroupsResults groupsResults = groupingsTestConfiguration.groupsResultsSuccessTestData();
        assertNotNull(groupsResults);

        groupsResults = new GroupsResults(null);
        assertNotNull(groupsResults);
    }

    @Test
    public void groupPathsTest() {
        GroupsResults groupsResults = groupingsTestConfiguration.groupsResultsSuccessTestData();
        List<String> groupPaths = groupsResults.groupPaths();
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertFalse(groupPaths.isEmpty());

        groupsResults = new GroupsResults(null);
        groupPaths = groupsResults.groupPaths();
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        groupsResults = groupingsTestConfiguration.groupsResultsEmptyResultsTestData();
        groupPaths = groupsResults.groupPaths();
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());

        groupsResults = groupingsTestConfiguration.groupsResultsEmptyGroupsTestData();
        groupPaths = groupsResults.groupPaths();
        assertNotNull(groupsResults);
        assertNotNull(groupPaths);
        assertTrue(groupPaths.isEmpty());
    }
}
