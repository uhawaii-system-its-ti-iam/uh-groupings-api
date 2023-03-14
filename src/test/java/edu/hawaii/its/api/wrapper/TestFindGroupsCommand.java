package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestFindGroupsCommand {

    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Test
    public void constructor() {
        FindGroupsCommand findGroupsCommand = new FindGroupsCommand();
        assertNotNull(findGroupsCommand);
    }

    @Test
    public void execute() {
        assertNotNull(new FindGroupsCommand()
                .addPath(GROUPING)
                .execute());
        assertNotNull(new FindGroupsCommand()
                .addPath("bad-path")
                .execute());
    }
}
