package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

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
        assertNotNull(subjectsCommand.addSubject("testiwta"));
        assertNotNull(subjectsCommand.addSubjectAttribute(""));
        assertNotNull(subjectsCommand.assignSearchString(""));
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(subjectsCommand.addSubjects(strings));
        assertEquals(subjectsCommand.self(), subjectsCommand);
    }

    @Test
    public void addSubjectsLooksUpInTheGivenSubjectSourceOnly() {
        SubjectsCommand subjectsCommand = new SubjectsCommand()
                .addSubjects(List.of("12345678", "testiwta"), "UH core LDAP")
                .addSubjects(List.of("87654321"));

        List<WsSubjectLookup> lookups = new ArrayList<>(lookups(subjectsCommand));
        assertEquals(3, lookups.size());
        assertEquals("12345678", lookups.get(0).getSubjectId());
        assertEquals("UH core LDAP", lookups.get(0).getSubjectSourceId());
        assertEquals("testiwta", lookups.get(1).getSubjectIdentifier());
        assertEquals("UH core LDAP", lookups.get(1).getSubjectSourceId());
        // Without a source, Grouper looks in every source.
        assertEquals("87654321", lookups.get(2).getSubjectId());
        assertNull(lookups.get(2).getSubjectSourceId());
    }

    @SuppressWarnings("unchecked")
    private Collection<WsSubjectLookup> lookups(SubjectsCommand subjectsCommand) {
        Object gcGetSubjects = ReflectionTestUtils.getField(subjectsCommand, "gcGetSubjects");
        return (Collection<WsSubjectLookup>) ReflectionTestUtils.getField(gcGetSubjects, "wsSubjectLookups");
    }
}
