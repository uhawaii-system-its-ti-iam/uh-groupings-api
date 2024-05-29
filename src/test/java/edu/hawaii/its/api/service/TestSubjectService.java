package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestSubjectService {
    @Autowired
    private SubjectService subjectService;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.dept-uids}")
    private List<String> TEST_DEPT_UH_UIDS;

    @Test
    public void constructor() {
        assertNotNull(subjectService);
    }

    @Test
    public void isValidIdentifier() {
        for (String uid : TEST_UIDS) {
            assertTrue(subjectService.isValidIdentifier(uid));
        }
        for (String uhuuid : TEST_UH_UUIDS) {
            assertTrue(subjectService.isValidIdentifier(uhuuid));
        }
        assertTrue(subjectService.isValidIdentifier(TEST_DEPT_UH_UIDS.get(1))); // testiwt2 is valid
        assertFalse(subjectService.isValidIdentifier("invalid-identifier"));
    }

    @Test
    public void getValidUhUuids() {
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(TEST_UH_UUIDS));
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(TEST_UIDS));

        List<String> hasInvalidIdentifiers = new ArrayList<>(TEST_UIDS);
        hasInvalidIdentifiers.add("invalid-identifier");
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(hasInvalidIdentifiers));

        hasInvalidIdentifiers = new ArrayList<>(TEST_UH_UUIDS);
        hasInvalidIdentifiers.add("invalid-identifier");
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(hasInvalidIdentifiers));
    }

    @Test
    public void getValidUhUuid() {
        String uid = TEST_UIDS.get(0);
        String uhuuid = TEST_UH_UUIDS.get(0);
        assertEquals(uhuuid, subjectService.getValidUhUuid(uid));
        assertEquals(uhuuid, subjectService.getValidUhUuid(uhuuid));

        assertEquals("", subjectService.getValidUhUuid("invalid-identifier"));
    }

}
