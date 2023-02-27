package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.service.GroupingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupSaveCommand {
    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Autowired
    private GroupingsService groupingsService;

    @Test
    public void constructor() {
        GroupSaveCommand groupSaveCommand = new GroupSaveCommand();
        assertNotNull(groupSaveCommand);
    }

    @Test
    public void execute() {
        String description = groupingsService.getGroupingDescription(GROUPING);
        GroupSaveResults groupSaveResults = new GroupSaveCommand()
                .setGroupingPath(GROUPING)
                .setDescription("description")
                .execute();
        assertNotNull(groupSaveResults);

        // Set description back.
        new GroupSaveCommand()
                .setGroupingPath(GROUPING)
                .setDescription(description)
                .execute();

        try {
            new GroupSaveCommand()
                    .setGroupingPath("bad-path")
                    .setDescription("")
                    .execute();
            fail("should throw and exception if an invalid path is passed");
        } catch (InvalidGroupPathException e) {
            assertTrue(e.getMessage().contains("404"));
        }
    }
}
