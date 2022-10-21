package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMembersCommand  {


    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;
    public void constructorTest() {
        AddMembersCommand addMembersCommand = new AddMembersCommand(GROUPING_INCLUDE, UH_NUMBERS);
        assertNotNull(addMembersCommand);
    }

    @Test
    public void executeTest() {
        new AddMembersCommand(GROUPING_INCLUDE, UH_NUMBERS).execute();

        String[] bogus = { "bogus-1", "bogus-2" };
        try {
            new AddMembersCommand(GROUPING_INCLUDE, Arrays.asList(bogus)).execute();
            fail("A list of all invalid identifiers should throw an exception.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

        bogus = new String[] { "bogus-1", UH_NUMBERS.get(0), "bogus-2" };
        try {
            new AddMembersCommand(GROUPING_INCLUDE, Arrays.asList(bogus)).execute();
            fail("A list of invalid and valid identifiers should throw an exception.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

        try {
            new AddMembersCommand("bogus-path", UH_NUMBERS).execute();
            fail("Passing and invalid group path should throw an exception.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }
    }
}
