package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestHasMembersCommand {

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;
    @Test
    public void constructor() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand);
    }

    @Test
    public void builders() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand.addUhIdentifier(""));
        assertNotNull(hasMembersCommand.addUhIdentifiers(new ArrayList<>()));
        assertNotNull(hasMembersCommand.assignGroupPath(""));
    }

    @Test
    public void execute() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        /*
        assertThrows(RuntimeException.class, hasMembersCommand::execute);
         */
        hasMembersCommand.assignGroupPath(GROUPING_INCLUDE).addUhIdentifier(UH_NUMBERS.get(0)).execute().getResult();
        hasMembersCommand.assignGroupPath(GROUPING_INCLUDE).addUhIdentifier(UH_USERNAMES.get(0)).execute().getResult();
    }
}
