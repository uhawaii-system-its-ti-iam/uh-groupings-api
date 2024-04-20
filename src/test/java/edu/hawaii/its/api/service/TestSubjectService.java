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

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.dept-uh-usernames}")
    private List<String> TEST_DEPT_UH_UIDS;

    @Test
    public void constructor() {
        assertNotNull(subjectService);
    }

    @Test
    public void isValidIdentifier() {
        for (String uid : TEST_UH_USERNAMES) {
            assertTrue(subjectService.isValidIdentifier(uid));
        }
        for (String number : TEST_UH_NUMBERS) {
            assertTrue(subjectService.isValidIdentifier(number));
        }
        assertTrue(subjectService.isValidIdentifier(TEST_DEPT_UH_UIDS.get(1))); // testiwt2 is valid
        assertFalse(subjectService.isValidIdentifier("invalid-identifier"));
    }

    @Test
    public void getValidUhUuids() {
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(TEST_UH_NUMBERS));
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(TEST_UH_USERNAMES));

        List<String> hasInvalidIdentifiers = new ArrayList<>(TEST_UH_USERNAMES);
        hasInvalidIdentifiers.add("invalid-identifier");
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(hasInvalidIdentifiers));

        hasInvalidIdentifiers = new ArrayList<>(TEST_UH_NUMBERS);
        hasInvalidIdentifiers.add("invalid-identifier");
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(hasInvalidIdentifiers));
    }

    @Test
    public void getValidUhUuid() {
        String uid = TEST_UH_USERNAMES.get(0);
        String number = TEST_UH_NUMBERS.get(0);
        assertEquals(number, subjectService.getValidUhUuid(uid));
        assertEquals(number, subjectService.getValidUhUuid(number));

        assertEquals("", subjectService.getValidUhUuid("invalid-identifier"));
    }

}
