package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SubjectTest {

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_NUMBERS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Value("${groupings.api.test.uh-first-names}")
    private List<String> TEST_FIRSTNAMES;

    @Value("${groupings.api.test.uh-last-names}")
    private List<String> TEST_LASTNAMES;

    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        subject = new Subject(null);
        assertNotNull(subject);
        assertNotNull(new Subject());
    }

    @Test
    public void accessors() {
        String username = TEST_USERNAMES.get(0);
        String number = TEST_NUMBERS.get(0);
        String name = TEST_NAMES.get(0);
        String firstName = TEST_FIRSTNAMES.get(0);
        String lastName = TEST_LASTNAMES.get(0);
        // Successful query using a uid.
        String json = propertyLocator.find("ws.subject.success.uid");
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
        json = propertyLocator.find("ws.subject.success.uhuuid");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(username, subject.getUid());
        assertEquals(number, subject.getUhUuid());
        assertEquals(name, subject.getName());
        assertEquals(firstName, subject.getFirstName());
        assertEquals(lastName, subject.getLastName());
        assertEquals("UH core LDAP", subject.getSourceId());

        // Unsuccessful query using uid.
        json = propertyLocator.find("ws.subject.subject.uid.not.found");
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
        json = propertyLocator.find("ws.subject.subject.uhuuid.not.found");
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

        // Null values
        json = propertyLocator.find("ws.subject.success.null.values");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals("", subject.getResultCode());
        assertEquals("", subject.getFirstName());
        assertEquals("", subject.getLastName());

    }

    @Test
    public void hasUHAttributesTest() {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setAttributeValues(new String[]{"attribute"});
        Subject subject = new Subject(wsSubject);
        assertTrue(subject.hasUHAttributes());
    }

    @Test
    public void hasUHAttributesNullTest() {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setAttributeValues(null);
        Subject subject = new Subject(wsSubject);
        assertFalse(subject.hasUHAttributes());
    }

    @Test
    public void hasUHAttributesEmptyTest() {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setAttributeValues(new String[]{""});
        Subject subject = new Subject(wsSubject);
        assertFalse(subject.hasUHAttributes());
    }

}
