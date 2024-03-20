package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ManagePersonResultTest {

    private ManagePersonResult managePersonResultEmptyOnConstruction;

    @BeforeEach
    public void setUp() {
        managePersonResultEmptyOnConstruction = new ManagePersonResult();
    }

    @Test
    public void construction() {
        assertNotNull(managePersonResultEmptyOnConstruction);
    }

    @Test
    public void getNameTest() {
        assertNull(managePersonResultEmptyOnConstruction.getName());
        String name = "name";
        managePersonResultEmptyOnConstruction.setName(name);
        assertEquals(name, managePersonResultEmptyOnConstruction.getName());
    }

    @Test
    public void getPathTest() {
        assertNull(managePersonResultEmptyOnConstruction.getPath());
        String path = "path";
        managePersonResultEmptyOnConstruction.setPath(path);
        assertEquals(path, managePersonResultEmptyOnConstruction.getPath());
    }

    @Test
    public void inIncludeTest() {
        assertFalse(managePersonResultEmptyOnConstruction.isInInclude());
        managePersonResultEmptyOnConstruction.setInInclude(true);
        assertTrue(managePersonResultEmptyOnConstruction.isInInclude());
    }

    @Test
    public void inExcludeTest() {
        assertFalse(managePersonResultEmptyOnConstruction.isInExclude());
        managePersonResultEmptyOnConstruction.setInExclude(true);
        assertTrue(managePersonResultEmptyOnConstruction.isInExclude());
    }

    @Test
    public void inOwnerTest() {
        assertFalse(managePersonResultEmptyOnConstruction.isInOwner());
        managePersonResultEmptyOnConstruction.setInOwner(true);
        assertTrue(managePersonResultEmptyOnConstruction.isInOwner());
    }

    @Test
    public void inBasisAndIncludeTest() {
        assertFalse(managePersonResultEmptyOnConstruction.isInBasisAndInclude());
        managePersonResultEmptyOnConstruction.setInBasisAndInclude(true);
        assertTrue(managePersonResultEmptyOnConstruction.isInBasisAndInclude());
    }

    @Test
    public void toStringTest() {
        ManagePersonResult managePersonResult = new ManagePersonResult();
        assertEquals("Membership{" +
                        "path='null'" +
                        ", name='null'" +
                        ", inInclude=false" +
                        ", inExclude=false" +
                        ", inOwner=false" +
                        ", inBasisAndInclude=false" +
                        '}'
                , managePersonResult.toString());
    }
}