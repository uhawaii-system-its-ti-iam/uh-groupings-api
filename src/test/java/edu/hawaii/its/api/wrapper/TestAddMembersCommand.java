package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMembersCommand {
    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new AddMembersCommand()::execute);
    }

    @Test
    public void self() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        assertEquals(addMembersCommand, addMembersCommand.self());
    }
}
