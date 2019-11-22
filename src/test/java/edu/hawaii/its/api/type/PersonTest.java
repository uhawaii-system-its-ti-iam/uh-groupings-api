package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PersonTest {

    private Person person;
    private Person person2;

    @Before
    public void setUp() {
        person = new Person();
    }

    @Test
    public void constuction() {
        assertNotNull(person);
        assertNull(person.getName());
        assertNull(person.getUhUuid());
        assertNull(person.getUsername());

        person = new Person("a", "b", "c");
        assertThat(person.getName(), equalTo("a"));
        assertThat(person.getUhUuid(), equalTo("b"));
        assertThat(person.getUsername(), equalTo("c"));

        person2 = new Person("a", "b", "c", "d", "e");
        assertThat(person2.getName(), equalTo("a"));
        assertThat(person2.getUhUuid(), equalTo("b"));
        assertThat(person2.getUsername(), equalTo("c"));
        assertThat(person2.getFirstName(), equalTo("d"));
        assertThat(person2.getLastName(), equalTo("e"));
    }

    @Test
    public void accessors() {
        assertNull(person.getName());
        assertNull(person.getUhUuid());
        assertNull(person.getUsername());

        person.setName("name");
        assertThat(person.getName(), equalTo("name"));
        assertNull(person.getUhUuid());
        assertNull(person.getUsername());

        person.setUhUuid("uuid");
        assertThat(person.getName(), equalTo("name"));
        assertThat(person.getUhUuid(), equalTo("uuid"));
        assertNull(person.getUsername());

        person.setUsername("username");
        assertThat(person.getName(), equalTo("name"));
        assertThat(person.getUhUuid(), equalTo("uuid"));
        assertThat(person.getUsername(), equalTo("username"));
    }

    @Test
    public void equals() {
        Person p0 = new Person();
        assertFalse(p0.equals(null));
        assertFalse(p0.equals(new String()));
        assertTrue(p0.equals(p0));

        Person p1 = new Person();
        assertTrue(p0.equals(p1));
        assertTrue(p1.equals(p0));

        p0.setName("name");
        assertFalse(p0.equals(p1));
        assertFalse(p1.equals(p0));
        p1.setName("name");
        assertTrue(p0.equals(p1));
        assertTrue(p1.equals(p0));

        p0.setUhUuid("uuid");
        assertFalse(p0.equals(p1));
        assertFalse(p1.equals(p0));
        p1.setUhUuid("uuid");
        assertTrue(p0.equals(p1));
        assertTrue(p1.equals(p0));

        p0.setUsername("username");
        assertFalse(p0.equals(p1));
        assertFalse(p1.equals(p0));
        p1.setUsername("username");
        assertTrue(p0.equals(p1));
        assertTrue(p1.equals(p0));
    }

    @Test
    public void testHashCode() {
        assertThat(person.getName(), equalTo(null));
        assertThat(person.getUhUuid(), equalTo(null));
        assertThat(person.getUsername(), equalTo(null));

        final int prime = 31;
        int hashCode = person.hashCode();
        assertTrue(hashCode > 31);

        int result = 1;
        result = prime * result + "name".hashCode();
        result = prime * result + 0;
        result = prime * result + 0;
        person.setName("name");
        assertThat(person.hashCode(), equalTo(result));

        result = 1;
        result = prime * result + 0;
        result = prime * result + "username".hashCode();
        result = prime * result + 0;
        person.setName(null);
        person.setUsername("username");
        person.setUhUuid(null);
        assertThat(person.hashCode(), equalTo(result));

        result = 1;
        result = prime * result + 0;
        result = prime * result + 0;
        result = prime * result + "uuid".hashCode();
        person.setName(null);
        person.setUsername(null);
        person.setUhUuid("uuid");
        assertThat(person.hashCode(), equalTo(result));

    }

    @Test
    public void compareTo() {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("d"));
        persons.add(new Person("c"));
        persons.add(new Person("b"));
        persons.add(new Person("a"));

        assertThat(persons.get(0).getName(), equalTo("d"));
        assertThat(persons.get(1).getName(), equalTo("c"));
        assertThat(persons.get(2).getName(), equalTo("b"));
        assertThat(persons.get(3).getName(), equalTo("a"));

        Collections.sort(persons);

        assertThat(persons.get(0).getName(), equalTo("a"));
        assertThat(persons.get(1).getName(), equalTo("b"));
        assertThat(persons.get(2).getName(), equalTo("c"));
        assertThat(persons.get(3).getName(), equalTo("d"));

        // Again.
        persons = new ArrayList<>();
        persons.add(new Person("", "p", ""));
        persons.add(new Person("", "o", ""));
        persons.add(new Person("", "n", ""));
        persons.add(new Person("", "m", ""));

        assertThat(persons.get(0).getName(), equalTo(""));
        assertThat(persons.get(1).getName(), equalTo(""));
        assertThat(persons.get(2).getName(), equalTo(""));
        assertThat(persons.get(3).getName(), equalTo(""));
        assertThat(persons.get(0).getUhUuid(), equalTo("p"));
        assertThat(persons.get(1).getUhUuid(), equalTo("o"));
        assertThat(persons.get(2).getUhUuid(), equalTo("n"));
        assertThat(persons.get(3).getUhUuid(), equalTo("m"));

        Collections.sort(persons);

        assertThat(persons.get(0).getName(), equalTo(""));
        assertThat(persons.get(1).getName(), equalTo(""));
        assertThat(persons.get(2).getName(), equalTo(""));
        assertThat(persons.get(3).getName(), equalTo(""));
        assertThat(persons.get(0).getUhUuid(), equalTo("m"));
        assertThat(persons.get(1).getUhUuid(), equalTo("n"));
        assertThat(persons.get(2).getUhUuid(), equalTo("o"));
        assertThat(persons.get(3).getUhUuid(), equalTo("p"));

        // Again.
        persons = new ArrayList<>();
        persons.add(new Person("", "", "z"));
        persons.add(new Person("", "", "y"));
        persons.add(new Person("", "", "x"));
        persons.add(new Person("", "", "w"));

        assertThat(persons.get(0).getUsername(), equalTo("z"));
        assertThat(persons.get(1).getUsername(), equalTo("y"));
        assertThat(persons.get(2).getUsername(), equalTo("x"));
        assertThat(persons.get(3).getUsername(), equalTo("w"));

        Collections.sort(persons);

        assertThat(persons.get(0).getUsername(), equalTo("w"));
        assertThat(persons.get(1).getUsername(), equalTo("x"));
        assertThat(persons.get(2).getUsername(), equalTo("y"));
        assertThat(persons.get(3).getUsername(), equalTo("z"));

        // Again.
        persons = new ArrayList<>();
        persons.add(new Person(null, null, "4"));
        persons.add(new Person(null, null, "3"));
        persons.add(new Person(null, null, "2"));
        persons.add(new Person(null, null, "1"));

        assertThat(persons.get(0).getUsername(), equalTo("4"));
        assertThat(persons.get(1).getUsername(), equalTo("3"));
        assertThat(persons.get(2).getUsername(), equalTo("2"));
        assertThat(persons.get(3).getUsername(), equalTo("1"));

        Collections.sort(persons);

        assertThat(persons.get(0).getUsername(), equalTo("1"));
        assertThat(persons.get(1).getUsername(), equalTo("2"));
        assertThat(persons.get(2).getUsername(), equalTo("3"));
        assertThat(persons.get(3).getUsername(), equalTo("4"));
    }

    @Test
    public void getAttributeTest() {
        String username = person.getAttribute("username");
        assertThat(person.getUsername(), equalTo(username));
    }

    @Test
    public void setAttributeTest() {
        assertFalse(person.getUsername() == "a");
        person.setAttribute("username", "a");
        assertTrue(person.getAttribute("username") == "a");
    }
}
