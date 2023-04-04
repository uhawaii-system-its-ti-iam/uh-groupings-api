package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void execute() {
        assertThrows(RuntimeException.class, new HasMembersCommand()::execute);
    }
}
