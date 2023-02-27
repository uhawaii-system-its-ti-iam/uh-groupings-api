package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestFindGroupsCommand {

    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Test
    public void constructor() {
        FindGroupsCommand findGroupsCommand = new FindGroupsCommand(GROUPING);
        assertNotNull(findGroupsCommand);
    }

    @Test
    public  void execute() {
        assertNotNull(new FindGroupsCommand(GROUPING).execute());
        assertNotNull(new FindGroupsCommand("bad-path").execute());
    }
}
