package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RemoveMemberResultTest {
    private RemoveMemberResult removeMemberResultInstantiatedOnConstruction;
    private RemoveMemberResult removeMemberResultEmptyOnConstruction;

    private final String pathOfRemoved = "pathOfRemoved";
    private final String name = "name";
    private final String uhUuid = " uhUuid";
    private final String uid = "uid";
    private final String result = "result";
    private final String userIdentifier = "userIdentifier";

    @BeforeEach
    public void setUp() {
        removeMemberResultInstantiatedOnConstruction =
                new RemoveMemberResult(true, pathOfRemoved, name, uhUuid, uid, result, userIdentifier);
        removeMemberResultEmptyOnConstruction = new RemoveMemberResult();
    }

    @Test
    public void construction() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction);
        assertNotNull(removeMemberResultEmptyOnConstruction);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(result, userIdentifier);
        assertNotNull(removeMemberResult);
        assertNotNull(removeMemberResult.getResult());
        assertNotNull(removeMemberResult.getUserIdentifier());
        assertNull(removeMemberResult.getPathOfRemoved());
        assertNull(removeMemberResult.getName());
        assertNull(removeMemberResult.getUhUuid());
        assertFalse(removeMemberResult.isUserWasRemoved());
    }

    @Test
    public void isUserWasRemoveedTest() {
        assertTrue(removeMemberResultInstantiatedOnConstruction.isUserWasRemoved());
    }

    @Test
    public void isUserWasRemovedTest() {
        assertTrue(removeMemberResultInstantiatedOnConstruction.isUserWasRemoved());
    }

    @Test
    public void getPathOfRemoveTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getPathOfRemoved());
        assertEquals(pathOfRemoved, removeMemberResultInstantiatedOnConstruction.getPathOfRemoved());
    }

    @Test
    public void setPathOfRemoveTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getPathOfRemoved());
        removeMemberResultEmptyOnConstruction.setPathOfRemoved(pathOfRemoved);
        assertEquals(pathOfRemoved, removeMemberResultEmptyOnConstruction.getPathOfRemoved());
    }

    @Test
    public void getPathOfRemovedTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getPathOfRemoved());
        assertEquals(pathOfRemoved, removeMemberResultInstantiatedOnConstruction.getPathOfRemoved());
    }

    @Test
    public void setPathOfRemovedTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getPathOfRemoved());
        removeMemberResultEmptyOnConstruction.setPathOfRemoved(pathOfRemoved);
        assertEquals(pathOfRemoved, removeMemberResultEmptyOnConstruction.getPathOfRemoved());
    }

    @Test
    public void getNameTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getName());
        assertEquals(name, removeMemberResultInstantiatedOnConstruction.getName());
    }

    @Test
    public void setNameTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getName());
        removeMemberResultEmptyOnConstruction.setName(name);
        assertEquals(name, removeMemberResultEmptyOnConstruction.getName());
    }

    @Test
    public void getUhUuidTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getUhUuid());
        assertEquals(uhUuid, removeMemberResultInstantiatedOnConstruction.getUhUuid());
    }

    @Test
    public void setUhUuidTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getUhUuid());
        removeMemberResultEmptyOnConstruction.setUhUuid(uhUuid);
        assertEquals(uhUuid, removeMemberResultEmptyOnConstruction.getUhUuid());
    }

    @Test
    public void getUidTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getUid());
        assertEquals(uid, removeMemberResultInstantiatedOnConstruction.getUid());
    }

    @Test
    public void setUidTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getUid());
        removeMemberResultEmptyOnConstruction.setUid(uid);
        assertEquals(uid, removeMemberResultEmptyOnConstruction.getUid());
    }

    @Test
    public void getResultTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getResult());
        assertEquals(result, removeMemberResultInstantiatedOnConstruction.getResult());
    }

    @Test
    public void setResultTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getResult());
        removeMemberResultEmptyOnConstruction.setResult(result);
        assertEquals(result, removeMemberResultEmptyOnConstruction.getResult());
    }

    @Test
    public void setUserIdentifierTest() {
        assertNotNull(removeMemberResultInstantiatedOnConstruction.getUserIdentifier());
        assertEquals(userIdentifier, removeMemberResultInstantiatedOnConstruction.getUserIdentifier());
    }

    @Test
    public void getUserIdentifierTest() {
        assertNull(removeMemberResultEmptyOnConstruction.getUserIdentifier());
        removeMemberResultEmptyOnConstruction.setUserIdentifier(userIdentifier);
        assertEquals(userIdentifier, removeMemberResultEmptyOnConstruction.getUserIdentifier());
    }

    @Test
    public void toStringTest() {
        assertEquals("RemoveMemberResult{" +
                "userWasRemoved=" + true +
                ", pathOfRemoved='" + pathOfRemoved + '\'' +
                ", name='" + name + '\'' +
                ", uhUuid='" + uhUuid + '\'' +
                ", uid='" + uid + '\'' +
                ", result='" + result + '\'' +
                ", userIdentifier='" + userIdentifier + '\'' +
                '}', removeMemberResultInstantiatedOnConstruction.toString());
    }
}
