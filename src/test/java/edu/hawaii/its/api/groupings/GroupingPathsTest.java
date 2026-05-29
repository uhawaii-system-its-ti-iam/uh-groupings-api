package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingPath;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingPathsTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        GroupingPaths groupingPaths = new GroupingPaths(
                groupingsTestConfiguration.getAttributeAssignmentResultsSuccessTestData());
        assertNotNull(groupingPaths);
        assertEquals("SUCCESS", groupingPaths.getResultCode());
        List<GroupingPath> paths = groupingPaths.getGroupingPaths();
        assertNotNull(paths);
        assertEquals(1, paths.size());
    }
}