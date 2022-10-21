package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestHasMembersCommand {
    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Test void Constructor() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand(GROUPING_INCLUDE, TEST_UH_NUMBERS);
        assertNotNull(hasMembersCommand);
    }

    @Test
    public void executeTest() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand(GROUPING_INCLUDE, TEST_UH_NUMBERS);
        HasMembersResults hasMembersResults = hasMembersCommand.execute();
        JsonUtil.printJson(hasMembersResults);
        assertNotNull(hasMembersCommand);

        hasMembersCommand = new HasMembersCommand(GROUPING_INCLUDE, TEST_UH_USERNAMES);
        hasMembersResults = hasMembersCommand.execute();
        JsonUtil.printJson(hasMembersResults);
        assertNotNull(hasMembersCommand);

    }
}
