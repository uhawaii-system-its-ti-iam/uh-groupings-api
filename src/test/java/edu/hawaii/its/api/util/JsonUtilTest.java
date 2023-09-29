package edu.hawaii.its.api.util;

import edu.hawaii.its.api.type.Person;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonUtilTest {

    @Test
    public void basics() {
        Person person0 = new Person("name", "uhUuid", "username", "firstName", "lastName");
        String personJson = JsonUtil.asJson(person0);

        Person person1 = JsonUtil.asObject(personJson, Person.class);

        assertEquals(person0.getName(), person1.getName());
        assertEquals(person0.getUhUuid(), person1.getUhUuid());
        assertEquals(person0.getUsername(), person1.getUsername());
        assertEquals(person0.getFirstName(), person1.getFirstName());
        assertEquals(person0.getLastName(), person1.getLastName());
        assertEquals(person0.getAttributes(), person1.getAttributes());
        assertEquals(person0.getClass(), person1.getClass());
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