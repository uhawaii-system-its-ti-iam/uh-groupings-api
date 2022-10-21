package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMemberCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;

    @Test public void constructorTest() {
        AddMemberCommand addMemberCommand = new AddMemberCommand(GROUPING_INCLUDE, UH_NUMBERS.get(0));
        assertNotNull(addMemberCommand);
    }

    @Test
    public void executeTest() {

        // Should throw an exception if an invalid uh identifier is passed.
        try {
            new AddMemberCommand(GROUPING_INCLUDE, "bogus-ident").execute();
            fail("Should throw an exception if an invalid uh identifier is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

        // Should throw an exception if an invalid group path is passed.
        try {
            new AddMemberCommand("bad-path", UH_NUMBERS.get(0)).execute();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

    }
}
