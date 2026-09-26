package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SubjectsResultsTest {

    final static private String SUCCESS = "SUCCESS";
    final static private String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertNotNull(subjectsResults);

        subjectsResults = new SubjectsResults(null);
        assertNotNull(subjectsResults);

        subjectsResults = new SubjectsResults();
        assertNotNull(subjectsResults);
    }

    @Test
    public void successfulResultsTest() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
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
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsFailureTestData();
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        assertNotNull(subjects);
    }

    @Test
    public void isSuccessfulUsesRawResultMetadataTest() {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(SUCCESS);
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);

        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertEquals(true, subjectsResults.isSuccessful());

        resultMetadata.setResultCode("FAILURE");
        assertEquals(false, subjectsResults.isSuccessful());
    }

    @Test
    public void emptyResultsTest() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsEmptyTestData();
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        assertNotNull(subjects);
    }

    @Test
    public void getSubjectsInRequestOrderReturnsOneSubjectPerRequestUnfiltered() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        List<Subject> subjects = subjectsResults.getSubjectsInRequestOrder();
        assertNotNull(subjects);
        assertEquals(subjectsResults.getSubjects().size(), subjects.size());

        String[] array = { SUBJECT_NOT_FOUND, SUCCESS, SUCCESS, SUCCESS };
        List<String> expectedResultCodes = Arrays.asList(array);
        Iterator<String> resultCodesIter = expectedResultCodes.iterator();
        Iterator<Subject> subjectsIter = subjects.iterator();
        while (resultCodesIter.hasNext() && subjectsIter.hasNext()) {
            assertEquals(resultCodesIter.next(), subjectsIter.next().getResultCode());
        }
    }

    @Test
    public void getSubjectsInRequestOrderKeepsSuccessfulSubjectsThatGetSubjectsWouldFilterOut() {
        // getSubjects() drops a successful-but-attribute-less ("orphan") subject entirely, which would
        // desync a caller correlating each response back to the request identifier at that position.
        // getSubjectsInRequestOrder() must keep it.
        WsSubject orphan = new WsSubject();
        orphan.setResultCode(SUCCESS);
        orphan.setId("uhuuid-orphan");

        WsSubject normal = new WsSubject();
        normal.setResultCode(SUCCESS);
        normal.setId("uhuuid-normal");
        normal.setAttributeValues(new String[] { "uid", "name", "lastname", "firstname", "affiliation" });

        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(SUCCESS);
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(new WsSubject[] { orphan, normal });

        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertEquals(1, subjectsResults.getSubjects().size());
        assertEquals(2, subjectsResults.getSubjectsInRequestOrder().size());
        assertEquals("uhuuid-orphan", subjectsResults.getSubjectsInRequestOrder().get(0).getUhUuid());
        assertEquals("uhuuid-normal", subjectsResults.getSubjectsInRequestOrder().get(1).getUhUuid());
    }

    @Test
    public void getSubjectsInRequestOrderReturnsEmptyListForNoSubjects() {
        SubjectsResults subjectsResults = new SubjectsResults();
        assertNotNull(subjectsResults.getSubjectsInRequestOrder());
        assertEquals(0, subjectsResults.getSubjectsInRequestOrder().size());
    }

}
