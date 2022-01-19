package edu.hawaii.its.api.type;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.MembershipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateTimestampResult {
    @Autowired
    private MembershipService membershipService;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    private UpdateTimestampResult updateTimestampResult;

    @BeforeAll
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
