package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GroupAttributeServiceTest {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.username}")
    private String USERNAME;

    @Value("${groupings.api.test.name}")
    private String NAME;

    @Value("${groupings.api.test.uhuuid}")
    private String UHUUID;

    @Value("Default description.")
    private String DEFAULT_DESCRIPTION;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.test.sync_destinations}")
    private List<String> SYNC_DESTINATIONS;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    private static final String PATH_ROOT = "path:to:grouping";

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;
    private static final String GROUPING_4_PATH = PATH_ROOT + 4;

    private static final String ADMIN_USER = "admin";
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private Group appGroup = new Group();

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(groupAttributeService);
    }

    @Test
    public void getAllSyncDestinationsTest() {
        List<SyncDestination> syncDestinations =
                groupAttributeService.getAllSyncDestinations(ADMIN_USER, GROUPING_0_PATH);
        assertTrue(syncDestinations.size() > 0);
        assertNotNull(syncDestinations);
        Set<String> names = new HashSet<>();
        for (SyncDestination syncDestination : syncDestinations) {
            assertNotEquals("", syncDestination.getDescription());
            assertNotEquals("", syncDestination.getName());
            assertEquals("", syncDestination.getTooltip());
            assertNotNull(syncDestination);
            // Check for duplicates.
            assertTrue(names.add(syncDestination.getName()));
        }

    }

    @Test
    public void getSyncDestinationsTest() {
        Grouping grouping = new Grouping(GROUPING_0_PATH);
        List<SyncDestination> syncDestinations = groupAttributeService.getSyncDestinations(grouping);
        assertTrue(syncDestinations.size() > 0);
        assertNotNull(syncDestinations);
        Set<String> names = new HashSet<>();
        for (SyncDestination syncDestination : syncDestinations) {
            assertNotEquals("", syncDestination.getDescription());
            assertNotEquals("", syncDestination.getName());
            assertEquals("", syncDestination.getTooltip());
            assertNotNull(syncDestination);
            // Check for duplicates.
            assertTrue(names.add(syncDestination.getName()));
        }
    }

    @Test
    public void changeListservStatusTest() {
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void changeReleasedGroupingStatusTest() {
        String user = users.get(1).getUsername(); // username1

        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void changeOptInStatusTest() {

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void changeOptOutStatusTest() {
        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void optOutPermissionTest() {

        boolean isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_0_PATH, OPT_OUT);

        assertThat(isHasPermission, is(false));

        isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_1_PATH, OPT_OUT);

        assertThat(isHasPermission, is(true));

    }

    @Test
    public void optInPermissionTest() {

        boolean isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_0_PATH, OPT_IN);

        assertThat(isHasPermission, is(true));

        isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_2_PATH, OPT_IN);

        assertThat(isHasPermission, is(false));
    }

    @Test
    public void hasListservTest() {

        boolean isGroupingHasListserv = groupAttributeService.isGroupAttribute(GROUPING_0_PATH, LISTSERV);

        assertThat(isGroupingHasListserv, is(false));

        isGroupingHasListserv = groupAttributeService.isGroupAttribute(GROUPING_3_PATH, LISTSERV);

        assertThat(isGroupingHasListserv, is(true));
    }

    @Test
    public void hasReleasedGroupingTest() {

        groupingRepository.findByPath(GROUPING_0_PATH);

        boolean isHasReleasedGrouping = groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING);
        assertTrue(isHasReleasedGrouping);

        groupingRepository.findByPath(GROUPING_1_PATH);
        isHasReleasedGrouping = groupAttributeService.isGroupAttribute(GROUPING_1_PATH, RELEASED_GROUPING);
        assertFalse(isHasReleasedGrouping);

    }

    @Test
    public void updateDescriptionTest() {

        GroupingsServiceResult groupingsServiceResult;

        //Set the description to the default description
        groupAttributeService.updateDescription(GROUPING_0_PATH, ADMIN_USER, DEFAULT_DESCRIPTION);
        assertThat(DEFAULT_DESCRIPTION,
                containsString(groupingRepository.findByPath(GROUPING_0_PATH).getDescription()));

        //Try to update grouping while user isn't owner or admin
        try {
            groupingsServiceResult = groupAttributeService
                    .updateDescription(GROUPING_0_PATH, users.get(4).getUsername(), DEFAULT_DESCRIPTION + " modified");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //Testing with admin
        groupingsServiceResult = groupAttributeService
                .updateDescription(GROUPING_0_PATH, ADMIN_USER, DEFAULT_DESCRIPTION + " modifiedbyadmin1");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        //Testing with owner
        groupingsServiceResult = groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(),
                DEFAULT_DESCRIPTION + " modifiedbyowner2");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        // Test with empty string
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(), "");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        //Revert any changes
        groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(), DEFAULT_DESCRIPTION);
    }
}


