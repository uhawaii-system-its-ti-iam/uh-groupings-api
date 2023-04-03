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
        // Successful query using a uid.
        String json = propertyValue("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(username, subject.getUid());
        assertEquals(number, subject.getUhUuid());

        // Successful query using a uhUuid.
        json = propertyValue("ws.subject.success.uhuuid");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(username, subject.getUid());
        assertEquals(number, subject.getUhUuid());

        // Unsuccessful query using uid.
        json = propertyValue("ws.subject.subject.not.found");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUBJECT_NOT_FOUND, subject.getResultCode());
        assertEquals("", subject.getUid());
        assertEquals("", subject.getUhUuid());
        assertEquals("", subject.getName());
    }

    public List<String> getTestUsernames() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNumbers() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
