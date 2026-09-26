package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 * Runs the UH numbers of the CSV files used to test the UI's file import (lt10/eq10/gt10 rows; all bad, all good,
 * mixed; with and without repeated UH numbers) through the calls the UI makes for an import: first every
 * identifier is checked (POST members/async), then only the valid ones are added to the include and exclude lists
 * (PUT include-members/async, exclude-members/async).
 * <p>
 * Grouper is faked: only the UH numbers 00000001 - 00000012 exist. Every other identifier is unknown to Grouper
 * (like "1234" or "abcdefgh") or malformed (like "12-345-678"), and must be reported back as invalid, never
 * fail the request.
 */
@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberImportFileTest {

    private static final String ADMIN = "testiwta";
    private static final String GROUPING = "group-path";

    private static final Set<String> KNOWN_UH_NUMBERS = Set.copyOf(uhNumbers(12));

    /**
     * The invalid UH numbers used by the CSV files, in the order the files use them. Every file that has invalid
     * entries uses a leading run of this list.
     */
    private static final List<String> INVALID_UH_NUMBERS = List.of("1234", "abcdefgh", "0000000A", "99999999999",
            "UH00001", "12-345-678", "!!!!!!!!", "000000", "true", "0000000", "999999990", "notanumber");

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @BeforeEach
    public void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                ADMIN, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        SecurityContextHolder.setContext(context);

        given(grouperService.getSubjects(anyList()))
                .willAnswer(invocation -> subjectsResultsFor(invocation.getArgument(0)));
        given(grouperService.getSubjects(anyString()))
                .willAnswer(invocation -> subjectsResultsFor(List.of(invocation.<String>getArgument(0))));
        given(grouperService.findGroupsResults(GROUPING))
                .willReturn(groupingsTestConfiguration.findGroupsResultsDescriptionTestData());
        given(grouperService.hasMemberResults(anyString(), anyString()))
                .willReturn(groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData());
        given(grouperService.addMembers(anyString(), anyString(), anyList()))
                .willReturn(groupingsTestConfiguration.addMemberResultsSuccessTestData());
        given(grouperService.removeMembers(anyString(), anyString(), anyList()))
                .willReturn(groupingsTestConfiguration.deleteMemberResultsSuccessTestData());
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("importFiles")
    public void importFileRunsThroughTheApi(String fileName, List<String> uhNumbers, List<String> expectedInvalid,
            List<String> expectedAdded) {
        // Step 1: check every identifier in the file.
        MemberAttributeResults validation = memberAttributeService.getMemberAttributeResultsAsync(ADMIN, uhNumbers).join();
        assertEquals(expectedInvalid, validation.getInvalid());
        assertEquals(expectedInvalid, memberAttributeService.getMemberAttributeResults(ADMIN, uhNumbers).getInvalid());

        // Step 2: add only the valid identifiers. With none, the UI skips the add and just reports the failures.
        List<String> valid = uhNumbers.stream().filter(uhNumber -> !validation.getInvalid().contains(uhNumber)).toList();
        if (expectedAdded.isEmpty()) {
            assertTrue(valid.isEmpty());
            return;
        }

        GroupingMoveMembersResult includeResult =
                updateMemberService.addIncludeMembersAsync(ADMIN, GROUPING, valid).join();
        assertEquals("SUCCESS", includeResult.getResultCode());
        assertTrue(includeResult.getInvalidUhIdentifiers().isEmpty());

        GroupingMoveMembersResult excludeResult =
                updateMemberService.addExcludeMembersAsync(ADMIN, GROUPING, valid).join();
        assertEquals("SUCCESS", excludeResult.getResultCode());
        assertTrue(excludeResult.getInvalidUhIdentifiers().isEmpty());

        // A UH number repeated in the file is only added once.
        verify(grouperService, times(1)).addMembers(ADMIN, GROUPING + ":include", expectedAdded);
        verify(grouperService, times(1)).removeMembers(ADMIN, GROUPING + ":exclude", expectedAdded);
        verify(grouperService, times(1)).addMembers(ADMIN, GROUPING + ":exclude", expectedAdded);
        verify(grouperService, times(1)).removeMembers(ADMIN, GROUPING + ":include", expectedAdded);
    }

    /**
     * One case per CSV file: file name, the UH numbers in the file (header row excluded), the invalid ones the API
     * must report (each once, in file order), and the UH numbers that get added (each once).
     */
    private static Stream<Arguments> importFiles() {
        List<Arguments> cases = new ArrayList<>();
        //                rows, good in mixed file, bad in mixed file, bad in mixed file without repeats
        addCases(cases, "lt10", 7, 3, 3, 4);
        addCases(cases, "eq10", 10, 5, 4, 5);
        addCases(cases, "gt10", 12, 6, 5, 6);
        return cases.stream();
    }

    private static void addCases(List<Arguments> cases, String size, int rows, int mixedGood, int mixedBad,
            int mixedNoRepeatsBad) {
        List<String> noInvalid = List.of();

        cases.add(Arguments.of(size + "_all_bad.csv",
                INVALID_UH_NUMBERS.subList(0, rows),
                INVALID_UH_NUMBERS.subList(0, rows),
                noInvalid));

        cases.add(Arguments.of(size + "_all_good_no_repeats.csv",
                uhNumbers(rows),
                noInvalid,
                uhNumbers(rows)));

        // The last two rows repeat the first two UH numbers.
        List<String> allGoodWithRepeats = new ArrayList<>(uhNumbers(rows - 2));
        allGoodWithRepeats.addAll(uhNumbers(2));
        cases.add(Arguments.of(size + "_all_good_with_repeats.csv",
                allGoodWithRepeats,
                noInvalid,
                uhNumbers(rows - 2)));

        // The first UH number is repeated after the good ones, followed by the invalid ones.
        List<String> mixedGoodRepeats = new ArrayList<>(uhNumbers(mixedGood));
        mixedGoodRepeats.add(uhNumbers(1).get(0));
        mixedGoodRepeats.addAll(INVALID_UH_NUMBERS.subList(0, mixedBad));
        cases.add(Arguments.of(size + "_mix_good_repeats.csv",
                mixedGoodRepeats,
                INVALID_UH_NUMBERS.subList(0, mixedBad),
                uhNumbers(mixedGood)));

        List<String> mixedNoRepeats = new ArrayList<>(uhNumbers(mixedGood));
        mixedNoRepeats.addAll(INVALID_UH_NUMBERS.subList(0, mixedNoRepeatsBad));
        cases.add(Arguments.of(size + "_mix_no_good_repeats.csv",
                mixedNoRepeats,
                INVALID_UH_NUMBERS.subList(0, mixedNoRepeatsBad),
                uhNumbers(mixedGood)));
    }

    /** The first count UH numbers: 00000001, 00000002, ... */
    private static List<String> uhNumbers(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> String.format("%08d", i)).toList();
    }

    /** A Grouper subject lookup: one result per identifier, in request order. */
    private static SubjectsResults subjectsResultsFor(List<String> identifiers) {
        WsSubject[] wsSubjects = new WsSubject[identifiers.size()];
        for (int i = 0; i < identifiers.size(); i++) {
            String identifier = identifiers.get(i);
            WsSubject subject = new WsSubject();
            subject.setIdentifierLookup(identifier);
            if (KNOWN_UH_NUMBERS.contains(identifier)) {
                subject.setResultCode("SUCCESS");
                subject.setId(identifier);
                subject.setAttributeValues(new String[] { "user" + identifier, "Name", "Last", "First", "email" });
            } else {
                subject.setResultCode("SUBJECT_NOT_FOUND");
            }
            wsSubjects[i] = subject;
        }

        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");
        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        return new SubjectsResults(wsGetSubjectsResults);
    }
}
