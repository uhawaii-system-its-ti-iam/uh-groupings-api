package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(subjectsCommand.addSubjects(strings));
    }
}
