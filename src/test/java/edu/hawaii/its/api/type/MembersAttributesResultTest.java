package edu.hawaii.its.api.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MembersAttributesResultTest {
    private MembersAttributesResult membersAttributesResult;
    private List<Person> membersAttributes;
    private final String result = "result";

    @BeforeEach
    public void setup() {
        membersAttributesResult = new MembersAttributesResult();
        Person person = new Person("name", "uhUuid", "uid");
        membersAttributes = new ArrayList<>();
        membersAttributes.add(person);
        membersAttributesResult.setMembersAttributes(membersAttributes);
        membersAttributesResult.setResult(result);
    }

    @Test
    public void constructors() {
        MembersAttributesResult mar = new MembersAttributesResult();
        assertNotNull(mar);
        mar = new MembersAttributesResult(membersAttributes, result);
        assertNotNull(mar);
    }

    @Test
    public void accessors() {
        assertEquals(membersAttributes, membersAttributesResult.getMembersAttributes());
        assertEquals(result, membersAttributesResult.getResult());
    }
}
