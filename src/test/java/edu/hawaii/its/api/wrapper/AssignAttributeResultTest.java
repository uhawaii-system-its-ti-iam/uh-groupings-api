package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AssignAttributeResultTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        AssignAttributeResult assignAttributeResult =
                groupingsTestConfiguration.assignAttributesResultsTurnOffOptInSuccessTestData();
        assertNotNull(assignAttributeResult);
        assertEquals("SUCCESS", assignAttributeResult.getResultCode());
        assertTrue(assignAttributeResult.isAttributeChanged());
        assertFalse(assignAttributeResult.isAttributeValuesChanged());
        assertTrue(assignAttributeResult.isAttributeRemoved());

        assignAttributeResult =
                groupingsTestConfiguration.assignAttributesResultsNullAssignAttributeResultTestData();
        assertNotNull(assignAttributeResult);
        assertEquals("FAILURE", assignAttributeResult.getResultCode());
        assertFalse(assignAttributeResult.isAttributeChanged());
        assertFalse(assignAttributeResult.isAttributeValuesChanged());
        assertFalse(assignAttributeResult.isAttributeRemoved());
    }
}
