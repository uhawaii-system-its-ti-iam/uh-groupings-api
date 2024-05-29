package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SubjectTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

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
        String uid = TEST_UIDS.get(0);
        String uhuuid = TEST_UH_UUIDS.get(0);
        String name = TEST_NAMES.get(0);
        String firstName = TEST_FIRSTNAMES.get(0);
        String lastName = TEST_LASTNAMES.get(0);
        // Successful query using a uid.
        String json = propertyLocator.find("ws.subject.success.uid");
        WsSubject wsSubject = JsonUtil.asObject(json, WsSubject.class);
        Subject subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(uid, subject.getUid());
        assertEquals(uhuuid, subject.getUhUuid());
        assertEquals(name, subject.getName());
        assertEquals(firstName, subject.getFirstName());
        assertEquals(lastName, subject.getLastName());

        // Successful query using a uhUuid.
        json = propertyLocator.find("ws.subject.success.uhuuid");
        wsSubject = JsonUtil.asObject(json, WsSubject.class);
        subject = new Subject(wsSubject);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(uid, subject.getUid());
        assertEquals(uhuuid, subject.getUhUuid());
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
    public void setFirstNameTest() {
        WsSubject wsSubject = new WsSubject();
        Subject subject = new Subject(wsSubject);
        subject.setFirstName("firstName");
        assertEquals("firstName", subject.getFirstName());
        subject.setFirstName(null);
        assertEquals("", subject.getFirstName());
    }

    @Test
    public void setLastNameTest() {
        WsSubject wsSubject = new WsSubject();
        Subject subject = new Subject(wsSubject);
        subject.setLastName("lastName");
        assertEquals("lastName", subject.getLastName());
        subject.setLastName(null);
        assertEquals("", subject.getLastName());
    }

    @Test
    public void setAttributeValueTest() {
        Subject subject = new Subject();
        subject.setAttributeValue(0, "value0");
        subject.setAttributeValue(1, "value1");
        subject.setAttributeValue(2, "value2");
        subject.setAttributeValue(3, "value3");
        subject.setAttributeValue(4, "value4");

        assertEquals("value0", subject.getAttributeValue(0));
        assertEquals("value1", subject.getAttributeValue(1));
        assertEquals("value2", subject.getAttributeValue(2));
        assertEquals("value3", subject.getAttributeValue(3));
        assertEquals("value4", subject.getAttributeValue(4));
    }

    @Test
    public void setResultCodeTest() {
        WsSubject wsSubject = new WsSubject();
        Subject subject = new Subject(wsSubject);
        subject.setResultCode("resultCode");
        assertEquals("resultCode", subject.getResultCode());
        subject.setResultCode(null);
        assertEquals("", subject.getResultCode());
    }

    @Test
    public void setSourceIdTest() {
        Subject subject = new Subject();
        subject.setSourceId("sourceId");
        assertEquals("sourceId", subject.getSourceId());
        subject.setSourceId(null);
        assertEquals("", subject.getSourceId());
    }

    @Test
    public void equals() {
        Subject subject0 = new Subject();
        assertNotNull(subject0);
        assertFalse(subject0.equals(""));
        assertTrue(subject0.equals(subject0));

        Subject subject1 = new Subject();
        assertTrue(subject0.equals(subject1));
        assertTrue(subject1.equals(subject0));

        subject0.setName("name");
        assertFalse(subject0.equals(subject1));
        assertFalse(subject1.equals(subject0));
        subject1.setName("name");
        assertTrue(subject0.equals(subject1));
        assertTrue(subject1.equals(subject0));

        subject0.setUhUuid("uhUuid");
        assertFalse(subject0.equals(subject1));
        assertFalse(subject1.equals(subject0));
        subject1.setUhUuid("uhUuid");
        assertTrue(subject0.equals(subject1));
        assertTrue(subject1.equals(subject0));

        subject0.setUid("uid");
        assertFalse(subject0.equals(subject1));
        assertFalse(subject1.equals(subject0));
        subject1.setUid("uid");
        assertTrue(subject0.equals(subject1));
        assertTrue(subject1.equals(subject0));
    }

    @Test
    public void toStringTest() {
        String name = "name";
        String uid = "uid";
        String uhUuid = "uhUuid";
        Subject subject = new Subject();
        subject.setName(name);
        subject.setUid(uid);
        subject.setUhUuid(uhUuid);
        String expected = "Subject [" + "name=" + name + ", uhUuid=" + uhUuid + ", uid=" + uid + "]";
        assertEquals(expected, subject.toString());
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
