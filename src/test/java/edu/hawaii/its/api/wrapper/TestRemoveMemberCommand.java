package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMemberCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Test
    public void constructorTest() {
        RemoveMemberCommand removeMemberCommand = new RemoveMemberCommand(GROUPING_INCLUDE, UH_NUMBERS.get(0));
        assertNotNull(removeMemberCommand);

        try {
            new RemoveMemberCommand(null, UH_NUMBERS.get(0));
        } catch (NullPointerException e) {
            assertEquals("groupPath cannot be null", e.getMessage());
        }

        try {
            new RemoveMemberCommand(GROUPING_INCLUDE, null);
        } catch (NullPointerException e) {
            assertEquals("uhIdentifier cannot be null", e.getMessage());
        }
    }
}
