package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class }) public class GroupTest {
    private PropertyLocator propertyLocator;

    @BeforeEach public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.group");
        WsGroup wsGroup = JsonUtil.asObject(json, WsGroup.class);
        assertNotNull(wsGroup);
        Group group = new Group(wsGroup);
        assertNotNull(group);
        assertEquals("SUCCESS", group.getResultCode());
        assertEquals("extension", group.getExtension());
        assertEquals("description", group.getDescription());
        assertEquals("group-path", group.getGroupPath());
        assertEquals("grouper-uuid", group.getGrouperUuid());
        assertTrue(group.isValidPath());
    }

    @Test
    public void invalidGroup() {
        Group group = new Group(null);
        assertEquals("FAILURE", group.getResultCode());
        assertEquals("", group.getExtension());
        assertEquals("", group.getDescription());
        assertEquals("", group.getGroupPath());
        assertEquals("", group.getGrouperUuid());
    }

}
