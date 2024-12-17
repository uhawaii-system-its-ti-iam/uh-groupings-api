package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.wrapper.GetMembersResults;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
class GroupingAssignmentServiceTest {

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String GROUPING_0_PATH = PATH_ROOT + 0;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        assertNotNull(groupingAssignmentService);
        assertNotNull(grouperService);
    }

    //TODO: finish unit test in GROUPINGS-1540
    @Test
    public void makeGroupsTest() {
        GetMembersResults getMembersResults = groupingsTestConfiguration.getMembersResultsSuccessTestData();
        Map<String, Group> groups = groupingAssignmentService.makeGroups(getMembersResults);
        assertFalse(groups.isEmpty());
        Group resultGroup = groups.get(GROUPING_0_PATH);
    }

}
