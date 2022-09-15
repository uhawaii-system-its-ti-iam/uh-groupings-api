package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectsResultsTest {

    private static Properties properties;
    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);

        subjectsResults = new SubjectsResults(null);
        assertNotNull(subjectsResults);

        subjectsResults = new SubjectsResults();
        assertNotNull(subjectsResults);
    }

    @Test
    public void successfulResultsTest() {
        String json = propertyValue("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjectsResults);
        assertEquals(SUCCESS, subjectsResults.getResultCode());
        assertNotNull(subjects);

        assertEquals(4, subjects.size());

        String[] array = { SUBJECT_NOT_FOUND, SUCCESS, SUCCESS, SUCCESS };
        List<String> expectedResultCodes = Arrays.asList(array);
        Iterator<String> resultCodesIter = expectedResultCodes.iterator();
        Iterator<Subject> subjectsIter = subjects.iterator();

        while (resultCodesIter.hasNext() && subjectsIter.hasNext()) {
            assertEquals(resultCodesIter.next(), subjectsIter.next().getResultCode());
        }
    }

    @Test
    public void failedResultsTest() {
        String json = propertyValue("ws.get.subjects.results.failure");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        assertNotNull(subjects);
    }

    @Test
    public void emptyResultsTest() {
        String json = propertyValue("ws.get.subjects.results.empty");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        assertNotNull(subjects);
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
