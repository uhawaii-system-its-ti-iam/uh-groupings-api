package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        try {
            new HasMembersCommand(null, TEST_UH_NUMBERS );
        }catch (NullPointerException e) {
            assertEquals("groupPath cannot be null", e.getMessage());
        }

        try {
            new HasMembersCommand(GROUPING_INCLUDE, null);
        }catch (NullPointerException e) {
            assertEquals("uhIdentifiers cannot be null", e.getMessage());
        }

        String[] array = { "uid", "uid", null, "uid" };
        List<String> listWithNull = new ArrayList<>(Arrays.asList(array));

        try {
            new HasMembersCommand(GROUPING_INCLUDE, listWithNull);
        }catch (NullPointerException e) {
            assertEquals("uhIdentifier cannot be null", e.getMessage());
        }
    }
}
