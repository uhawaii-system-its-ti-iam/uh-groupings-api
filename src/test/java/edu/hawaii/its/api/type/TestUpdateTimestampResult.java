package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.MembershipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateTimestampResult {
    @Autowired
    private MembershipService membershipService;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    private UpdateTimestampResult updateTimestampResult;

    @Before
    public void setUp() {
        updateTimestampResult = membershipService.updateLastModified(GROUPING_INCLUDE);
    }

    @Test
    public void construction() {
        assertNotNull(updateTimestampResult);
    }

    @Test
    public void wsAssignAttributesResultsTest() {
        assertNotNull(updateTimestampResult);
        assertNotNull(updateTimestampResult.getWsAssignAttributesResults());
        updateTimestampResult.setWsAssignAttributesResults(null);
        assertNull(updateTimestampResult.getWsAssignAttributesResults());
    }

    @Test
    public void pathOfUpdateTest() {
        assertNotNull(updateTimestampResult);
        assertNotNull(updateTimestampResult.getPathOfUpdate());
        assertEquals(GROUPING_INCLUDE, updateTimestampResult.getPathOfUpdate());
        updateTimestampResult.setPathOfUpdate(null);
        assertEquals("", updateTimestampResult.getPathOfUpdate());
    }

    @Test
    public void timestampUpdateArrayTest() {
        assertNotNull(updateTimestampResult);
        assertNotNull(updateTimestampResult.getTimestampUpdateArray());
        updateTimestampResult.setTimestampUpdateArray(null);
        assertNull(updateTimestampResult.getTimestampUpdateArray());
    }
}
