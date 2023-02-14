package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMembersCommand {


    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;
    @Test
    public void constructorTest() {
        AddMembersCommand addMembersCommand = new AddMembersCommand(GROUPING_INCLUDE, UH_NUMBERS);
        assertNotNull(addMembersCommand);

        try {
            new AddMembersCommand(null, UH_NUMBERS);
        }catch (NullPointerException e) {
            assertEquals("groupPath cannot be null", e.getMessage());
        }

        try {
            new AddMembersCommand(GROUPING_INCLUDE, null);
        }catch (NullPointerException e) {
            assertEquals("uhIdentifiers cannot be null", e.getMessage());
        }

        String[] array = { "uid", "uid", null, "uid" };
        List<String> listWithNull = new ArrayList<>(Arrays.asList(array));

        try {
            new AddMembersCommand(GROUPING_INCLUDE, listWithNull);
        }catch (NullPointerException e) {
            assertEquals("uhIdentifier cannot be null", e.getMessage());
        }
    }
}
