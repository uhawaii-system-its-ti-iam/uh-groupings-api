package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupAttributeService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;
    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUPING_BASIS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;
    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;
    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    public Environment env; // Just for the settings check.

    @PostConstruct
    public void init() {
        Assert.hasLength(env.getProperty("grouperClient.webService.url"),
                "property 'grouperClient.webService.url' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.login"),
                "property 'grouperClient.webService.login' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.password"),
                "property 'grouperClient.webService.password' is required");
    }

    @Before
    public void setUp() throws IOException, MessagingException {
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, ADMIN, true);
        groupAttributeService.changeOptOutStatus(GROUPING, ADMIN, true);

        //put in include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(username[0]);
        includeNames.add(username[1]);
        includeNames.add(username[2]);
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, includeNames);

        //remove from exclude
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, Collections.singletonList(username[4]));
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, Collections.singletonList(username[5]));

        //add to exclude
        membershipService.addGroupMembers(username[0], GROUPING_EXCLUDE, Collections.singletonList(username[3]));

        //add owners
        membershipService.addOwnerships(GROUPING, ADMIN, Arrays.asList(username[0]));

        //remove from owners
        membershipService.removeOwnerships(GROUPING, username[0], Arrays.asList(username[1]));
    }

    @Test
    public void getAllSyncDestinationsTest() {

        // test with admin
        List<SyncDestination> destinations = groupAttributeService.getAllSyncDestinations(ADMIN, GROUPING);
        assertTrue(destinations.size() > 0);

        // test with owner
        destinations = groupAttributeService.getAllSyncDestinations(username[0], GROUPING);
        assertTrue(destinations.size() > 0);

        // Test with a user who isn't an owner or admin
        try {
            groupAttributeService.getAllSyncDestinations(username[5], GROUPING);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(INSUFFICIENT_PRIVILEGES));
        }
    }

    @Test public void getSyncDestinationsTest() {
        Grouping grouping = new Grouping();
        grouping.setPath(GROUPING);
        grouping.setName("test-grouping");
        List<SyncDestination> destinations = groupAttributeService.getSyncDestinations(grouping);
        Set<String> names = new HashSet<>();
        for (SyncDestination destination : destinations) {
            assertNotNull(destination.getTooltip());
            assertNotNull(destination.getName());
            assertNotNull(destination.getDescription());
            assertNotNull(destination.isSynced());
            assertNotNull(destination.isHidden());
            // Check for duplicates.
            assertTrue(names.add(destination.getName()));
        }
    }

    @Test
    public void optOutPermissionTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
    }

    @Test
    public void optInPermissionTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
    }

    @Test
    public void hasListservTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
    }

    @Test
    public void changeListServStatusTest() {

        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));

        // Get last modified time
        WsGetAttributeAssignmentsResults attributes =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV, true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));

        //get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertThat(lastModTime2, is(lastModTime));

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV, false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));

        // Get last modified time and make sure that it has changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime3 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime2, lastModTime3);

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV, false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));

        // Get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime4 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertThat(lastModTime4, is(lastModTime3));

        // Invalid user can't change attribute status
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, "zzz_zzz", LISTSERV, true);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(INSUFFICIENT_PRIVILEGES));
        }
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV, true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, "zzz_zzz", LISTSERV, false);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(INSUFFICIENT_PRIVILEGES));
        }
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
    }

    @Test
    public void changeUhReleasedGroupingsStatusTest() {
        //todo
    }

    @Test
    public void updateDescriptionTest() {

        GroupingsServiceResult groupingsServiceResult;

        // Sets the description to the default
        groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION);

        //Test to make sure description is set to the default.
        String description = grouperFactoryService.getDescription(GROUPING);
        assertThat(DEFAULT_DESCRIPTION, containsString(description));

        //Try to update grouping while user isn't owner or admin
        try {
            groupingsServiceResult =
                    groupAttributeService.updateDescription(GROUPING, username[3], DEFAULT_DESCRIPTION + " modified");
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(INSUFFICIENT_PRIVILEGES));
        }

        //Testing with admin
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION + " modified");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        //Testing with owner
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING, username[0], DEFAULT_DESCRIPTION + " modifiedTwo");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        // Test with empty string
        groupingsServiceResult = groupAttributeService.updateDescription(GROUPING, ADMIN, "");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        //Revert any changes
        groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION);

    }

    //todo Test to play around with GroupAttribute methods
    @Ignore
    @Test
    public void changeGroupAttributeStatusTest() {
        groupAttributeService.isGroupAttribute(GROUPING, LISTSERV);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, LISTSERV, true);
        groupAttributeService.isGroupAttribute(GROUPING, LISTSERV);
        assertTrue(true);
    }
}