package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

public class FindAttributesResultsTest {
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
        FindAttributesResults findAttributesResults = new FindAttributesResults(wsFindAttributeDefNamesResults);
        assertNotNull(findAttributesResults);
        assertEquals("SUCCESS", findAttributesResults.getResultCode());
    }
}
