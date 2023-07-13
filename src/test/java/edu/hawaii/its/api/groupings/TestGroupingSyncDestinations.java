package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingSyncDestinations {
    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;
    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.google.sync.dest.suffix}")
    private String googleSyncDestSuffix;

    @Value("#{T(java.util.regex.Pattern).compile('${groupings.api.sync.dest.regex}')}")
    private Pattern regex;
    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void constructor() {
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                SYNC_DESTINATIONS_CHECKBOXES,
                SYNC_DESTINATIONS_LOCATION);
        GroupAttributeResults groupAttributeResults = grouperApiService.groupAttributeResults(
                findAttributesResults.getResults().stream().map(AttributesResult::getName).collect(Collectors.toList()),
                GROUPING);
        GroupingSyncDestinations groupingSyncDestinations = new GroupingSyncDestinations(
                findAttributesResults,
                groupAttributeResults, new KeyParser(googleSyncDestSuffix, regex));
        assertNotNull(groupingSyncDestinations);
        groupingSyncDestinations = new GroupingSyncDestinations();
        assertNotNull(groupingSyncDestinations);
    }

    @Test
    public void success() {
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                SYNC_DESTINATIONS_CHECKBOXES,
                SYNC_DESTINATIONS_LOCATION);
        GroupAttributeResults groupAttributeResults = grouperApiService.groupAttributeResults(
                findAttributesResults.getResults().stream().map(AttributesResult::getName).collect(Collectors.toList()),
                GROUPING);
        GroupingSyncDestinations groupingSyncDestinations = new GroupingSyncDestinations(
                findAttributesResults,
                groupAttributeResults, new KeyParser(googleSyncDestSuffix, regex));

        assertNotNull(groupingSyncDestinations);
        assertEquals("SUCCESS", groupingSyncDestinations.getResultCode());
        List<GroupingSyncDestination> syncDestinations = groupingSyncDestinations.getSyncDestinations();
        assertNotNull(syncDestinations);
        assertFalse(syncDestinations.isEmpty());
        assertTrue(syncDestinations.stream().allMatch(Objects::nonNull));
        assertTrue(syncDestinations.stream().map(GroupingSyncDestination::getName).allMatch(Objects::nonNull));

        assertEquals(syncDestinations.stream().sorted(Comparator.comparing(GroupingSyncDestination::getName)).map(GroupingSyncDestination::getName).collect(Collectors.toList()),
                findAttributesResults.getResults().stream().map(AttributesResult::getName)
                        .collect(Collectors.toList()));

        assertTrue(syncDestinations.stream().map(GroupingSyncDestination::getDescription).allMatch(Objects::nonNull));
        assertTrue(syncDestinations.stream().map(GroupingSyncDestination::getTooltip).allMatch(Objects::nonNull));
        assertTrue(syncDestinations.stream().map(GroupingSyncDestination::getSynced).allMatch(Objects::nonNull));
    }

    @Test
    public void failure() {
        GroupingSyncDestinations groupingSyncDestinations =
                new GroupingSyncDestinations(new FindAttributesResults(new WsFindAttributeDefNamesResults()),
                        new GroupAttributeResults(new WsGetAttributeAssignmentsResults()),
                        new KeyParser(googleSyncDestSuffix, regex));
        assertNotNull(groupingSyncDestinations);
        assertEquals("FAILURE", groupingSyncDestinations.getResultCode());
        assertTrue(groupingSyncDestinations.getSyncDestinations().isEmpty());
    }
}
