package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void constructor() {
        assertNotNull(subjectService);
    }

    @Test
    public void isValidIdentifier() {
        for (String username : TEST_UH_USERNAMES) {
            assertTrue(subjectService.isValidIdentifier(username));
        }
        for (String number : TEST_UH_NUMBERS) {
            assertTrue(subjectService.isValidIdentifier(number));
        }
        assertFalse(subjectService.isValidIdentifier("invalid-identifier"));
    }

    @Test
    public void getValidUhUuids() {
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(TEST_UH_NUMBERS));
        assertEquals(TEST_UH_NUMBERS, subjectService.getValidUhUuids(TEST_UH_USERNAMES));
    }

    @Test
    public void getValidUhUuid() {
        String username = TEST_UH_USERNAMES.get(0);
        String number = TEST_UH_NUMBERS.get(0);
        assertEquals(number, subjectService.getValidUhUuid(username));
        assertEquals(number, subjectService.getValidUhUuid(number));

        try {
            subjectService.getValidUhUuid("invalid-identifier");
        }catch (UhMemberNotFoundException e) {
            assertEquals("SUBJECT_NOT_FOUND", e.getReason());
        }
    }

}
