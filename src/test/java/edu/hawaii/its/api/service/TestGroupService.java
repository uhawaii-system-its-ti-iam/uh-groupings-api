package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.ServiceTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static edu.hawaii.its.api.service.PathFilter.onlyGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupService extends ServiceTest {
    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;
    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;
    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;
    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_UH_NUMBERS;

    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private UpdateMemberService updateMemberService;

    private static String UH_UUID;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

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

}
