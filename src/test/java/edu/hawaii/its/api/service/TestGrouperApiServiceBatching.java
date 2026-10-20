package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.UhIdentifierValidationResult;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * Checks, against Grouper, the answers that bulk imports rely on (see "Bulk Request Batching" in AGENTS.md). Every
 * request in this class only reads from Grouper. With a batch size of 2, a handful of identifiers spans several
 * Grouper requests.
 */
@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class },
        properties = { "groupings.api.grouper.lookup-batch-size=2" })
public class TestGrouperApiServiceBatching {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouper.person-source-id}")
    private String PERSON_SOURCE_ID;

    // Lists people and groups.
    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUP_PATH;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private SubjectService subjectService;

    @Test
    public void validateUhIdentifiersMatchesTheAnswersOfSeveralBatches() {
        // Sent as [uid, unknown] [UH number, unknown] [uid]: each batch answers its unknown identifier with its own
        // SUBJECT_NOT_FOUND entry, which identifies nothing.
        List<String> uhIdentifiers = List.of(TEST_UIDS.get(0), "zzzunknown1", TEST_UH_UUIDS.get(1), "zzzunknown2",
                TEST_UIDS.get(2));

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(ADMIN, uhIdentifiers);

        assertEquals(TEST_UH_UUIDS.subList(0, 3), result.getValidIdentifiers());
        assertEquals(List.of("zzzunknown1", "zzzunknown2"), result.getInvalidIdentifiers());
    }

    @Test
    public void aLookupInWhichNothingResolvesSucceeds() {
        // An import can hold a whole batch of bad rows. The lookup of that batch must not fail the import.
        SubjectsResults subjectsResults = grouperService.getSubjects(List.of("zzzunknown1", "zzzunknown2",
                "zzzunknown3"));

        assertTrue(subjectsResults.isSuccessful());
        assertTrue(subjectsResults.getUnfilteredSubjects().stream()
                .allMatch(subject -> subject.getResultCode().equals("SUBJECT_NOT_FOUND")));
    }

    @Test
    public void aLookupInThePersonSourceInWhichNothingResolvesSucceeds() {
        // Validation looks every identifier up in the person source first, so a batch of bad rows must not fail there.
        SubjectsResults subjectsResults = grouperService.getSubjects(List.of("zzzunknown1", "zzzunknown2",
                "zzzunknown3"), PERSON_SOURCE_ID, count -> {
        });

        assertTrue(subjectsResults.isSuccessful());
        assertTrue(subjectsResults.getUnfilteredSubjects().stream()
                .allMatch(subject -> subject.getResultCode().equals("SUBJECT_NOT_FOUND")));
    }

    @Test
    public void validateUhIdentifiersReportsEveryIdentifierOfAListInWhichNothingResolves() {
        List<String> uhIdentifiers = List.of("zzzunknown1", "zzzunknown2", "zzzunknown3");

        UhIdentifierValidationResult result = subjectService.validateUhIdentifiers(ADMIN, uhIdentifiers);

        assertTrue(result.getValidIdentifiers().isEmpty());
        assertEquals(uhIdentifiers, result.getInvalidIdentifiers());
    }

    @Test
    public void aLookupInThePersonSourceFindsTheSameSubjectsAsALookupInEverySource() {
        // Validation looks identifiers up in the person source first, because Grouper answers that several times
        // faster than a lookup in every source.
        List<String> uhIdentifiers = List.of(TEST_UIDS.get(0), TEST_UH_UUIDS.get(1), TEST_UIDS.get(2));

        List<String> inEverySource = describe(grouperService.getSubjects(uhIdentifiers));
        List<String> inPersonSource = describe(grouperService.getSubjects(uhIdentifiers, PERSON_SOURCE_ID, count -> {
        }));

        assertEquals(3, inPersonSource.size());
        assertEquals(inEverySource, inPersonSource);
    }

    @Test
    public void validateUhIdentifiersReportsTheSubjectSourceOfEachValidIdentifier() {
        UhIdentifierValidationResult result =
                subjectService.validateUhIdentifiers(ADMIN, List.of(TEST_UIDS.get(0), "zzzunknown1"));

        assertEquals(List.of(TEST_UH_UUIDS.get(0)), result.getValidIdentifiers());
        assertEquals(List.of("zzzunknown1"), result.getInvalidIdentifiers());
        assertEquals(Map.of(TEST_UH_UUIDS.get(0), PERSON_SOURCE_ID), result.getSubjectSourceIds());
    }

    @Test
    public void immediateMembersWithoutSubjectDetailStillCarryTheirUhNumbers() {
        // UpdateMemberService.moveGroupMembers decides which members to remove from the member ids of this list.
        List<String> withDetail = sortedIds(grouperService.getImmediateMembers(ADMIN, GROUP_PATH, true));
        List<String> withoutDetail = sortedIds(grouperService.getImmediateMembers(ADMIN, GROUP_PATH, false));

        assertFalse(withDetail.isEmpty());
        assertEquals(withDetail, withoutDetail);
    }

    private List<String> sortedIds(GetMembersResult getMembersResult) {
        return getMembersResult.getSubjects().stream().map(Subject::getUhUuid).sorted().toList();
    }

    /**
     * The found subjects of a lookup, each as its UH number, uid, name and subject source, in UH number order.
     */
    private List<String> describe(SubjectsResults subjectsResults) {
        return subjectsResults.getSubjects().stream()
                .map(subject -> String.join("|", subject.getUhUuid(), subject.getUid(), subject.getName(),
                        subject.getSourceId()))
                .sorted()
                .toList();
    }
}
