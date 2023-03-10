package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMembersCommand {

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;

    @Test
    public void constructorTest() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        assertNotNull(addMembersCommand);
    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new AddMembersCommand()::execute);
    }
}
