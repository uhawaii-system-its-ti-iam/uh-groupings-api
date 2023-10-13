package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MembershipTest {
    private Membership membershipInstantiatedOnConstruction;
    private Membership membershipEmptyOnConstruction;
    private final Person person = new Person();
    private final Group group = new Group();

    @BeforeEach
    public void setUp() {
        membershipEmptyOnConstruction = new Membership();
        membershipInstantiatedOnConstruction = new Membership(person, group);
    }

    @Test
    public void construction() {
        assertNotNull(membershipInstantiatedOnConstruction);
        assertNotNull(membershipEmptyOnConstruction);
        assertNotNull(membershipInstantiatedOnConstruction.getPerson());
        assertNotNull(membershipInstantiatedOnConstruction.getGroup());
    }

    @Test
    public void personTest() {
        membershipEmptyOnConstruction.setPerson(person);
        assertEquals(person, membershipEmptyOnConstruction.getPerson());
    }

    @Test
    public void groupTest() {
        membershipEmptyOnConstruction.setGroup(group);
        assertEquals(group, membershipEmptyOnConstruction.getGroup());
    }

    @Test
    public void identifierTest() {
        assertNull(membershipEmptyOnConstruction.getIdentifier());
        String identifier = "identifier";
        membershipEmptyOnConstruction.setIdentifier(identifier);
        assertEquals(identifier, membershipEmptyOnConstruction.getIdentifier());
    }

    @Test
    public void getNameTest() {
        assertNull(membershipEmptyOnConstruction.getName());
        String name = "name";
        membershipEmptyOnConstruction.setName(name);
        assertEquals(name, membershipEmptyOnConstruction.getName());
    }

    @Test
    public void getPathTest() {
        assertNull(membershipEmptyOnConstruction.getPath());
        String path = "path";
        membershipEmptyOnConstruction.setPath(path);
        assertEquals(path, membershipEmptyOnConstruction.getPath());
    }

    @Test
    public void getDescriptionTest() {
        assertNull(membershipEmptyOnConstruction.getDescription());
        String description = "description";
        membershipEmptyOnConstruction.setDescription(description);
        assertEquals(description, membershipEmptyOnConstruction.getDescription());
    }

    @Test
    public void inBasisTest() {
        assertFalse(membershipEmptyOnConstruction.isInBasis());
        membershipEmptyOnConstruction.setInBasis(true);
        assertTrue(membershipEmptyOnConstruction.isInBasis());
    }

    @Test
    public void inOwnerTest() {
        assertFalse(membershipEmptyOnConstruction.isInOwner());
        membershipEmptyOnConstruction.setInOwner(true);
        assertTrue(membershipEmptyOnConstruction.isInOwner());
    }

    @Test
    public void inIncludeTest() {
        assertFalse(membershipEmptyOnConstruction.isInInclude());
        membershipEmptyOnConstruction.setInInclude(true);
        assertTrue(membershipEmptyOnConstruction.isInInclude());
    }

    @Test
    public void inBasisAndIncludeTest() {
        assertFalse(membershipEmptyOnConstruction.isInBasisAndInclude());
        membershipEmptyOnConstruction.setInBasisAndInclude(true);
        assertTrue(membershipEmptyOnConstruction.isInBasisAndInclude());
    }

    @Test
    public void inExcludeTest() {
        assertFalse(membershipEmptyOnConstruction.isInExclude());
        membershipEmptyOnConstruction.setInExclude(true);
        assertTrue(membershipEmptyOnConstruction.isInExclude());
    }

    @Test
    public void selfOptedTest() {
        assertFalse(membershipEmptyOnConstruction.isSelfOpted());
        membershipEmptyOnConstruction.setSelfOpted(true);
        assertTrue(membershipEmptyOnConstruction.isSelfOpted());
    }

    @Test
    public void optInEnabledTest() {
        assertFalse(membershipEmptyOnConstruction.isOptInEnabled());
        membershipEmptyOnConstruction.setOptInEnabled(true);
        assertTrue(membershipEmptyOnConstruction.isOptInEnabled());
    }

    @Test
    public void optOutEnabledTest() {
        assertFalse(membershipEmptyOnConstruction.isOptOutEnabled());
        membershipEmptyOnConstruction.setOptOutEnabled(true);
        assertTrue(membershipEmptyOnConstruction.isOptOutEnabled());
    }

    @Test
    public void toStringTest() {
        Membership membership = new Membership();
        assertEquals("Membership{" +
                "identifier='null'" +
                ", person=null" +
                ", group=null" +
                ", path='null'" +
                ", name='null'" +
                ", description='null'" +
                ", isSelfOpted=false" +
                ", isOptInEnabled=false" +
                ", isOptOutEnabled=false" +
                ", inBasis=false" +
                ", inInclude=false" +
                ", inExclude=false" +
                ", inOwner=false" +
                ", inBasisAndInclude=false" +
                '}'
        , membership.toString());
    }
}
