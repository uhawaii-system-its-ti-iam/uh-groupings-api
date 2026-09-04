package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.type.UhIdentifierValidationResult;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SubjectServiceTest {

    private static final String TEST_USER = "testiwta";

    @Autowired
    private SubjectService subjectService;

    @MockitoBean
    private GrouperService grouperService;

    @Test
    public void getValidUhUuidPropagatesGrouperException() {
        given(grouperService.getSubjects(TEST_USER))
                .willThrow(new GrouperException("Grouper unavailable"));

        assertThrows(GrouperException.class, () -> subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    @Test
    public void getValidUhUuidReturnsEmptyForSubjectNotFound() {
        given(grouperService.getSubjects(TEST_USER))
                .willReturn(subjectsResults("SUCCESS", "SUBJECT_NOT_FOUND"));

        assertEquals("", subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    @Test
    public void getValidUhUuidThrowsGrouperExceptionWhenRawSubjectLookupFails() {
        given(grouperService.getSubjects(TEST_USER))
                .willReturn(subjectsResults("FAILURE", "SUBJECT_NOT_FOUND"));

        assertThrows(GrouperException.class, () -> subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    @Test
    public void getValidUhUuidsThrowsGrouperExceptionWhenRawSubjectLookupFails() {
        given(grouperService.getSubjects(java.util.List.of(TEST_USER)))
                .willReturn(subjectsResults("FAILURE", "SUBJECT_NOT_FOUND"));

        assertThrows(GrouperException.class,
                () -> subjectService.getValidUhUuids(TEST_USER, java.util.List.of(TEST_USER)));
    }
    private SubjectsResults subjectsResults(String rawResultCode, String subjectResultCode) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(rawResultCode);

        WsSubject subject = new WsSubject();
        subject.setResultCode(subjectResultCode);
        subject.setIdentifierLookup(TEST_USER);

        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(new WsSubject[] { subject });

        return new SubjectsResults(wsGetSubjectsResults);
    }

    /**
     * Builds a SubjectsResults with one WsSubject per identifier, in the same order, so tests can control
     * exactly which positions are found/not-found - mirroring the request/response correlation that
     * SubjectsResults.getSubjectsInRequestOrder() relies on.
     */
    private SubjectsResults subjectsResultsInOrder(List<String> identifiers, List<String> resultCodes) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");

        WsSubject[] wsSubjects = new WsSubject[identifiers.size()];
        for (int i = 0; i < identifiers.size(); i++) {
            WsSubject subject = new WsSubject();
            subject.setResultCode(resultCodes.get(i));
            subject.setIdentifierLookup(identifiers.get(i));
            if (resultCodes.get(i).equals("SUCCESS")) {
                subject.setId("uhuuid-" + identifiers.get(i));
            }
            wsSubjects[i] = subject;
        }

        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);

        return new SubjectsResults(wsGetSubjectsResults);
    }

    private List<String> elevenIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            identifiers.add(String.format("uid%02d", i));
        }
        return identifiers;
    }

    @Test
    public void validateUhIdentifiersWithNoBadEntries() {
        List<String> identifiers = elevenIdentifiers();
        List<String> resultCodes = identifiers.stream().map(id -> "SUCCESS").toList();
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(identifiers, resultCodes));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(11, result.getValidIdentifiers().size());
        assertTrue(result.getInvalidIdentifiers().isEmpty());
    }

    @Test
    public void validateUhIdentifiersWithTenBadEntries() {
        List<String> identifiers = elevenIdentifiers();
        List<String> resultCodes = new ArrayList<>();
        resultCodes.add("SUCCESS");
        for (int i = 1; i < 11; i++) {
            resultCodes.add("SUBJECT_NOT_FOUND");
        }
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(identifiers, resultCodes));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(1, result.getValidIdentifiers().size());
        assertEquals(10, result.getInvalidIdentifiers().size());
        assertEquals(identifiers.subList(1, 11), result.getInvalidIdentifiers());
    }

    @Test
    public void validateUhIdentifiersWithElevenBadEntries() {
        List<String> identifiers = elevenIdentifiers();
        List<String> resultCodes = identifiers.stream().map(id -> "SUBJECT_NOT_FOUND").toList();
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(identifiers, resultCodes));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertTrue(result.getValidIdentifiers().isEmpty());
        assertEquals(11, result.getInvalidIdentifiers().size());
        assertEquals(identifiers, result.getInvalidIdentifiers());
    }

    @Test
    public void validateUhIdentifiersReportsMalformedEntriesWithoutQueryingGrouper() {
        List<String> identifiers = List.of("goodUid", "bad uid with spaces");
        given(grouperService.getSubjects(List.of("goodUid")))
                .willReturn(subjectsResultsInOrder(List.of("goodUid"), List.of("SUCCESS")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(1, result.getValidIdentifiers().size());
        assertEquals(List.of("bad uid with spaces"), result.getInvalidIdentifiers());
    }

    @Test
    public void validateUhIdentifiersFallsBackToOriginalIdentifierWhenSubjectHasNoUhUuid() {
        // Julio's warning: a subject can resolve successfully in Grouper without carrying a uhUuid, so the
        // original submitted identifier - not an empty/derived uhUuid - must be used for the add.
        List<String> identifiers = List.of("serviceAccountUid");
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        WsSubject subject = new WsSubject();
        subject.setResultCode("SUCCESS");
        subject.setIdentifierLookup("serviceAccountUid");
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(new WsSubject[] { subject });
        given(grouperService.getSubjects(identifiers)).willReturn(new SubjectsResults(wsGetSubjectsResults));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("serviceAccountUid"), result.getValidIdentifiers());
    }

    @Test
    public void validateUhIdentifiersDeduplicatesRepeatedIdentifiers() {
        List<String> identifiers = List.of("dup", "dup", "other");
        given(grouperService.getSubjects(List.of("dup", "other")))
                .willReturn(subjectsResultsInOrder(List.of("dup", "other"), List.of("SUCCESS", "SUCCESS")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(2, result.getValidIdentifiers().size());
    }

    @Test
    public void validateUhIdentifiersThrowsWhenGrouperReturnsUnexpectedResultCount() {
        List<String> identifiers = List.of("uidA", "uidB");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(List.of("uidA"), List.of("SUCCESS")));

        assertThrows(GrouperException.class, () -> subjectService.validateUhIdentifiers(TEST_USER, identifiers));
    }

    @Test
    public void validateUhIdentifiersThrowsWhenRawSubjectLookupFails() {
        List<String> identifiers = List.of(TEST_USER);
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResults("FAILURE", "SUBJECT_NOT_FOUND"));

        assertThrows(GrouperException.class, () -> subjectService.validateUhIdentifiers(TEST_USER, identifiers));
    }
}
