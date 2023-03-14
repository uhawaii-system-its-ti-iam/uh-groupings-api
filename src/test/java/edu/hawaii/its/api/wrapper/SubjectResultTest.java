package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectResultTest {

    private static Properties properties;

    final static private String SUCCESS = "SUCCESS";

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
        SubjectResult subjectResult = new SubjectResult(wsGetSubjectsResults);
        assertNotNull(subjectResult);

        subjectResult = new SubjectResult(null);
        assertNotNull(subjectResult);

        subjectResult = new SubjectResult();
        assertNotNull(subjectResult);
    }

    @Test
    public void successfulResultsTest() {
        String json = propertyValue("ws.get.subject.result.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectResult subjectResult = new SubjectResult(wsGetSubjectsResults);
        assertEquals(SUCCESS, subjectResult.getResultCode());
        assertEquals(getTestNumbers().get(0), subjectResult.getUhUuid());
        assertEquals(getTestUsernames().get(0), subjectResult.getUid());
        assertEquals(getTestNames().get(0), subjectResult.getName());
        assertEquals(getTestFirstNames().get(0), subjectResult.getFirstName());
        assertEquals(getTestLastNames().get(0), subjectResult.getLastName());

        assertNotNull(subjectResult.getSubject());
    }

    @Test
    public void failedResultsTest() {
        String json = propertyValue("ws.get.subject.result.failure");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);

        SubjectResult subjectResult = new SubjectResult(wsGetSubjectsResults);
        assertEquals("SUBJECT_NOT_FOUND", subjectResult.getResultCode());
        assertEquals("", subjectResult.getUhUuid());
        assertEquals("", subjectResult.getUid());
        assertEquals("", subjectResult.getName());
        assertEquals("", subjectResult.getLastName());
        assertEquals("", subjectResult.getFirstName());
    }

    public List<String> getTestUsernames() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNumbers() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNames() {
        String[] array = { "Testf-iwt-a TestIAM-staff", "Testf-iwt-b TestIAM-staff", "Testf-iwt-c TestIAM-staff",
                "Testf-iwt-d TestIAM-faculty", "Testf-iwt-e TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestLastNames() {
        String[] array = { "TestIAM-staff", "TestIAM-staff", "TestIAM-staff",
                "TestIAM-faculty", "TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestFirstNames() {
        String[] array = { "Testf-iwt-a", "Testf-iwt-b", "Testf-iwt-c",
                "Testf-iwt-d", "Testf-iwt-e" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }

}
