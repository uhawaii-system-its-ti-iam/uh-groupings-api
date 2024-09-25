package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MembershipResultTest {
    private MembershipResult membershipResultEmptyOnConstruction;
    private MembershipResult membershipResultHydratedOnConstruction;

    @BeforeEach
    public void setUp() {
        membershipResultEmptyOnConstruction = new MembershipResult();
        membershipResultHydratedOnConstruction = new MembershipResult("path", "name", "description");
    }

    @Test
    public void construction() {
        assertNotNull(membershipResultEmptyOnConstruction);
        assertNull(membershipResultEmptyOnConstruction.getName());
        assertNull(membershipResultEmptyOnConstruction.getPath());
        assertNull(membershipResultEmptyOnConstruction.getDescription());
        assertNotNull(membershipResultHydratedOnConstruction);
        String name = "name";
        String path = "path";
        String description = "description";
        assertNotNull(membershipResultHydratedOnConstruction.getName());
        assertEquals(name, membershipResultHydratedOnConstruction.getName());
        assertNotNull(membershipResultHydratedOnConstruction.getPath());
        assertEquals(path, membershipResultHydratedOnConstruction.getPath());
        assertNotNull(membershipResultHydratedOnConstruction.getDescription());
        assertEquals(description, membershipResultHydratedOnConstruction.getDescription());
    }

    @Test
    public void getNameTest() {
        assertNull(membershipResultEmptyOnConstruction.getName());
        String name = "name";
        membershipResultEmptyOnConstruction.setName(name);
        assertEquals(name, membershipResultEmptyOnConstruction.getName());
    }

    @Test
    public void getPathTest() {
        assertNull(membershipResultEmptyOnConstruction.getPath());
        String path = "path";
        membershipResultEmptyOnConstruction.setPath(path);
        assertEquals(path, membershipResultEmptyOnConstruction.getPath());
    }

    @Test
    public void getDescriptionTest() {
        assertNull(membershipResultEmptyOnConstruction.getDescription());
        String description = "description";
        membershipResultEmptyOnConstruction.setDescription(description);
        assertEquals(description, membershipResultEmptyOnConstruction.getDescription());
    }

    @Test
    public void optOutEnabledTest() {
        assertFalse(membershipResultEmptyOnConstruction.isOptOutEnabled());
        membershipResultEmptyOnConstruction.setOptOutEnabled(true);
        assertTrue(membershipResultEmptyOnConstruction.isOptOutEnabled());
    }

    @Test
    public void toStringTest() {
        MembershipResult membershipResult = new MembershipResult();
        assertEquals("Membership{" +
                "path='null'" +
                ", name='null'" +
                ", description='null'" +
                ", isOptOutEnabled=false" +
                '}'
        , membershipResult.toString());
    }
}
