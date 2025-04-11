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
public class TestSubjectsCommand {

    @Test
    public void execute() {
        SubjectsCommand subjectsCommand = new SubjectsCommand();
        assertNotNull(subjectsCommand.assignGroupingPath(""));
        assertThrows(RuntimeException.class, subjectsCommand::execute);
    }

    @Test
    public void self() {
        SubjectsCommand subjectsCommand = new SubjectsCommand();
        assertEquals(subjectsCommand, subjectsCommand.self());
    }
}