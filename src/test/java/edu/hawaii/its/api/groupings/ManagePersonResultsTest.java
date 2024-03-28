package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.type.ManagePersonResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagePersonResultsTest {

    @Test
    public void testManagePersonResultsConstructor() {
        List<ManagePersonResult> results = new ArrayList<>();
        ManagePersonResult managePersonResult = new ManagePersonResult();
        results.add(managePersonResult);

        ManagePersonResults managePersonResults = new ManagePersonResults(results);
        assertNotNull(managePersonResults);
        assertEquals("SUCCESS", managePersonResults.getResultCode());
        assertEquals(1, managePersonResults.getResults().size());

        ManagePersonResults emptyManagePersonResults = new ManagePersonResults();
        assertNotNull(emptyManagePersonResults);
        assertEquals("FAILURE", emptyManagePersonResults.getResultCode());
        assertEquals(0, emptyManagePersonResults.getResults().size());
    }
}
