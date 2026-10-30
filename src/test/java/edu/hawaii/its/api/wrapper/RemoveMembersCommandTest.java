package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

public class RemoveMembersCommandTest {
    @Test
    public void constructor() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand();
        assertNotNull(removeMembersCommand);
    }

    @Test
    public void builders() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand();
        assertNotNull(removeMembersCommand.addUhIdentifier(""));
        assertNotNull(removeMembersCommand.addUhIdentifier("testiwta"));
        assertNotNull(removeMembersCommand.assignGroupPath(""));
        assertNotNull(removeMembersCommand.includeUhMemberDetails(true));
        assertNotNull(removeMembersCommand.owner(""));
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(removeMembersCommand.addUhIdentifiers(strings));
        assertEquals(removeMembersCommand.self(), removeMembersCommand);
    }

    @Test
    public void addUhIdentifiersLooksEachUpInItsSubjectSource() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand()
                .addUhIdentifiers(List.of("12345678", "87654321"), Map.of("12345678", "UH core LDAP"));

        Object gcDeleteMember = ReflectionTestUtils.getField(removeMembersCommand, "gcDeleteMember");
        @SuppressWarnings("unchecked")
        List<WsSubjectLookup> lookups =
                (List<WsSubjectLookup>) ReflectionTestUtils.getField(gcDeleteMember, "subjectLookups");
        assertEquals(2, lookups.size());
        assertEquals("12345678", lookups.get(0).getSubjectId());
        assertEquals("UH core LDAP", lookups.get(0).getSubjectSourceId());
        // An identifier without a source is looked up in every source.
        assertEquals("87654321", lookups.get(1).getSubjectId());
        assertNull(lookups.get(1).getSubjectSourceId());
    }
}
