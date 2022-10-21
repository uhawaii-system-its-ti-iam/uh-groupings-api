package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import static edu.hawaii.its.api.wrapper.SubjectResult.SUBJECT_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectResultTest {

    private static PropertyLocator propertyLocator;

    @BeforeAll
    public static void beforeAll() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void nullConstruction() {
        SubjectResult results = new SubjectResult(null);
        assertThat(results, is(notNullValue()));
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));
    }

    @Test
    public void emptyConstruction() {
        SubjectResult results = new SubjectResult(new WsGetSubjectsResults());
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));

        results = new SubjectResult();
        assertNotNull(results);
    }

    @Test
    public void emptyConstructionAgain() {
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        assertThat(wsGetSubjectsResults.getWsSubjects(), is(nullValue()));
        WsSubject[] wsSubjects = new WsSubject[0];
        assertThat(wsSubjects.length, equalTo(0));

        SubjectResult results = new SubjectResult(wsGetSubjectsResults);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(0));

        String json = propertyLocator.find("subject.not.found");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        results = new SubjectResult(wsGetSubjectsResults);
        assertThat(results.getResultCode(), equalTo(SUBJECT_NOT_FOUND));
        assertThat(results.getSubjectAttributeNameCount(), equalTo(5));
    }

    @Test
    public void emptyConstructionFromJson() {
        String json = propertyLocator.find("subject.not.found");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        assertThat(wsGetSubjectsResults.getWsSubjects(), is(notNullValue()));
        SubjectResult results = new SubjectResult(wsGetSubjectsResults);
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
        SubjectResult results = new SubjectResult(wsGetSubjectsResults);
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
        SubjectResult results = new SubjectResult(wsGetSubjectsResults);

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

        String json = propertyLocator.find("subject.found");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        results = new SubjectResult(wsGetSubjectsResults);

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
}