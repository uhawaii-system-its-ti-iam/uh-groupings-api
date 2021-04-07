package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MembershipTest {
    private Membership membershipInstantiatedOnConstruction;
    private Membership membershipEmptyOnConstruction;
    private final Person person = new Person();
    private final Group group = new Group();

    @Before
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

}
