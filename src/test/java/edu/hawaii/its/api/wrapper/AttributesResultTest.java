package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AttributesResultTest {

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        assertNotNull(new AttributesResult(null));

        AttributesResult attributesResult =
                groupingsTestConfiguration.attributeDefNameResultsSuccessTestData().getResults().get(0);

        assertNotNull(attributesResult);
        assertEquals("name", attributesResult.getName());
        assertEquals("description", attributesResult.getDescription());
        assertEquals(SYNC_DESTINATIONS_CHECKBOXES, attributesResult.getDefinition());
    }

    @Test
    public void emptyConstruction() {
        AttributesResult attributesResult = new AttributesResult();
        assertEquals("", attributesResult.getName());
        assertEquals("", attributesResult.getDescription());
        assertEquals("", attributesResult.getDefinition());
    }

}
