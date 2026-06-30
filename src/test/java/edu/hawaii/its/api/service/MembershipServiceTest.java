package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.type.MembershipResult;
import edu.hawaii.its.api.wrapper.Group;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final String CURRENT_USER = "testiwta";
    private static final String UH_UUID = "99997010";

    // The composite grouping path. Grouper itself computes the composite
    // membership as (basis + include) - exclude. A user appears as a member
    // of this path ONLY if Grouper's composite calculation says so.
    private static final String GROUPING_PATH = "tmp:testiwta:testiwta-aux";
    private static final String GROUPING_NAME = "testiwta-aux";
    private static final String GROUPING_DESCRIPTION = "Test grouping description";

    private static final String BASIS_PATH = GROUPING_PATH + ":basis";
    private static final String INCLUDE_PATH = GROUPING_PATH + ":include";
    private static final String EXCLUDE_PATH = GROUPING_PATH + ":exclude";

    @Mock
    private SubjectService subjectService;

    @Mock
    private GroupingsService groupingsService;

    @Mock
    private GroupPathService groupPathService;

    @InjectMocks
    private MembershipService membershipService;

    @Mock
    private Group membershipGrouping;

    @Captor
    private ArgumentCaptor<List<String>> pathsCaptor;

    @BeforeEach
    void setUp() {
        // Nothing global; each test stubs what it needs so Mockito's strict
        // stubbing can flag unused or incorrect interactions.
    }

    /**
     * Make the mocked filterGroupPaths apply the REAL predicate that the
     * service passes in (e.g. PathFilter.onlyGroupingPaths()). This is what
     * lets these tests detect a regression back to manual
     * (basis + include) - exclude arithmetic: membership must be derived
     * solely from composite grouping paths, never reconstructed from
     * basis/include/exclude lists.
     */
    private void stubRealPathFiltering() {
        when(groupingsService.filterGroupPaths(anyList(), any()))
                .thenAnswer(invocation -> {
                    List<String> paths = invocation.getArgument(0);
                    Predicate<String> filter = invocation.getArgument(1);
                    return paths.stream().filter(filter).collect(Collectors.toList());
                });
    }

    @Test
    void membershipResultsThrowsWhenUhIdentifierNotFound() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn("");

        assertThrows(UhIdentifierNotFoundException.class,
                () -> membershipService.membershipResults(CURRENT_USER));

        verify(groupingsService, never()).allGroupPaths(CURRENT_USER);
    }

    /**
     * Core regression test for the pagination bug: membership must come from
     * the composite grouping path that Grouper returns, NOT from manually
     * combining basis/include/exclude lists. Here Grouper reports the user as
     * a member of the composite path, so exactly one membership is produced,
     * and only the composite path is sent to getValidGroupings.
     */
    @Test
    void membershipIsDerivedFromCompositePathOnly() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        // Grouper says: user is in basis, include, AND the composite group.
        when(groupingsService.allGroupPaths(CURRENT_USER))
                .thenReturn(Arrays.asList(GROUPING_PATH, BASIS_PATH, INCLUDE_PATH));
        stubRealPathFiltering();
        when(groupingsService.curatedGroupings()).thenReturn(new ArrayList<>());
        when(membershipGrouping.getGroupPath()).thenReturn(GROUPING_PATH);
        when(membershipGrouping.getDescription()).thenReturn(GROUPING_DESCRIPTION);
        when(groupPathService.getValidGroupings(anyList()))
                .thenReturn(Collections.singletonList(membershipGrouping));
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.emptyList());

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        assertEquals(1, results.getResults().size());
        assertEquals(GROUPING_PATH, results.getResults().get(0).getPath());
        assertEquals(GROUPING_NAME, results.getResults().get(0).getName());

        // Only the composite path — never basis/include/exclude paths — may
        // be used as the source of membership.
        verify(groupPathService).getValidGroupings(pathsCaptor.capture());
        assertEquals(Collections.singletonList(GROUPING_PATH), pathsCaptor.getValue());
    }

    /**
     * The exclusion scenario that broke under pagination: the user is in
     * basis but also in exclude. Grouper's composite calculation therefore
     * does NOT list the user in the composite group, so allGroupPaths
     * contains the basis and exclude paths but NOT the composite path.
     * The service must produce zero memberships — it must not "rediscover"
     * a membership from the basis path.
     */
    @Test
    void excludedUserGetsNoMembershipForThatGrouping() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        // No composite path: Grouper already applied (basis + include) - exclude.
        when(groupingsService.allGroupPaths(CURRENT_USER))
                .thenReturn(Arrays.asList(BASIS_PATH, EXCLUDE_PATH));
        stubRealPathFiltering();
        when(groupingsService.curatedGroupings()).thenReturn(new ArrayList<>());
        when(groupPathService.getValidGroupings(Collections.emptyList()))
                .thenReturn(Collections.emptyList());
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.emptyList());

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        assertNotNull(results);
        assertTrue(results.getResults().isEmpty(),
                "An excluded user must not be reported as a member; membership "
                        + "must come from Grouper's composite group, not from basis/include lists.");
    }

    @Test
    void membershipResultsSetsOptOutEnabledFlag() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        when(groupingsService.allGroupPaths(CURRENT_USER))
                .thenReturn(Arrays.asList(GROUPING_PATH, BASIS_PATH, INCLUDE_PATH));
        stubRealPathFiltering();
        when(groupingsService.curatedGroupings()).thenReturn(new ArrayList<>());
        when(membershipGrouping.getGroupPath()).thenReturn(GROUPING_PATH);
        when(membershipGrouping.getDescription()).thenReturn(GROUPING_DESCRIPTION);
        when(groupPathService.getValidGroupings(anyList()))
                .thenReturn(Collections.singletonList(membershipGrouping));
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.singletonList(GROUPING_PATH));

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        assertTrue(results.getResults().get(0).isOptOutEnabled());
    }

    @Test
    void membershipResultsOptOutDisabledWhenNotInOptOutList() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        when(groupingsService.allGroupPaths(CURRENT_USER))
                .thenReturn(Arrays.asList(GROUPING_PATH, BASIS_PATH, INCLUDE_PATH));
        stubRealPathFiltering();
        when(groupingsService.curatedGroupings()).thenReturn(new ArrayList<>());
        when(membershipGrouping.getGroupPath()).thenReturn(GROUPING_PATH);
        when(membershipGrouping.getDescription()).thenReturn(GROUPING_DESCRIPTION);
        when(groupPathService.getValidGroupings(anyList()))
                .thenReturn(Collections.singletonList(membershipGrouping));
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.emptyList());

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        assertFalse(results.getResults().get(0).isOptOutEnabled());
    }

    @Test
    void membershipResultsIncludesCuratedGroupingsUserBelongsTo() {
        String otherCuratedPath = "tmp:other:other-curated"; // user is NOT a member

        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        when(groupingsService.allGroupPaths(CURRENT_USER))
                .thenReturn(Collections.singletonList(GROUPING_PATH));
        stubRealPathFiltering();
        // Mutable list required: the service calls retainAll() on it.
        when(groupingsService.curatedGroupings())
                .thenReturn(new ArrayList<>(Arrays.asList(GROUPING_PATH, otherCuratedPath)));
        when(membershipGrouping.getGroupPath()).thenReturn(GROUPING_PATH);
        when(membershipGrouping.getDescription()).thenReturn(GROUPING_DESCRIPTION);
        when(groupPathService.getValidGroupings(anyList()))
                .thenReturn(Collections.singletonList(membershipGrouping));
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.emptyList());

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        // One composite membership + one curated entry the user belongs to;
        // the curated grouping the user is NOT in was removed by retainAll().
        assertEquals(2, results.getResults().size());
        MembershipResult curated = results.getResults().get(1);
        assertEquals(GROUPING_PATH, curated.getPath());
        assertEquals("", curated.getDescription());
    }

    @Test
    void membershipResultsReturnsEmptyWhenUserHasNoGroupPaths() {
        when(subjectService.getValidUhUuid(CURRENT_USER, CURRENT_USER)).thenReturn(UH_UUID);
        when(groupingsService.allGroupPaths(CURRENT_USER)).thenReturn(Collections.emptyList());
        stubRealPathFiltering();
        when(groupingsService.curatedGroupings()).thenReturn(new ArrayList<>());
        when(groupPathService.getValidGroupings(Collections.emptyList()))
                .thenReturn(Collections.emptyList());
        when(groupingsService.optOutEnabledGroupingPaths(anyList()))
                .thenReturn(Collections.emptyList());

        MembershipResults results = membershipService.membershipResults(CURRENT_USER);

        assertTrue(results.getResults().isEmpty());
    }
}