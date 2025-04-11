package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SubjectsCommandTest {
    @Test
    public void constructor() {
        SubjectsCommand subjectsCommand = new SubjectsCommand();
        assertNotNull(subjectsCommand);
    }

    @Test
    public void builders() {
        SubjectsCommand subjectsCommand = new SubjectsCommand();
        assertNotNull(subjectsCommand.addSubject(""));
        assertNotNull(subjectsCommand.addSubject("11111111"));
        assertNotNull(subjectsCommand.addSubjectAttribute(""));
        assertNotNull(subjectsCommand.assignSearchString(""));
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(subjectsCommand.addSubjects(strings));
        assertEquals(subjectsCommand.self(), subjectsCommand);
    }
}
