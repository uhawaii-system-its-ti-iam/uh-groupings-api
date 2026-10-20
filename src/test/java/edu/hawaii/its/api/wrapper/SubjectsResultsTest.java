package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void getUnfilteredSubjectsReturnsEverySubjectGrouperAnsweredWith() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        List<Subject> subjects = subjectsResults.getUnfilteredSubjects();
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
    public void getUnfilteredSubjectsKeepsSuccessfulSubjectsThatGetSubjectsWouldFilterOut() {
        // getSubjects() drops a successful-but-attribute-less ("orphan") subject entirely, which would make a
        // caller validating identifiers report a resolved subject as unknown. getUnfilteredSubjects() must keep it.
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
        assertEquals(2, subjectsResults.getUnfilteredSubjects().size());
        assertEquals("uhuuid-orphan", subjectsResults.getUnfilteredSubjects().get(0).getUhUuid());
        assertEquals("uhuuid-normal", subjectsResults.getUnfilteredSubjects().get(1).getUhUuid());
    }

    @Test
    public void getUnfilteredSubjectsReturnsEmptyListForNoSubjects() {
        SubjectsResults subjectsResults = new SubjectsResults();
        assertNotNull(subjectsResults.getUnfilteredSubjects());
        assertEquals(0, subjectsResults.getUnfilteredSubjects().size());
    }

    @Test
    public void mergeOfOneBatchIsThatBatch() {
        SubjectsResults batch = groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        assertSame(batch, SubjectsResults.merge(List.of(batch)));
    }

    @Test
    public void mergeOfNoBatchesIsEmpty() {
        assertTrue(SubjectsResults.merge(List.of()).getUnfilteredSubjects().isEmpty());
    }

    @Test
    public void mergeCombinesTheBatchesInOrder() {
        // Like real Grouper, each batch collapses its unresolved lookups into one SUBJECT_NOT_FOUND entry.
        SubjectsResults merged = SubjectsResults.merge(List.of(
                batch(SUCCESS, found("00000001"), notFound()),
                batch(SUCCESS, notFound(), found("00000002"))));

        assertTrue(merged.isSuccessful());
        assertEquals(List.of(SUCCESS, SUBJECT_NOT_FOUND, SUBJECT_NOT_FOUND, SUCCESS),
                merged.getUnfilteredSubjects().stream().map(Subject::getResultCode).toList());
        assertEquals(List.of("00000001", "", "", "00000002"),
                merged.getUnfilteredSubjects().stream().map(Subject::getUhUuid).toList());
    }

    @Test
    public void mergeIsUnsuccessfulWhenABatchIs() {
        SubjectsResults merged = SubjectsResults.merge(List.of(
                batch(SUCCESS, found("00000001")),
                batch("FAILURE", notFound()),
                batch(SUCCESS, found("00000002"))));

        assertFalse(merged.isSuccessful());
        assertEquals("FAILURE", merged.getRawResultCode());
        assertEquals(3, merged.getUnfilteredSubjects().size());
    }

    private SubjectsResults batch(String rawResultCode, WsSubject... wsSubjects) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(rawResultCode);
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        return new SubjectsResults(wsGetSubjectsResults);
    }

    private WsSubject found(String uhUuid) {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setResultCode(SUCCESS);
        wsSubject.setId(uhUuid);
        wsSubject.setAttributeValues(new String[] { "uid" + uhUuid, "Name", "Last", "First", "" });
        return wsSubject;
    }

    private WsSubject notFound() {
        WsSubject wsSubject = new WsSubject();
        wsSubject.setResultCode(SUBJECT_NOT_FOUND);
        return wsSubject;
    }
}
