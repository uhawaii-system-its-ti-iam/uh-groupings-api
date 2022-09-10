package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GrouperApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMembersCommand {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void executeTest() {
        RemoveMembersResults removeMembersResults =
                new RemoveMembersCommand(GROUPING_INCLUDE, TEST_UH_NUMBERS).execute();
        assertNotNull(removeMembersResults);

        String[] bogus = { "bogus-1", "bogus-2" };
        removeMembersResults = new RemoveMembersCommand(GROUPING_INCLUDE, Arrays.asList(bogus)).execute();
    }
}
