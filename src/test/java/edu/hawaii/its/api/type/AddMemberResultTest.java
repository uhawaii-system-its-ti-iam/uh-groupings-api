package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AddMemberResultTest {
    private AddMemberResult addMemberResultInstantiatedOnConstruction;
    private AddMemberResult addMemberResultEmptyOnConstruction;
    boolean userWasAdded = true;
    boolean userWasRemoved = true;
    private final String pathOfAdd = "pathOfAdd";
    private final String pathOfRemoved = "pathOfRemoved";
    private final String name = "name";
    private final String uhUuid = " uhUuid";
    private final String uid = "uid";
    private final String result = "result";
    private final String userIdentifier = "userIdentifier";

    @Before
    public void setUp() {
        addMemberResultInstantiatedOnConstruction =
                new AddMemberResult(userWasAdded, userWasRemoved, pathOfAdd, pathOfRemoved, name, uhUuid, uid, result,
                        userIdentifier);
        addMemberResultEmptyOnConstruction = new AddMemberResult();
    }

    @Test
    public void construction() {
        assertNotNull(addMemberResultInstantiatedOnConstruction);
        assertNotNull(addMemberResultEmptyOnConstruction);
        AddMemberResult addMemberResult = new AddMemberResult(result, userIdentifier);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getResult());
        assertNotNull(addMemberResult.getUserIdentifier());
        assertNull(addMemberResult.getPathOfAdd());
        assertNull(addMemberResult.getPathOfRemoved());
        assertNull(addMemberResult.getName());
        assertNull(addMemberResult.getUhUuid());
        assertFalse(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());
    }

    @Test
    public void isUserWasAddedTest() {
        assertTrue(addMemberResultInstantiatedOnConstruction.isUserWasAdded());
    }

    @Test
    public void isUserWasRemovedTest() {
        assertTrue(addMemberResultInstantiatedOnConstruction.isUserWasRemoved());
    }

    @Test
    public void getPathOfAddTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getPathOfAdd());
        assertEquals(pathOfAdd, addMemberResultInstantiatedOnConstruction.getPathOfAdd());
    }

    @Test
    public void setPathOfAddTest() {
        assertNull(addMemberResultEmptyOnConstruction.getPathOfAdd());
        addMemberResultEmptyOnConstruction.setPathOfAdd(pathOfAdd);
        assertEquals(pathOfAdd, addMemberResultEmptyOnConstruction.getPathOfAdd());
    }

    @Test
    public void getPathOfRemovedTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getPathOfRemoved());
        assertEquals(pathOfRemoved, addMemberResultInstantiatedOnConstruction.getPathOfRemoved());
    }

    @Test
    public void setPathOfRemovedTest() {
        assertNull(addMemberResultEmptyOnConstruction.getPathOfRemoved());
        addMemberResultEmptyOnConstruction.setPathOfRemoved(pathOfRemoved);
        assertEquals(pathOfRemoved, addMemberResultEmptyOnConstruction.getPathOfRemoved());
    }

    @Test
    public void getNameTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getName());
        assertEquals(name, addMemberResultInstantiatedOnConstruction.getName());
    }

    @Test
    public void setNameTest() {
        assertNull(addMemberResultEmptyOnConstruction.getName());
        addMemberResultEmptyOnConstruction.setName(name);
        assertEquals(name, addMemberResultEmptyOnConstruction.getName());
    }

    @Test
    public void getUhUuidTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getUhUuid());
        assertEquals(uhUuid, addMemberResultInstantiatedOnConstruction.getUhUuid());
    }

    @Test
    public void setUhUuidTest() {
        assertNull(addMemberResultEmptyOnConstruction.getUhUuid());
        addMemberResultEmptyOnConstruction.setUhUuid(uhUuid);
        assertEquals(uhUuid, addMemberResultEmptyOnConstruction.getUhUuid());
    }

    @Test
    public void getUidTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getUid());
        assertEquals(uid, addMemberResultInstantiatedOnConstruction.getUid());
    }

    @Test
    public void setUidTest() {
        assertNull(addMemberResultEmptyOnConstruction.getUid());
        addMemberResultEmptyOnConstruction.setUid(uid);
        assertEquals(uid, addMemberResultEmptyOnConstruction.getUid());
    }

    @Test
    public void getResultTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getResult());
        assertEquals(result, addMemberResultInstantiatedOnConstruction.getResult());
    }

    @Test
    public void setResultTest() {
        assertNull(addMemberResultEmptyOnConstruction.getResult());
        addMemberResultEmptyOnConstruction.setResult(result);
        assertEquals(result, addMemberResultEmptyOnConstruction.getResult());
    }

    @Test
    public void setUserIdentifierTest() {
        assertNotNull(addMemberResultInstantiatedOnConstruction.getUserIdentifier());
        assertEquals(userIdentifier, addMemberResultInstantiatedOnConstruction.getUserIdentifier());
    }

    public void getUserIdentifierTest() {
        assertNull(addMemberResultEmptyOnConstruction.getUserIdentifier());
        addMemberResultEmptyOnConstruction.setUserIdentifier(userIdentifier);
        assertEquals(userIdentifier, addMemberResultEmptyOnConstruction.getUserIdentifier());
    }
}
