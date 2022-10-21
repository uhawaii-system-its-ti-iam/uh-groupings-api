package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMemberCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;

    @Test
    public void constructorTest() {
        RemoveMemberCommand removeMemberCommand = new RemoveMemberCommand(GROUPING_INCLUDE, UH_NUMBERS.get(0));
        assertNotNull(removeMemberCommand);
    }

    @Test
    public void executeTest() {
        RemoveMemberResult response = new RemoveMemberCommand(GROUPING_INCLUDE, "bogus-ident").execute();
        assertNotNull(response);
        assertEquals("", response.getUid());
        assertEquals(GROUPING_INCLUDE, response.getGroupPath());
        assertEquals("SUCCESS_WASNT_IMMEDIATE", response.getResultCode());

        // Should throw an exception if an invalid group path is passed.
        try {
            new RemoveMemberCommand("bad-path", UH_NUMBERS.get(0)).execute();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (RemoveMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }
    }
}
