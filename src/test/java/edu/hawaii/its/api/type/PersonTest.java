package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.hawaii.its.api.wrapper.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PersonTest {

    private Person person;

    @BeforeEach
    public void beforeEach() {
        person = new Person();
    }

    @Test
    public void construction() {
        assertNotNull(person);
        assertNull(person.getName());
        assertNull(person.getUhUuid());
        assertNull(person.getUsername());
        assertNull(person.getFirstName());
        assertNull(person.getLastName());

        assertNull(person.getAttribute(Person.ATTRIBUTE_COMMON_NAME));
        assertNull(person.getAttribute(Person.ATTRIBUTE_UHUUID));
        assertNull(person.getAttribute(Person.ATTRIBUTE_USERNAME));
        assertNull(person.getAttribute(Person.ATTRIBUTE_FIRST_NAME));
        assertNull(person.getAttribute(Person.ATTRIBUTE_LAST_NAME));

        person = new Person("a", "b", "c");
        assertThat(person.getName(), equalTo("a"));
        assertThat(person.getUhUuid(), equalTo("b"));
        assertThat(person.getUsername(), equalTo("c"));

        Person person2 = new Person("a", "b", "c", "d", "e");
        assertThat(person2.getName(), equalTo("a"));
        assertThat(person2.getUhUuid(), equalTo("b"));
        assertThat(person2.getUsername(), equalTo("c"));
        assertThat(person2.getFirstName(), equalTo("d"));
        assertThat(person2.getLastName(), equalTo("e"));

        assertThat(Person.primaryAttributeCount(), equalTo(5));
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

        person.setUhUuid("uhUuid");
        assertThat(person.getName(), equalTo("name"));
        assertThat(person.getUhUuid(), equalTo("uhUuid"));
        assertNull(person.getUsername());

        person.setUsername("username");
        assertThat(person.getName(), equalTo("name"));
        assertThat(person.getUhUuid(), equalTo("uhUuid"));
        assertThat(person.getUsername(), equalTo("username"));

        person.setLastName("lastName");
        assertEquals("lastName", person.getLastName());
        assertNotNull(person.getLastName());

        person.setFirstName("firstName");
        assertEquals("firstName", person.getFirstName());
        assertNotNull(person.getFirstName());
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

        p0.setUhUuid("uhUuid");
        assertFalse(p0.equals(p1));
        assertFalse(p1.equals(p0));
        p1.setUhUuid("uhUuid");
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
        result = prime * result + "uhUuid".hashCode();
        person.setName(null);
        person.setUsername(null);
        person.setUhUuid("uhUuid");
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

        assertThat(persons.get(0).getUhUuid(), equalTo("p"));
        assertThat(persons.get(1).getUhUuid(), equalTo("o"));
        assertThat(persons.get(2).getUhUuid(), equalTo("n"));
        assertThat(persons.get(3).getUhUuid(), equalTo("m"));

        Collections.sort(persons);

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
        persons.add(new Person("", "", ""));
        persons.add(new Person("", "", ""));
        persons.add(new Person("", "", ""));
        persons.add(new Person("", "", ""));

        assertThat(persons.get(0).getUsername(), equalTo(""));
        assertThat(persons.get(1).getUsername(), equalTo(""));
        assertThat(persons.get(2).getUsername(), equalTo(""));
        assertThat(persons.get(3).getUsername(), equalTo(""));

        Collections.sort(persons);

        assertThat(persons.get(0).getUsername(), equalTo(""));
        assertThat(persons.get(1).getUsername(), equalTo(""));
        assertThat(persons.get(2).getUsername(), equalTo(""));
        assertThat(persons.get(3).getUsername(), equalTo(""));
    }

    @Test
    public void addAttribute() {
        assertThat(person.getAttributes().size(), equalTo(0));

        person.addAttribute(null, null);
        assertThat(person.getAttributes().size(), equalTo(0));

        person.addAttribute(null, "malcom");
        assertThat(person.getAttributes().size(), equalTo(0));
        assertThat(person.getAttribute(null), equalTo(null));
        assertThat(person.getAttribute(null), is(nullValue()));

        person.addAttribute("angus", "young");
        assertThat(person.getAttributes().size(), equalTo(1));
        assertThat(person.getAttribute("angus"), equalTo("young"));
        assertThat(person.getAttribute(null), is(nullValue()));
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

    @Test
    public void toCsvTest() {
        String uid = "uid";
        String uhUuid = "uhUuid";
        String firstName = "firstName";
        String lastName = "lastName";
        String name = "name";
        person = new Person(name, uhUuid, uid, firstName, lastName);
        String[] csv = person.toCsv();
        List<String> fields = Arrays.asList(uid, uhUuid, firstName, lastName, name);
        int i = 0;
        for (String field : fields) {
            assertEquals(field, csv[i++]);
        }
    }

    @Test
    public void toStringTest() {
        String name = "name";
        String uid = "uid";
        String uhUuid = "uhUuid";
        person = new Person(name, uhUuid, uid);
        String expected = "Person [" + "name=" + name + ", uhUuid=" + uhUuid + ", username=" + uid + "]";
        assertEquals(expected, person.toString());
    }

    @Test
    public void subjectConstructorTest() {
        Subject subject = new Subject();
        Person person = new Person(subject);
        assertThat(person.getName(), is(subject.getName()));
        assertThat(person.getUhUuid(), is(subject.getUhUuid()));
        assertThat(person.getUsername(), is(subject.getUid()));
        assertThat(person.getFirstName(), is(subject.getFirstName()));
        assertThat(person.getLastName(), is(subject.getLastName()));
    }
}
