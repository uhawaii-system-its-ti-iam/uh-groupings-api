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

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GroupAttributeServiceTest {

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;
    private static final String GROUPING_4_PATH = PATH_ROOT + 4;
    private static final String ADMIN_USER = "admin";
    private static final String DEFAULT_DESCRIPTION = "Default description.";

    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();
    private Group appGroup = new Group();
    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private GrouperConfiguration grouperConfiguration;
   
    @Autowired
    private GroupAttributeService groupAttributeService;

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

        assertTrue(groupAttributeService.getAllSyncDestinations(ADMIN_USER, GROUPING_0_PATH).size() > 0);

    }

    @Test
    public void changeListservStatusTest() {

        Grouping grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(),
                            grouperConfiguration.getListserv(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOnWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(),
                                grouperConfiguration.getListserv(), true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(),
                            grouperConfiguration.getListserv(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOnWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(),
                                grouperConfiguration.getListserv(), true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOnWhenOnAdmin =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, grouperConfiguration.getListserv(),
                                true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(),
                            grouperConfiguration.getListserv(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOffWhenOnOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(),
                                grouperConfiguration.getListserv(), false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOnWhenOffAdmin =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, grouperConfiguration.getListserv(),
                                true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOffWhenOnAdmin =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, grouperConfiguration.getListserv(),
                                false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(1).getUsername(),
                            grouperConfiguration.getListserv(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOffWhenOffOwner =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, users.get(0).getUsername(),
                                grouperConfiguration.getListserv(), false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        GroupingsServiceResult turnOffWhenOffAdmin =
                groupAttributeService
                        .changeGroupAttributeStatus(GROUPING_4_PATH, ADMIN_USER, grouperConfiguration.getListserv(),
                                false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isSyncDestinationOn(grouperConfiguration.getListserv()));

        assertTrue(turnOnWhenOnOwner.getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.getResultCode().startsWith(grouperConfiguration.getSuccess()));

        assertTrue(turnOffWhenOnOwner.getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOnAdmin.getResultCode().startsWith(grouperConfiguration.getSuccess()));

        assertTrue(turnOnWhenOffOwner.getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOffAdmin.getResultCode().startsWith(grouperConfiguration.getSuccess()));

        assertTrue(turnOffWhenOffOwner.getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.getResultCode().startsWith(grouperConfiguration.getSuccess()));
    }

    @Test
    public void changeReleasedGroupingStatusTest() {
        List<GroupingsServiceResult> results = new ArrayList<>();
        String user = users.get(1).getUsername(); // username1
        String owner = users.get(0).getUsername(); // username0

        //starts ON
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping()));

        // ON to ON
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_0_PATH, user, grouperConfiguration.getReleasedGrouping(),
                            true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        results.add(groupAttributeService
                .changeGroupAttributeStatus(GROUPING_0_PATH, owner, grouperConfiguration.getReleasedGrouping(), true));
        assertTrue(results.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping()));

        //ON to OFF
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_0_PATH, user, grouperConfiguration.getReleasedGrouping(),
                            false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        results.add(groupAttributeService
                .changeGroupAttributeStatus(GROUPING_0_PATH, owner, grouperConfiguration.getReleasedGrouping(), false));
        assertTrue(results.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertFalse(
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping()));

        // OFF to OFF
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_0_PATH, user, grouperConfiguration.getReleasedGrouping(),
                            false);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        results.add(groupAttributeService
                .changeGroupAttributeStatus(GROUPING_0_PATH, owner, grouperConfiguration.getReleasedGrouping(), false));
        assertTrue(results.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertFalse(
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping()));

        // OFF to ON
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING_0_PATH, user, grouperConfiguration.getReleasedGrouping(),
                            true);
            fail("Shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        results.add(groupAttributeService
                .changeGroupAttributeStatus(GROUPING_0_PATH, owner, grouperConfiguration.getReleasedGrouping(), true));
        assertTrue(results.get(3).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping()));
    }

    @Test
    public void changeOptInStatusTest() {

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupAttributeService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));

        assertThat(turnOffWhenOnOwner.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnOwner.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnOwner.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));

        assertThat(turnOnWhenOffOwner.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffOwner.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffOwner.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));

        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
    }

    @Test
    public void changeOptOutStatusTest() {

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }

        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        try {
            groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupAttributeService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));

        assertThat(turnOffWhenOnOwner.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnOwner.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnOwner.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOffWhenOnAdmin.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));

        assertThat(turnOnWhenOffOwner.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffOwner.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffOwner.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(0).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(1).getResultCode(), is(grouperConfiguration.getSuccess()));
        assertThat(turnOnWhenOffAdmin.get(2).getResultCode(), is(grouperConfiguration.getSuccess()));

        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(grouperConfiguration.getSuccess()));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(grouperConfiguration.getSuccess()));
    }

    @Test
    public void optOutPermissionTest() {

        boolean isHasPermission =
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getOptOut());

        assertThat(isHasPermission, is(false));

        isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_1_PATH, grouperConfiguration.getOptOut());

        assertThat(isHasPermission, is(true));

    }

    @Test
    public void optInPermissionTest() {

        boolean isHasPermission =
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getOptIn());

        assertThat(isHasPermission, is(true));

        isHasPermission = groupAttributeService.isGroupAttribute(GROUPING_2_PATH, grouperConfiguration.getOptIn());

        assertThat(isHasPermission, is(false));
    }

    @Test
    public void hasListservTest() {

        boolean isGroupingHasListserv =
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getListserv());

        assertThat(isGroupingHasListserv, is(false));

        isGroupingHasListserv =
                groupAttributeService.isGroupAttribute(GROUPING_3_PATH, grouperConfiguration.getListserv());

        assertThat(isGroupingHasListserv, is(true));
    }

    @Test
    public void hasReleasedGroupingTest() {

        groupingRepository.findByPath(GROUPING_0_PATH);

        boolean isHasReleasedGrouping =
                groupAttributeService.isGroupAttribute(GROUPING_0_PATH, grouperConfiguration.getReleasedGrouping());
        assertTrue(isHasReleasedGrouping);

        groupingRepository.findByPath(GROUPING_1_PATH);
        isHasReleasedGrouping =
                groupAttributeService.isGroupAttribute(GROUPING_1_PATH, grouperConfiguration.getReleasedGrouping());
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
            assertThat(grouperConfiguration.getInsufficientPrivileges(), is(ade.getMessage()));
        }

        //Testing with admin
        groupingsServiceResult = groupAttributeService
                .updateDescription(GROUPING_0_PATH, ADMIN_USER, DEFAULT_DESCRIPTION + " modifiedbyadmin1");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        //Testing with owner
        groupingsServiceResult = groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(),
                DEFAULT_DESCRIPTION + " modifiedbyowner2");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        // Test with empty string
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(), "");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        //Revert any changes
        groupAttributeService.updateDescription(GROUPING_0_PATH, users.get(0).getUsername(), DEFAULT_DESCRIPTION);
    }
}


