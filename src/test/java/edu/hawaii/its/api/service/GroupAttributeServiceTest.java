package edu.hawaii.its.api.service;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

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
    private DatabaseSetupService databaseSetupService;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingRepository groupingRepository;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
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
    public void testGetAllSyncDestinationsWithException() {
        try {
            String username = "user0";
            groupAttributeService.getAllSyncDestinations(username, GROUPING_0_PATH);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertTrue(e instanceof AccessDeniedException);
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

        Grouping grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOnWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOnWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOnWhenOnAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOffWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOnWhenOffAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOffWhenOnAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOffWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        GroupingsServiceResult turnOffWhenOffAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(LISTSERV));

        assertTrue(turnOnWhenOnOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOnOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOnAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOnWhenOffOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOffAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOffOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void changeReleasedGroupingStatusTest() {
        List<GroupingsServiceResult> results = new ArrayList<>();
        String user = users.get(1).getUsername(); // username1
        String owner = users.get(0).getUsername(); // username0

        //starts ON
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING));

        // ON to ON
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, true));
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING));

        //ON to OFF
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, false));
        assertTrue(results.get(1).getResultCode().startsWith(SUCCESS));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING));

        // OFF to OFF
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, false));
        assertTrue(results.get(2).getResultCode().startsWith(SUCCESS));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING));

        // OFF to ON
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, true));
        assertTrue(results.get(3).getResultCode().startsWith(SUCCESS));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, RELEASED_GROUPING));
    }

    @Test
    public void changeOptInStatusTest() {

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(SUCCESS));

        assertThat(turnOffWhenOnOwner.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnOwner.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnOwner.get(2).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(2).getResultCode(), is(SUCCESS));

        assertThat(turnOnWhenOffOwner.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffOwner.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffOwner.get(2).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(2).getResultCode(), is(SUCCESS));

        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void changeOptOutStatusTest() {

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(SUCCESS));

        assertThat(turnOffWhenOnOwner.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnOwner.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnOwner.get(2).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOffWhenOnAdmin.get(2).getResultCode(), is(SUCCESS));

        assertThat(turnOnWhenOffOwner.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffOwner.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffOwner.get(2).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(0).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(1).getResultCode(), is(SUCCESS));
        assertThat(turnOnWhenOffAdmin.get(2).getResultCode(), is(SUCCESS));

        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(SUCCESS));
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
