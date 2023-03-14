package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectTest {

    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";
    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        subject = new Subject(null);
        assertNotNull(subject);
        assertNotNull(new Subject());
    }

    @Test
    public void accessors() {
        String username = getTestUsernames().get(0);
        String number = getTestNumbers().get(0);
        String name = getTestNames().get(0);
        String firstName = getTestFirstNames().get(0);
        String lastName = getTestLastNames().get(0);
        // Successful query using a uid.
        String json = propertyValue("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(username, subject.getUid());
        assertEquals(number, subject.getUhUuid());
        assertEquals(name, subject.getName());
        assertEquals(firstName, subject.getFirstName());
        assertEquals(lastName, subject.getLastName());

        // Successful query using a uhUuid.
        json = propertyValue("ws.subject.success.uhuuid");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(username, subject.getUid());
        assertEquals(number, subject.getUhUuid());
        assertEquals(name, subject.getName());
        assertEquals(firstName, subject.getFirstName());
        assertEquals(lastName, subject.getLastName());

        // Unsuccessful query using uid.
        json = propertyValue("ws.subject.subject.uid.not.found");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUBJECT_NOT_FOUND, subject.getResultCode());
        assertEquals(wsSubject.getIdentifierLookup(), subject.getUid());
        assertEquals("invalid-uid", subject.getUid());
        assertEquals("", subject.getUhUuid());
        assertEquals("", subject.getName());
        assertEquals("", subject.getFirstName());
        assertEquals("", subject.getLastName());

        // Unsuccessful query using uhUuid.
        json = propertyValue("ws.subject.subject.uhuuid.not.found");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUBJECT_NOT_FOUND, subject.getResultCode());
        assertEquals(wsSubject.getId(), subject.getUhUuid());
        assertEquals("11111111", subject.getUhUuid());
        assertEquals("", subject.getUid());
        assertEquals("", subject.getName());
        assertEquals("", subject.getFirstName());
        assertEquals("", subject.getLastName());
    }

    public List<String> getTestUsernames() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNumbers() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNames() {
        String[] array = { "Testf-iwt-a TestIAM-staff", "Testf-iwt-b TestIAM-staff", "Testf-iwt-c TestIAM-staff",
                "Testf-iwt-d TestIAM-faculty", "Testf-iwt-e TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestLastNames() {
        String[] array = { "TestIAM-staff", "TestIAM-staff", "TestIAM-staff",
                "TestIAM-faculty", "TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestFirstNames() {
        String[] array = { "Testf-iwt-a", "Testf-iwt-b", "Testf-iwt-c",
                "Testf-iwt-d", "Testf-iwt-e" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
