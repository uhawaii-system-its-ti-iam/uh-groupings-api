package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Person implements Comparable<Person> {

    public static String ATTRIBUTE_COMMON_NAME = "cn";
    public static String ATTRIBUTE_FIRST_NAME = "givenName";
    public static String ATTRIBUTE_LAST_NAME = "sn";
    public static String ATTRIBUTE_UHUUID = "uhUuid";
    public static String ATTRIBUTE_USERNAME = "uid";
    private final static String[] ATTRIBUTE_NAMES = {
            ATTRIBUTE_COMMON_NAME,
            ATTRIBUTE_FIRST_NAME,
            ATTRIBUTE_LAST_NAME,
            ATTRIBUTE_UHUUID,
            ATTRIBUTE_USERNAME
    };

    private Map<String, String> attributes = new HashMap<>();

    public static int primaryAttributeCount() {
        return ATTRIBUTE_NAMES.length;
    }

    // Constructor.
    public Person() {
        // Empty.
    }

    // Constructor.
    public Person(String name) {
        this();
        attributes.put(ATTRIBUTE_COMMON_NAME, name);
    }

    // Constructor.
    public Person(String name, String uhUuid, String username) {
        this(name);

        attributes.put(ATTRIBUTE_UHUUID, uhUuid);
        attributes.put(ATTRIBUTE_USERNAME, username);
    }

    // Constructor.
    public Person(String name, String uhUuid, String username, String firstName, String lastName) {
        this(name, uhUuid, username);

        attributes.put(ATTRIBUTE_FIRST_NAME, firstName);
        attributes.put(ATTRIBUTE_LAST_NAME, lastName);
    }

    public String getUsername() {
        return attributes.get(ATTRIBUTE_USERNAME);
    }

    public void setUsername(String username) {
        attributes.put(ATTRIBUTE_USERNAME, username);
    }

    public String getName() {
        return attributes.get(ATTRIBUTE_COMMON_NAME);
    }

    public void setName(String name) {
        attributes.put(ATTRIBUTE_COMMON_NAME, name);
    }

    public String getUhUuid() {
        return attributes.get(ATTRIBUTE_UHUUID);
    }

    public void setUhUuid(String uhUuid) {
        attributes.put(ATTRIBUTE_UHUUID, uhUuid);
    }

    public String getFirstName() {
        return attributes.get(ATTRIBUTE_FIRST_NAME);
    }
    public void setFirstName(String firstName) {
        attributes.put(ATTRIBUTE_FIRST_NAME, firstName);
    }
    public void setLastName(String lastName) {
        attributes.put(ATTRIBUTE_LAST_NAME, lastName);
    }

    public String getLastName() {
        return attributes.get(ATTRIBUTE_LAST_NAME);
    }

    @JsonIgnore
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, String value) {
        if (key == null) {
            return;
        }
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String name = getName();
        String username = getUsername();
        String uhUuid = getUhUuid();

        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((uhUuid == null) ? 0 : uhUuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        String name = getName();
        String username = getUsername();
        String uhUuid = getUhUuid();

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (name == null) {
            if (other.getName() != null)
                return false;
        } else if (!name.equals(other.getName()))
            return false;
        if (username == null) {
            if (other.getUsername() != null)
                return false;
        } else if (!username.equals(other.getUsername()))
            return false;
        if (uhUuid == null) {
            return other.getUhUuid() == null;
        } else
            return uhUuid.equals(other.getUhUuid());
    }

    @Override
    public int compareTo(Person person) {
        Comparator<String> nullSafeComparator = Comparator.nullsFirst(String::compareTo);

        int usernameComp = nullSafeComparator.compare(getUsername(), person.getUsername());

        if (usernameComp != 0) {
            return usernameComp;
        }

        int nameComp = nullSafeComparator.compare(getName(), person.getName());
        if (nameComp != 0) {
            return nameComp;
        }

        int uhUuidComp = nullSafeComparator.compare(getUhUuid(), person.getUhUuid());
        if (uhUuidComp != 0) {
            return uhUuidComp;
        }

        return 0;
    }

    public String[] toCsv() {
        String[] data = new String[5];
        data[0] = getUsername();
        data[1] = getUhUuid();
        data[2] = getFirstName();
        data[3] = getLastName();
        data[4] = getName();

        return data;
    }

    @Override
    public String toString() {
        return "Person [name=" + getName() + ", uhUuid=" + getUhUuid() + ", username=" + getUsername() + "]";
    }
}
