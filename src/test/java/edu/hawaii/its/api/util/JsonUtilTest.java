package edu.hawaii.its.api.util;

import edu.hawaii.its.api.type.Person;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;

public class JsonUtilTest {

    private static Person person0;
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

    @BeforeAll
    public static void beforeAll() {
        person0 = new Person("name", "uhUuid", "username", "firstName", "lastName");
    }

    @BeforeEach
    public void beforeEach() {
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
    }

    @Test
    public void asJsonAsObject() {
        String personJson = JsonUtil.asJson(person0);

        Person person1 = JsonUtil.asObject(personJson, Person.class);

        assertEquals(person0.getName(), person1.getName());
        assertEquals(person0.getUhUuid(), person1.getUhUuid());
        assertEquals(person0.getUsername(), person1.getUsername());
        assertEquals(person0.getFirstName(), person1.getFirstName());
        assertEquals(person0.getLastName(), person1.getLastName());
        assertEquals(person0.getAttributes(), person1.getAttributes());
        assertEquals(person0.getClass(), person1.getClass());
        assertDoesNotThrow(() -> JsonUtil.asJson(mock(Object.class)));
        assertDoesNotThrow(() -> JsonUtil.asObject("", Object.class));
    }

    @Test
    public void prettyPrint() {
        JsonUtil.prettyPrint(person0);
        assertTrue(outStream.toString().contains("name"));
        assertDoesNotThrow(() -> JsonUtil.prettyPrint(mock(Object.class)));
    }

    @Test
    public void printJson() {
        JsonUtil.printJson(person0);
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

}
