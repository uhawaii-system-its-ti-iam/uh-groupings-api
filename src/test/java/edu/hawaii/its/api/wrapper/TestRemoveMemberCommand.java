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
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Test
    public void constructor() {
        RemoveMemberCommand removeMemberCommand = new RemoveMemberCommand(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(removeMemberCommand);
        removeMemberCommand = new RemoveMemberCommand(GROUPING_INCLUDE, "bogus-user");
        RemoveMemberResult removeMemberResult = removeMemberCommand.execute();
    }

    @Test
    public void executeTest() {
        RemoveMemberResult response;

        // Should not throw exception when invalid identifier is passed.
        try {
            // Remove uid.
            response = new RemoveMemberCommand(GROUPING_INCLUDE, "bogus-ident").execute();
            assertNotNull(response);
            assertEquals("", response.getUid());
            assertEquals(GROUPING_INCLUDE, response.getGroupPath());
            assertEquals("SUCCESS_WASNT_IMMEDIATE", response.getResultCode());

        } catch (RemoveMemberRequestRejectedException e) {
            fail("Should not throw exception when invalid identifier is passed.");
        }

        // Should throw an exception if an invalid group path is passed.
        String badPath = "bad-path";
        try {
            response = new RemoveMemberCommand(badPath, TEST_UH_NUMBERS.get(0)).execute();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (RemoveMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

    }
}
