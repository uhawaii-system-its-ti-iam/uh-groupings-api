package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestHasMembersCommand {

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new HasMembersCommand()::execute);
    }

    @Test
    public void self() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertEquals(hasMembersCommand, hasMembersCommand.self());
    }
}
