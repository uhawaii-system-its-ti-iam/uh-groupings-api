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

public class AddMembersCommandTest {
    @Test
    public void constructor() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        assertNotNull(addMembersCommand);
    }

    @Test
    public void builders() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(addMembersCommand.getGcAddMember());
        assertNotNull(addMembersCommand.addUhIdentifiers(strings));
        assertNotNull(addMembersCommand.addUhIdentifier(""));
        assertNotNull(addMembersCommand.addUhIdentifier("testiwta"));
        assertNotNull(addMembersCommand.assignGroupPath(""));
        assertNotNull(addMembersCommand.addOwnerGrouping("test-group-path"));
        assertNotNull(addMembersCommand.addOwnerGroupings(strings));
        assertNotNull(addMembersCommand.owner(""));
        assertNotNull(addMembersCommand.includeUhMemberDetails(true));
        assertNotNull(addMembersCommand.replaceGroupMembers(true));
        assertEquals(addMembersCommand.self(), addMembersCommand);
    }

    @Test
    public void addUhIdentifiersLooksEachUpInItsSubjectSource() {
        AddMembersCommand addMembersCommand = new AddMembersCommand()
                .addUhIdentifiers(List.of("12345678", "testiwta", "87654321"),
                        Map.of("12345678", "UH core LDAP", "testiwta", "other-source"));

        @SuppressWarnings("unchecked")
        List<WsSubjectLookup> lookups = (List<WsSubjectLookup>)
                ReflectionTestUtils.getField(addMembersCommand.getGcAddMember(), "subjectLookups");
        assertEquals(3, lookups.size());
        assertEquals("12345678", lookups.get(0).getSubjectId());
        assertEquals("UH core LDAP", lookups.get(0).getSubjectSourceId());
        assertEquals("testiwta", lookups.get(1).getSubjectIdentifier());
        assertEquals("other-source", lookups.get(1).getSubjectSourceId());
        // An identifier without a source is looked up in every source.
        assertEquals("87654321", lookups.get(2).getSubjectId());
        assertNull(lookups.get(2).getSubjectSourceId());
    }
}
