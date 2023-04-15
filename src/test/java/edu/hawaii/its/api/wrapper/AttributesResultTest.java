package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AttributesResultTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.attribute.def.name.results.success");
        WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults =
                JsonUtil.asObject(json, WsFindAttributeDefNamesResults.class);
        AttributesResult attributesResult =
                new FindAttributesResults(wsFindAttributeDefNamesResults).getResults().get(0);
        assertNotNull(attributesResult);
        assertEquals("name", attributesResult.getName());
        assertEquals("description", attributesResult.getDescription());
    }
}
