package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectTest {

    private static Properties properties;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

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
    }

    @Test
    public void accessors() {
        // Successful query using a uid.
        String json = propertyValue("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals("uid", subject.getUid());
        assertEquals("uhuuid", subject.getUhUuid());
        assertEquals("name", subject.getName());
        assertEquals(SUCCESS, subject.getResultCode());

        // Successful query using a uhUuid.
        json = propertyValue("ws.subject.success.uhuuid");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals("uid", subject.getUid());
        assertEquals("uhuuid", subject.getUhUuid());
        assertEquals("name", subject.getName());
        assertEquals(SUCCESS, subject.getResultCode());

        // Unsuccessful query using uid.
        json = propertyValue("ws.subject.subject.not.found");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals("", subject.getUid());
        assertEquals("", subject.getUhUuid());
        assertEquals("", subject.getName());
        assertEquals(SUBJECT_NOT_FOUND, subject.getResultCode());
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
