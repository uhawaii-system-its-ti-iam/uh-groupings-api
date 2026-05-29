package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
