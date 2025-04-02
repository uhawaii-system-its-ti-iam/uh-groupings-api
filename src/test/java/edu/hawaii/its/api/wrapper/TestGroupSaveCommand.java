package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupSaveCommand {

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new GroupSaveCommand()::execute);
    }

    @Test
    public void setGroupingPath() {
        assertNotNull(new GroupSaveCommand().setGroupingPath(""));
    }

    @Test
    public void self() {
        GroupSaveCommand groupSaveCommand = new GroupSaveCommand();
        assertEquals(groupSaveCommand, groupSaveCommand.self());
    }
}
