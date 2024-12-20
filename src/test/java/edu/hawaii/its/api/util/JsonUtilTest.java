package edu.hawaii.its.api.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.wrapper.Subject;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class JsonUtilTest {

    private static PropertyLocator propertyLocator;
    private static Subject subject0;
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
        String json = propertyLocator.find("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject0 = new Subject(wsSubject);
    }

    @BeforeEach
    public void beforeEach() {
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
    }

    @Test
    public void asJsonAsObject() {
        String subjectJson = JsonUtil.asJson(subject0);
        Subject subject1 = JsonUtil.asObject(subjectJson, Subject.class);
        assertEquals(subject0.getName(), subject1.getName());
        assertEquals(subject0.getUhUuid(), subject1.getUhUuid());
        assertEquals(subject0.getUid(), subject1.getUid());
        assertEquals(subject0.getFirstName(), subject1.getFirstName());
        assertEquals(subject0.getLastName(), subject1.getLastName());
        assertEquals(subject0.getClass(), subject1.getClass());
        assertDoesNotThrow(() -> JsonUtil.asJson(mock(Object.class)));
        assertDoesNotThrow(() -> JsonUtil.asObject("", Object.class));
    }

    @Test
    public void prettyPrint() {
        JsonUtil.prettyPrint(subject0);
        assertTrue(outStream.toString().contains("name"));
        assertDoesNotThrow(() -> JsonUtil.prettyPrint(mock(Object.class)));
    }

    @Test
    public void printJson() {
        JsonUtil.printJson(subject0);
        assertFalse(errStream.toString().trim().isEmpty());
        assertDoesNotThrow(() -> JsonUtil.printJson(mock(Object.class)));
    }

    @Test
    public void problems() {
        String json = JsonUtil.asJson(null);
        assertEquals(json, "null");

        json = JsonUtil.asJson("{}");
        assertEquals(json, "\"{}\"");

        json = JsonUtil.asJson("mistake");
        assertEquals(json, "\"mistake\"");
    }

    @Test
    public void constructorIsPrivate() throws Exception {
        Constructor<JsonUtil> constructor = JsonUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void asObjectFromFile() {
        TestUser user = JsonUtil.asObjectFromFile("test.json", TestUser.class);
        assertNotNull(user);
        assertTrue(user.getTest().equals("value"));
        assertEquals(123, user.getNumber());

        assertNull(JsonUtil.asObjectFromFile("non-existent.json", TestUser.class));
    }

    @Test
    public void asListFromFile() {
        List<OotbActiveProfile> users =
                JsonUtil.asListFromFile("ootb.active.user.profiles.json", OotbActiveProfile.class);
        assertNotNull(users);

        assertNull(JsonUtil.asListFromFile("non-existent.json", TestUser.class));
    }

    // Test class matching test.json structure
    private static class TestUser {
        private String test;
        private int number;

        public String getTest() {
            return test;
        }

        public int getNumber() {
            return number;
        }
    }
}
