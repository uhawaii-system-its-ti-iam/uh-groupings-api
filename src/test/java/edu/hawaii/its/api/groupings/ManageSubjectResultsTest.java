package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.type.ManageSubjectResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManageSubjectResultsTest {

    @Test
    public void testManageSubjectResultsConstructor() {
        List<ManageSubjectResult> results = new ArrayList<>();
        ManageSubjectResult manageSubjectResult = new ManageSubjectResult();
        results.add(manageSubjectResult);

        ManageSubjectResults manageSubjectResults = new ManageSubjectResults(results);
        assertNotNull(manageSubjectResults);
        assertEquals("SUCCESS", manageSubjectResults.getResultCode());
        assertEquals(1, manageSubjectResults.getResults().size());

        ManageSubjectResults emptyManageSubjectResults = new ManageSubjectResults();
        assertNotNull(emptyManageSubjectResults);
        assertEquals("FAILURE", emptyManageSubjectResults.getResultCode());
        assertEquals(0, emptyManageSubjectResults.getResults().size());
    }
}
