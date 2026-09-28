package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import edu.hawaii.its.api.wrapper.Subject;
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
     * exactly which identifiers are found/not-found. Real Grouper collapses unknown identifiers into one entry;
     * see subjectsResultsLikeGrouper for that shape.
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
        // The Grouper subject behind each valid identifier is returned too, so a caller needing attributes
        // (MemberAttributeService) can use this one lookup instead of querying Grouper again.
        assertEquals(11, result.getValidSubjects().size());
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
    public void validateUhIdentifiersReportsInvalidIdentifiersInSubmittedOrder() {
        // Malformed entries never reach Grouper and unknown ones are only found after the lookup, but the
        // caller (e.g. a file import listing rows it could not add) needs them in the order they were submitted.
        List<String> identifiers = List.of("1234", "12-345-678", "goodUid", "0000000a", "!!!!!!!!");
        List<String> wellFormed = List.of("1234", "goodUid", "0000000a");
        given(grouperService.getSubjects(wellFormed))
                .willReturn(subjectsResultsInOrder(wellFormed, List.of("SUBJECT_NOT_FOUND", "SUCCESS", "SUBJECT_NOT_FOUND")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("uhuuid-goodUid"), result.getValidIdentifiers());
        assertEquals(List.of("1234", "12-345-678", "0000000a", "!!!!!!!!"), result.getInvalidIdentifiers());
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
    public void validateUhIdentifiersDoesNotRequireOneResultPerIdentifier() {
        List<String> identifiers = List.of("uidA", "uidB");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(List.of("uidA"), List.of("SUCCESS")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("uhuuid-uidA"), result.getValidIdentifiers());
        assertEquals(List.of("uidB"), result.getInvalidIdentifiers());
    }

    // The tests below use the response shape real Grouper produces (see TestGrouperApiService.getSubjects):
    // lookups that resolve to nothing are collapsed into a single SUBJECT_NOT_FOUND entry, and the entries are
    // not in request order. A found subject carries its UH number as id and its uid as identifierLookup (when it
    // was looked up by uid) or as the first attribute.

    @Test
    public void validateUhIdentifiersReportsEveryUnknownIdentifierWhenGrouperCollapsesThemIntoOneEntry() {
        List<String> identifiers = List.of("1234", "abcdefgh", "0000000a");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(notFoundEntry()));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertTrue(result.getValidIdentifiers().isEmpty());
        assertEquals(identifiers, result.getInvalidIdentifiers());
        verify(grouperService, never()).getSubjects(anyString());
    }

    @Test
    public void validateUhIdentifiersMatchesFoundSubjectsToIdentifiersWhateverTheResponseOrder() {
        List<String> identifiers = List.of("1234", "00000001", "abcdefgh", "00000002", "0000000a");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(
                        foundByUhNumber("00000002", "uidtwo"),
                        notFoundEntry(),
                        foundByUhNumber("00000001", "uidone")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("00000001", "00000002"), result.getValidIdentifiers());
        assertEquals(List.of("1234", "abcdefgh", "0000000a"), result.getInvalidIdentifiers());
        assertEquals(2, result.getValidSubjects().size());
        assertEquals(List.of("uidone", "uidtwo"),
                result.getValidSubjects().stream().map(Subject::getUid).toList());
        verify(grouperService, never()).getSubjects(anyString());
    }

    @Test
    public void validateUhIdentifiersMatchesSubjectsFoundByUid() {
        List<String> identifiers = List.of("uidone", "nobody");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(foundByUid("00000001", "uidone"), notFoundEntry()));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("00000001"), result.getValidIdentifiers());
        assertEquals(List.of("nobody"), result.getInvalidIdentifiers());
    }

    @Test
    public void validateUhIdentifiersListsASubjectOnceWhenItsUidAndUhNumberAreBothSubmitted() {
        // Grouper answers both lookups with the one subject.
        List<String> identifiers = List.of("uidone", "00000001");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(foundByUid("00000001", "uidone")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("00000001"), result.getValidIdentifiers());
        assertTrue(result.getInvalidIdentifiers().isEmpty());
        // Both identifiers resolved to the same subject, so it's returned once, not twice.
        assertEquals(1, result.getValidSubjects().size());
        verify(grouperService, never()).getSubjects(anyString());
    }

    @Test
    public void validateUhIdentifiersIgnoresCaseWhenMatchingIdentifiers() {
        List<String> identifiers = List.of("UIDONE");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(foundByUhNumber("00000001", "uidone")));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("00000001"), result.getValidIdentifiers());
        assertTrue(result.getInvalidIdentifiers().isEmpty());
    }

    @Test
    public void validateUhIdentifiersVerifiesLeftoverIdentifiersWhenAFoundSubjectMatchesNoneOfThem() {
        // "alias" was resolved by Grouper under a uhUuid and uid that are not the submitted string, so it can't
        // be matched from the bulk response: it is verified with a single lookup instead of being reported unknown.
        List<String> identifiers = List.of("alias", "unknown", "00000001");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsLikeGrouper(
                        foundByUhNumber("00000001", "uidone"),
                        foundByUhNumber("00000009", "uidnine"),
                        notFoundEntry()));
        given(grouperService.getSubjects("alias"))
                .willReturn(subjectsResultsLikeGrouper(foundByUid("00000009", "alias")));
        given(grouperService.getSubjects("unknown"))
                .willReturn(subjectsResultsLikeGrouper(notFoundEntry()));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(TEST_USER, identifiers);

        assertEquals(List.of("00000009", "00000001"), result.getValidIdentifiers());
        assertEquals(List.of("unknown"), result.getInvalidIdentifiers());
        verify(grouperService, times(1)).getSubjects("alias");
        verify(grouperService, times(1)).getSubjects("unknown");
        verify(grouperService, times(1)).getSubjects(identifiers);
    }

    private SubjectsResults subjectsResultsLikeGrouper(WsSubject... entries) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");

        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(entries);
        return new SubjectsResults(wsGetSubjectsResults);
    }

    private WsSubject notFoundEntry() {
        WsSubject subject = new WsSubject();
        subject.setResultCode("SUBJECT_NOT_FOUND");
        return subject;
    }

    /** A subject looked up by UH number: Grouper does not echo an identifier for it. */
    private WsSubject foundByUhNumber(String uhNumber, String uid) {
        WsSubject subject = new WsSubject();
        subject.setResultCode("SUCCESS");
        subject.setId(uhNumber);
        subject.setAttributeValues(new String[] { uid, "Name", "Last", "First", "" });
        return subject;
    }

    /** A subject looked up by uid: Grouper echoes the uid back as the identifier lookup. */
    private WsSubject foundByUid(String uhNumber, String uid) {
        WsSubject subject = foundByUhNumber(uhNumber, uid);
        subject.setIdentifierLookup(uid);
        return subject;
    }

    @Test
    public void validateUhIdentifiersThrowsWhenRawSubjectLookupFails() {
        List<String> identifiers = List.of(TEST_USER);
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResults("FAILURE", "SUBJECT_NOT_FOUND"));

        assertThrows(GrouperException.class, () -> subjectService.validateUhIdentifiers(TEST_USER, identifiers));
    }
}
