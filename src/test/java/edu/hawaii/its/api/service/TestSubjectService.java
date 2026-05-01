package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;;
import edu.hawaii.its.api.exception.InvalidUhIdentifierException;
import edu.hawaii.its.api.util.JsonUtil;

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Test
    public void constructor() {
        assertNotNull(subjectService);
    }

    @Test
    public void isValidIdentifier() {
        for (String uid : TEST_UIDS) {
            assertTrue(subjectService.isValidIdentifier(ADMIN, uid));
        }
        for (String uhuuid : TEST_UH_UUIDS) {
            assertTrue(subjectService.isValidIdentifier(ADMIN, uhuuid));
        }
        assertTrue(subjectService.isValidIdentifier(ADMIN, TEST_DEPT_UH_UIDS.get(1))); // testiwt2 is valid
        assertFalse(subjectService.isValidIdentifier(ADMIN, "invalidIdentifier"));

        //invalid identifier (null)
        try {
            subjectService.isValidIdentifier(ADMIN, null);
            fail("Should throw an exception if identifier is null");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (empty)
        try {
            subjectService.isValidIdentifier(ADMIN, "");
            fail("Should throw an exception if identifier is empty");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (too long)
        try {
            String longIdentifier = "a".repeat(70);
            subjectService.isValidIdentifier(ADMIN, longIdentifier);
            fail("Should throw an exception if identifier is too long");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (bad characters)
        try {
            subjectService.isValidIdentifier(ADMIN, "@bad_identifier");
            fail("Should throw an exception if identifier has invalid characters");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //accept only a leading underscore (This is well-formed but not valid)
        assertFalse(subjectService.isValidIdentifier(ADMIN, "_testiwt"));
        try {
            subjectService.isValidIdentifier(ADMIN, "__invalidIdentifier");
            fail("Should throw an exception if identifier has a non-leading underscore");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
    }

    @Test
    public void getValidUhUuids() {
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(ADMIN, TEST_UH_UUIDS));
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(ADMIN, TEST_UIDS));

        List<String> hasInvalidIdentifiers = new ArrayList<>(TEST_UIDS);
        hasInvalidIdentifiers.add("non-existant-identifier");
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(ADMIN, hasInvalidIdentifiers));

        hasInvalidIdentifiers = new ArrayList<>(TEST_UH_UUIDS);
        hasInvalidIdentifiers.add("non-existant-identifier");
        assertEquals(TEST_UH_UUIDS, subjectService.getValidUhUuids(ADMIN, hasInvalidIdentifiers));

        List<String> invalidIdentifiers = List.of(
                "",
                "a".repeat(260),
                "@invalid-identifier!"
        );
        assertEquals(new ArrayList<>(), subjectService.getValidUhUuids(ADMIN, invalidIdentifiers));

    }

    @Test
    public void getValidUhUuid() {
        String uid = TEST_UIDS.get(0);
        String uhuuid = TEST_UH_UUIDS.get(0);
        assertEquals(uhuuid, subjectService.getValidUhUuid(ADMIN, uid));
        assertEquals(uhuuid, subjectService.getValidUhUuid(ADMIN, uhuuid));

        //identifier not found
        try {
            String result = subjectService.getValidUhUuid(ADMIN, "nonExistantIdentifier");
            assertEquals("", result);
        } catch (InvalidUhIdentifierException e) {
            fail("Should not throw exception if identifier cannot be found");
        }
        //invalid identifier (null)
        try {
            subjectService.getValidUhUuid(ADMIN, null);
            fail("Should throw an exception if identifier is null");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (empty)
        try {
            subjectService.getValidUhUuid(ADMIN, "");
            fail("Should throw an exception if identifier is empty");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (too long)
        try {
            String longIdentifier = "a".repeat(70);
            subjectService.getValidUhUuid(ADMIN, longIdentifier);
            fail("Should throw an exception if identifier is too long");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
        //invalid identifier (bad characters)
        try {
            subjectService.getValidUhUuid(ADMIN, "@bad_identifier");
            fail("Should throw an exception if identifier has invalid characters");
        } catch (InvalidUhIdentifierException e) {
            JsonUtil.printJson(e);
        }
    }

}
