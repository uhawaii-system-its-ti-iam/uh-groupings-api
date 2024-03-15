package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ManageSubjectResultTest {

    private ManageSubjectResult manageSubjectResultEmptyOnConstruction;

    @BeforeEach
    public void setUp() {
        manageSubjectResultEmptyOnConstruction = new ManageSubjectResult();
    }

    @Test
    public void construction() {
        assertNotNull(manageSubjectResultEmptyOnConstruction);
    }

    @Test
    public void getNameTest() {
        assertNull(manageSubjectResultEmptyOnConstruction.getName());
        String name = "name";
        manageSubjectResultEmptyOnConstruction.setName(name);
        assertEquals(name, manageSubjectResultEmptyOnConstruction.getName());
    }

    @Test
    public void getPathTest() {
        assertNull(manageSubjectResultEmptyOnConstruction.getPath());
        String path = "path";
        manageSubjectResultEmptyOnConstruction.setPath(path);
        assertEquals(path, manageSubjectResultEmptyOnConstruction.getPath());
    }

    @Test
    public void inIncludeTest() {
        assertFalse(manageSubjectResultEmptyOnConstruction.isInInclude());
        manageSubjectResultEmptyOnConstruction.setInInclude(true);
        assertTrue(manageSubjectResultEmptyOnConstruction.isInInclude());
    }

    @Test
    public void inExcludeTest() {
        assertFalse(manageSubjectResultEmptyOnConstruction.isInExclude());
        manageSubjectResultEmptyOnConstruction.setInExclude(true);
        assertTrue(manageSubjectResultEmptyOnConstruction.isInExclude());
    }

    @Test
    public void inOwnerTest() {
        assertFalse(manageSubjectResultEmptyOnConstruction.isInOwner());
        manageSubjectResultEmptyOnConstruction.setInOwner(true);
        assertTrue(manageSubjectResultEmptyOnConstruction.isInOwner());
    }

    @Test
    public void inBasisAndIncludeTest() {
        assertFalse(manageSubjectResultEmptyOnConstruction.isInBasisAndInclude());
        manageSubjectResultEmptyOnConstruction.setInBasisAndInclude(true);
        assertTrue(manageSubjectResultEmptyOnConstruction.isInBasisAndInclude());
    }

    @Test
    public void toStringTest() {
        ManageSubjectResult manageSubjectResult = new ManageSubjectResult();
        assertEquals("Membership{" +
                        "path='null'" +
                        ", name='null'" +
                        ", inInclude=false" +
                        ", inExclude=false" +
                        ", inOwner=false" +
                        ", inBasisAndInclude=false" +
                        '}'
                , manageSubjectResult.toString());
    }
}