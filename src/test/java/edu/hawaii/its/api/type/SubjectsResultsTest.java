package edu.hawaii.its.api.type;

import static edu.hawaii.its.api.type.SubjectsResults.SUBJECT_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.SubjectsResults;
import edu.hawaii.its.api.util.JsonUtil;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SubjectsResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void nullConstruction() {
        SubjectsResults results = new SubjectsResults(null);
        assertThat(results, is(notNullValue()));
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));
    }

    @Test
    public void emptyConstruction() {
        SubjectsResults results = new SubjectsResults(new WsGetSubjectsResults());
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));
    }

    @Test
    public void emptyConstructionAgain() {
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        assertThat(wsGetSubjectsResults.getWsSubjects(), is(nullValue()));
        WsSubject[] wsSubjects = new WsSubject[0];
        assertThat(wsSubjects.length, equalTo(0));

        SubjectsResults results = new SubjectsResults(wsGetSubjectsResults);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));

        String json = propertyValue("subject.not.found");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        results = new SubjectsResults(wsGetSubjectsResults);
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(5));
    }

    @Test
    public void emptyConstructionFromJson() {
        String json = propertyValue("subject.not.found");
        WsGetSubjectsResults wsGetSubjectsResults =
                JsonUtil.asObject(json, WsGetSubjectsResults.class);
        assertThat(wsGetSubjectsResults.getWsSubjects(), is(notNullValue()));
        SubjectsResults results = new SubjectsResults(wsGetSubjectsResults);
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(5));
    }

    @Test
    public void resultWithoutAttributes() {
        // Set up backing result.
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        assertThat(wsGetSubjectsResults.getWsSubjects(), is(nullValue()));
        final String expectedStatusCode = "rock";
        WsSubject[] wsSubjects = { makeWsSubject(expectedStatusCode) };
        assertThat(wsSubjects.length, equalTo(1));
        wsGetSubjectsResults.setWsSubjects(wsSubjects);

        // What we are testing.
        SubjectsResults results = new SubjectsResults(wsGetSubjectsResults);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        assertThat(results.getResultCode(), equalTo(expectedStatusCode));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));
    }

    @Test
    public void resultWithAttributes() {
        // Set up backing result.
        WsSubject wsSubject = makeWsSubject("rock-n-roll");
        String[] attributeValues = { "ac", "⚡", "dc" };
        wsSubject.setAttributeValues(attributeValues);

        WsSubject rockNRoll = new WsSubject();
        rockNRoll.setAttributeValues(attributeValues);
        WsSubject[] wsSubjects = { rockNRoll };

        String[] attributeNames = { "bon", "angus", "malcom" };
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setSubjectAttributeNames(attributeNames);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);

        // What we are testing.
        SubjectsResults results = new SubjectsResults(wsGetSubjectsResults);

        assertThat(results.getSubjectAttributeNameCount(), equalTo(3));

        assertThat(results.getSubjectAttributeName(0), equalTo("bon"));
        assertThat(results.getSubjectAttributeName(1), equalTo("angus"));
        assertThat(results.getSubjectAttributeName(2), equalTo("malcom"));

        assertThat(results.getAttributeValue(0), equalTo("ac"));
        assertThat(results.getAttributeValue(1), equalTo("⚡"));
        assertThat(results.getAttributeValue(2), equalTo("dc"));

        assertThat(results.getSubjectAttributeName(0), equalTo("bon"));
        assertThat(results.getSubjectAttributeName(1), equalTo("angus"));
        assertThat(results.getSubjectAttributeName(2), equalTo("malcom"));

        String json = propertyValue("subject.found");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        results = new SubjectsResults(wsGetSubjectsResults);

        assertThat(results.getResultCode(), equalTo("SUCCESS"));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(5));

        assertThat(results.getSubjectAttributeName(0), equalTo("uid"));
        assertThat(results.getSubjectAttributeName(1), equalTo("cn"));
        assertThat(results.getSubjectAttributeName(2), equalTo("sn"));
        assertThat(results.getSubjectAttributeName(3), equalTo("givenName"));
        assertThat(results.getSubjectAttributeName(4), equalTo("uhUuid"));

        assertThat(results.getAttributeValue(0), equalTo("iamtst02"));
        assertThat(results.getAttributeValue(1), equalTo("tst02name"));
        assertThat(results.getAttributeValue(2), equalTo("tst02name"));
        assertThat(results.getAttributeValue(3), equalTo("tst02name"));
        assertThat(results.getAttributeValue(4), equalTo("iamtst02"));
    }

    private WsSubject makeWsSubject(String resultCode) {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setResultCode(resultCode);
        return wsSubject;
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
