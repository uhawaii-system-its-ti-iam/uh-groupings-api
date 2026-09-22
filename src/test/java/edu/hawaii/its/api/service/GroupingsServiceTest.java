package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupingsServiceTest {

    private GrouperService grouperService;
    private GroupingsService groupingsService;

    @BeforeEach
    void setUp() {
        grouperService = mock(GrouperService.class);
        groupingsService = new GroupingsService(mock(GroupPathService.class), grouperService);
        ReflectionTestUtils.setField(groupingsService, "TRIO", "trio");
        ReflectionTestUtils.setField(groupingsService, "groupingCatalogTtlMillis", 60_000L);
    }

    @Test
    void paginatedCatalogCachesAndFiltersByPathAndDescription() {
        when(grouperService.groupAttributeResults("trio")).thenReturn(catalog(
                group("apps:test1", "description-test1"),
                group("apps:test2", "description-test2"),
                group("apps:test3", "description-test3")));

        GroupingPaths firstPage = groupingsService.paginatedAdminGroupingPaths(1, 1, "apps:");
        GroupingPaths secondPage = groupingsService.paginatedAdminGroupingPaths(2, 1, "apps:");
        GroupingPaths descriptionMatch = groupingsService.paginatedAdminGroupingPaths(1, 1, "description-test3");

        assertEquals(3, firstPage.getTotalCount());
        assertEquals("apps:test1", firstPage.getGroupingPaths().getFirst().getPath());
        assertEquals("apps:test2", secondPage.getGroupingPaths().getFirst().getPath());
        assertEquals("apps:test3", descriptionMatch.getGroupingPaths().getFirst().getPath());
        verify(grouperService, times(1)).groupAttributeResults("trio");
    }

    @Test
    void failedRefreshRetainsThePreviousCatalog() {
        when(grouperService.groupAttributeResults("trio"))
                .thenReturn(catalog(group("apps:test1", "test1")))
                .thenThrow(new RuntimeException("Grouper unavailable"));

        ReflectionTestUtils.setField(groupingsService, "groupingCatalogPreload", true);

        groupingsService.refreshGroupingCatalog();
        groupingsService.refreshGroupingCatalog();

        GroupingPaths result = groupingsService.paginatedAdminGroupingPaths(1, 25, null);

        assertEquals(1, result.getTotalCount());
        assertEquals("apps:test1", result.getGroupingPaths().getFirst().getPath());

        verify(grouperService, times(2))
                .groupAttributeResults("trio");
    }

    @Test
    void paginatedCatalogHandlesEmptyPagesBlankSearchAndInvalidBounds() {
        when(grouperService.groupAttributeResults("trio"))
                .thenReturn(catalog(group("apps:test1", "description-test1")));

        GroupingPaths beyondFinalPage = groupingsService.paginatedAdminGroupingPaths(2, 1, "   ");

        assertEquals(1, beyondFinalPage.getTotalCount());
        assertEquals(0, beyondFinalPage.getGroupingPaths().size());
        assertThrows(IllegalArgumentException.class,
                () -> groupingsService.paginatedAdminGroupingPaths(0, 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> groupingsService.paginatedAdminGroupingPaths(1, 0, null));
        assertThrows(IllegalArgumentException.class,
                () -> groupingsService.paginatedAdminGroupingPaths(1, 101, null));
    }

    @Test
    void invalidatingTheCatalogForcesTheNextRequestToReloadIt() {
        when(grouperService.groupAttributeResults("trio"))
                .thenReturn(catalog(group("apps:test1", "description-test1")));

        groupingsService.paginatedAdminGroupingPaths(1, 25, null);
        groupingsService.invalidateGroupingCatalog();
        groupingsService.paginatedAdminGroupingPaths(1, 25, null);

        verify(grouperService, times(2)).groupAttributeResults("trio");
    }

    private GroupAttributeResults catalog(WsGroup... groups) {
        WsGetAttributeAssignmentsResults results = new WsGetAttributeAssignmentsResults();
        results.setWsGroups(groups);
        GroupAttributeResults parsed = new GroupAttributeResults(results);
        assertEquals(groups.length, parsed.getGroups().size());
        return parsed;
    }

    private WsGroup group(String path, String description) {
        WsGroup group = new WsGroup();
        group.setName(path);
        group.setDescription(description);
        return group;
    }
}
