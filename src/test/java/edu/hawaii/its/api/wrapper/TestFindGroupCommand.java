package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestFindGroupCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_basis}")
    protected String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_many_owners}")
    protected String GROUPING_OWNERS;

    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Test
    public void constructor() {
        FindGroupCommand findGroupCommand = new FindGroupCommand(GROUPING);
        assertNotNull(findGroupCommand);
        findGroupCommand = new FindGroupCommand(null);
        assertNotNull(findGroupCommand);
    }

    @Test
    public void execute() {
        FindGroupCommand findGroupCommand = new FindGroupCommand(GROUPING);
        assertNotNull(findGroupCommand);
        FindGroupResult findGroupResult = findGroupCommand.execute();
        assertNotNull(findGroupResult);

        findGroupCommand = new FindGroupCommand(GROUPING_INCLUDE);
        assertNotNull(findGroupCommand);
        findGroupResult = findGroupCommand.execute();
        assertNotNull(findGroupResult);
        assertEquals("", findGroupResult.getDescription());

        findGroupCommand = new FindGroupCommand(GROUPING_BASIS);
        assertNotNull(findGroupCommand);
        findGroupResult = findGroupCommand.execute();
        assertNotNull(findGroupResult);
        assertEquals("", findGroupResult.getDescription());

        findGroupCommand = new FindGroupCommand("invalid-group-path");
        assertNotNull(findGroupCommand);
        findGroupResult = findGroupCommand.execute();
        assertNotNull(findGroupResult);
    }
}
