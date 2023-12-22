package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
public class InvalidUhIdentifiersResultsTest {

    @Test
    public void test() {
        List<String> invalidUhIdentifiers = Arrays.asList("invalid1", "invalid2");
        InvalidUhIdentifiersResults results = new InvalidUhIdentifiersResults(invalidUhIdentifiers);
        assertEquals("SUCCESS", results.getResultCode());
        assertNotNull(results.getResults());
        assertEquals(2, results.getResults().size());

        InvalidUhIdentifiersResults emptyResults = new InvalidUhIdentifiersResults(Arrays.asList());
        assertEquals("SUCCESS", emptyResults.getResultCode());
        assertNotNull(emptyResults.getResults());
        assertEquals(0, emptyResults.getResults().size());

        InvalidUhIdentifiersResults emptyConstructorResults = new InvalidUhIdentifiersResults();
        assertEquals("FAILURE", emptyConstructorResults.getResultCode());
        assertNotNull(emptyConstructorResults.getResults());
        assertEquals(0, emptyConstructorResults.getResults().size());
    }
}
