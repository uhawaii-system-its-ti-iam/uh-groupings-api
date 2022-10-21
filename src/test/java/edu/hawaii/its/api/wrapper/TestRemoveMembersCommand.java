package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMembersCommand {

    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;
    @Test public void constructorTest() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand(GROUPING_INCLUDE, UH_NUMBERS);
        assertNotNull(removeMembersCommand);
    }

    @Test
    public void executeTest() {
        RemoveMembersResults removeMembersResults =
                new RemoveMembersCommand(GROUPING_INCLUDE, UH_NUMBERS).execute();
        assertNotNull(removeMembersResults);

        String[] bogus = { "bogus-1", "bogus-2" };
        removeMembersResults = new RemoveMembersCommand(GROUPING_INCLUDE, Arrays.asList(bogus)).execute();
    }
}
