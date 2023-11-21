package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupAttributeTest {
    private PropertyLocator propertyLocator;
    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void constructor() {
        String json = propertyLocator.find("ws.attribute.assign.success");
        WsAttributeAssign wsAttributeAssign = JsonUtil.asObject(json, WsAttributeAssign.class);
        assertNotNull(new GroupAttribute(wsAttributeAssign));

        assertNotNull(new GroupAttribute(null));
        assertNotNull(new GroupAttribute());
        assertNotNull(new GroupAttribute(new WsAttributeAssign()));
    }

    @Test
    public void accessors() {
        String json = propertyLocator.find("ws.attribute.assign.success");
        WsAttributeAssign wsAttributeAssign = JsonUtil.asObject(json, WsAttributeAssign.class);
        GroupAttribute groupAttribute = new GroupAttribute(wsAttributeAssign);
        assertNotNull(groupAttribute);
        assertEquals("SUCCESS", groupAttribute.getResultCode());
        assertEquals("group-path", groupAttribute.getGroupPath());
        assertEquals(TRIO, groupAttribute.getAttributeName());

        json = propertyLocator.find("ws.attribute.assign.failure");
        wsAttributeAssign = JsonUtil.asObject(json, WsAttributeAssign.class);
        groupAttribute = new GroupAttribute(wsAttributeAssign);
        assertNotNull(groupAttribute);
        assertEquals("FAILURE", groupAttribute.getResultCode());
        assertEquals("", groupAttribute.getGroupPath());
        assertEquals("", groupAttribute.getAttributeName());
    }
}
