package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

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
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @Value("${groupings.api.test.uuid}")
    private String UUID;

    @Value("Default description.")
    private String DEFAULT_DESCRIPTION;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.test.sync_destinations}")
    private List<String> SYNC_DESTINATIONS;

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
    public void getSyncDestinationsTest() {
        assertEquals(SYNC_DESTINATIONS, groupAttributeService.getAllSyncDestinations(ADMIN_USER));
        assertEquals(SYNC_DESTINATIONS, groupAttributeService.getAllSyncDestinations(users.get(0).getUsername()));

        try {
            groupAttributeService.getAllSyncDestinations(users.get(6).getUsername());
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
    }

    @Test
    public void changeListservStatusTest() {

        Grouping grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOnAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOffAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOnAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(), LISTSERV, false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(), LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOffAdmin =
                groupAttributeService.changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, LISTSERV, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

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
        assertTrue(groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // ON to ON
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, true));
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH));

        //ON to OFF
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, false));
        assertTrue(results.get(1).getResultCode().startsWith(SUCCESS));
        assertFalse(groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // OFF to OFF
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, false));
        assertTrue(results.get(2).getResultCode().startsWith(SUCCESS));
        assertFalse(groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // OFF to ON
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, user, RELEASED_GROUPING, true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        results.add(groupAttributeService.changeGroupAttributeStatus(GROUPING_0_PATH, owner, RELEASED_GROUPING, true));
        assertTrue(results.get(3).getResultCode().startsWith(SUCCESS));
        assertTrue(groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH));
    }

    @Test
    public void changeOptInStatusTest() {

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
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

        assertEquals(SUCCESS, turnOffWhenOnOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(2).getResultCode());

        assertEquals(SUCCESS, turnOnWhenOffOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(2).getResultCode());

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
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
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

        assertEquals(SUCCESS, turnOffWhenOnOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(2).getResultCode());

        assertEquals(SUCCESS, turnOnWhenOffOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(2).getResultCode());

        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void optOutPermissionTest() {

        boolean isHasPermission = groupAttributeService.isOptOutPossible(GROUPING_0_PATH);

        assertEquals(false, isHasPermission);

        isHasPermission = groupAttributeService.isOptOutPossible(GROUPING_1_PATH);

        assertEquals(true, isHasPermission);

    }

    @Test
    public void optInPermissionTest() {

        boolean isHasPermission = groupAttributeService.isOptInPossible(GROUPING_0_PATH);

        assertEquals(true, isHasPermission);

        isHasPermission = groupAttributeService.isOptInPossible(GROUPING_2_PATH);

        assertEquals(false, isHasPermission);
    }

    @Test
    public void hasListservTest() {

        boolean isGroupingHasListserv = groupAttributeService.isContainingListserv(GROUPING_0_PATH);

        assertEquals(false, isGroupingHasListserv);

        isGroupingHasListserv = groupAttributeService.isContainingListserv(GROUPING_3_PATH);

        assertEquals(true, isGroupingHasListserv);
    }

    @Test
    public void hasReleasedGroupingTest() {

        groupingRepository.findByPath(GROUPING_0_PATH);

        boolean isHasReleasedGrouping = groupAttributeService.isContainingReleasedGrouping(GROUPING_0_PATH);
        assertTrue(isHasReleasedGrouping);

        groupingRepository.findByPath(GROUPING_1_PATH);
        isHasReleasedGrouping = groupAttributeService.isContainingReleasedGrouping(GROUPING_1_PATH);
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
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
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


