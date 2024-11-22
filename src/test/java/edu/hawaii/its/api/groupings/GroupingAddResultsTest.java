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

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingAddResultsTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        GroupingAddResults groupingAddResults = new GroupingAddResults(
                groupingsTestConfiguration.addMemberResultsSuccessTestData());
        assertNotNull(groupingAddResults);
        List<GroupingAddResult> results = groupingAddResults.getResults();
        assertNotNull(results);
        assertEquals("SUCCESS", groupingAddResults.getResultCode());
    }

}
