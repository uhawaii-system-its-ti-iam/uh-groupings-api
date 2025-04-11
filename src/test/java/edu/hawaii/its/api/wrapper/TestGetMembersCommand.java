package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGetMembersCommand {

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new GetMembersCommand()::execute);
    }

    @Test
    public void self() {
        GetMembersCommand getMembersCommand = new GetMembersCommand();
        assertEquals(getMembersCommand, getMembersCommand.self());
    }
}
