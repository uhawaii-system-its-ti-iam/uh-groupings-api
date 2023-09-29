package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.type.Announcements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.ServiceTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static edu.hawaii.its.api.service.PathFilter.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsService extends ServiceTest {
    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Autowired
    private GroupingsService groupingsService;
    private static String UH_UUID;

    @BeforeEach
    public void init() {
        UH_UUID = TEST_UH_NUMBERS.get(0);
    }

    @Test
    public void groupingPaths() {
        List<String> result = groupingsService.groupingPaths();
        assertFalse(containsDuplicates(result));
    }

    @Test
    public void optOutEnabledGroupingPaths() {
        List<String> result = groupingsService.optOutEnabledGroupingPaths();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(containsDuplicates(result));

        result = groupingsService.optOutEnabledGroupingPaths(groupingsService.ownedGroupingPaths(UH_UUID));
        assertFalse(containsDuplicates(result));
    }

    @Test
    public void allOptInGroupingPaths() {
        List<String> result = groupingsService.optInEnabledGroupingPaths();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(containsDuplicates(result));

        result = groupingsService.optInEnabledGroupingPaths(groupingsService.ownedGroupingPaths(UH_UUID));
        assertFalse(containsDuplicates(result));
    }

    @Test
    public void getGroupingPaths() {
        List<GroupingPath> groupingPaths = groupingsService.getGroupingPaths(
                Arrays.asList(GROUPING, GROUPING_EXCLUDE, GROUPING_INCLUDE, GROUPING_OWNERS));
        assertEquals(1, groupingPaths.size());
        assertTrue(groupingPaths.stream().allMatch(
                groupingPath -> groupingPath.getPath().equals(GROUPING) && groupingPath.getDescription() != null));
    }

    @Test
    public void ownedGroupingPaths() {
        List<String> results = groupingsService.ownedGroupingPaths(UH_UUID);
        assertTrue(results.stream().allMatch(onlyGroupingPaths()));
        assertFalse(containsDuplicates(results));
    }

    @Test
    public void filterGroupPaths() {
        List<String> ownerAndBasis = groupingsService.groupPaths(ADMIN, pathHasBasis().or(pathHasOwner()));
        List<String> owner = groupingsService.filterGroupPaths(ownerAndBasis, pathHasOwner());
        List<String> basis = groupingsService.filterGroupPaths(ownerAndBasis, pathHasBasis());
        for (String path : owner) {
            assertTrue(pathHasOwner().test(path));
        }
        for (String path : basis) {
            assertTrue(pathHasBasis().test(path));
        }
    }

    @Test
    public void getGroupingDescription() {
        assertEquals("", groupingsService.getGroupingDescription("bad-path"));
        assertEquals("", groupingsService.getGroupingDescription(GROUPING_INCLUDE));
        assertNotNull(groupingsService.getGroupingDescription(GROUPING));
    }

    @Test
    public void updateGroupingDescription() {
        String updatedDescription = groupingsService.getGroupingDescription(GROUPING);
        String description = "abcdefghifklmnopqrstuvwxyz!@##$%%45234543";
        GroupingUpdateDescriptionResult result = groupingsService.updateGroupingDescription(GROUPING, description);
        assertEquals("SUCCESS_UPDATED", result.getResultCode());
        assertEquals(updatedDescription, result.getUpdatedDescription());
        assertEquals(description, result.getCurrentDescription());
        assertEquals(GROUPING, result.getGroupPath());

        result = groupingsService.updateGroupingDescription(GROUPING, description);
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", result.getResultCode());
        assertEquals(description, result.getUpdatedDescription());
        assertEquals(description, result.getCurrentDescription());

        result = groupingsService.updateGroupingDescription(GROUPING, updatedDescription);
        assertEquals("SUCCESS_UPDATED", result.getResultCode());
        assertEquals(updatedDescription, result.getCurrentDescription());
        assertEquals(description, result.getUpdatedDescription());
    }
}
