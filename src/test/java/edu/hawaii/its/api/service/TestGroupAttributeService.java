package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupAttributeService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Autowired
    private GrouperConfiguration grouperConfiguration;

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
        groupAttributeService
                .changeGroupAttributeStatus(GROUPING, username[0], grouperConfiguration.getListserv(), true);
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);

        //put in include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(username[0]);
        includeNames.add(username[1]);
        includeNames.add(username[2]);
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, includeNames);

        //remove from exclude
        membershipService.addGroupingMember(username[0], GROUPING, username[4]);
        membershipService.addGroupingMember(username[0], GROUPING, username[5]);

        //add to exclude
        membershipService.deleteGroupingMember(username[0], GROUPING, username[3]);

        //remove from owners
        memberAttributeService.removeOwnership(GROUPING, username[0], username[1]);
    }

    @Test
    public void getSyncDestinationsTest() {

        // test with grouperConfiguration.getTestAdminUser()

        List<SyncDestination> destinations =
                groupAttributeService.getAllSyncDestinations(grouperConfiguration.getTestAdminUser()
                        , GROUPING);
        assertTrue(destinations.size() > 0);

        // test with owner
        destinations = groupAttributeService.getAllSyncDestinations(username[0], GROUPING);
        assertTrue(destinations.size() > 0);

        // Test with a user who isn't an owner or grouperConfiguration.getTestAdminUser()

        try {
            groupAttributeService.getAllSyncDestinations(username[5], GROUPING);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }
    }

    @Test
    public void optOutPermissionTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
    }

    @Test
    public void optInPermissionTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
    }

    @Test
    public void hasListservTest() {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));
    }

    @Test
    public void changeListServStatusTest() {

        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));

        //get last modified time
        WsGetAttributeAssignmentsResults attributes =
                groupAttributeService.attributeAssignmentsResults(grouperConfiguration.getAssignTypeGroup()
                        , GROUPING, grouperConfiguration.getYyyymmddthhmm()
                );
        String lastModTime = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        groupAttributeService
                .changeGroupAttributeStatus(GROUPING, username[0], grouperConfiguration.getListserv(), true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));

        //get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(grouperConfiguration.getAssignTypeGroup()
                , GROUPING, grouperConfiguration.getYyyymmddthhmm()
        );
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertThat(lastModTime2, is(lastModTime));

        groupAttributeService
                .changeGroupAttributeStatus(GROUPING, username[0], grouperConfiguration.getListserv(), false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));

        //todo get last modified time and make sure that it has changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(grouperConfiguration.getAssignTypeGroup()
                , GROUPING, grouperConfiguration.getYyyymmddthhmm()
        );
        String lastModTime3 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime2, lastModTime3);

        groupAttributeService
                .changeGroupAttributeStatus(GROUPING, username[0], grouperConfiguration.getListserv(), false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));

        //todo get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(grouperConfiguration.getAssignTypeGroup()
                , GROUPING, grouperConfiguration.getYyyymmddthhmm()
        );
        String lastModTime4 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertThat(lastModTime4, is(lastModTime3));

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING, username[1], grouperConfiguration.getListserv(), true);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));
        groupAttributeService
                .changeGroupAttributeStatus(GROUPING, username[0], grouperConfiguration.getListserv(), true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));
        try {
            groupAttributeService
                    .changeGroupAttributeStatus(GROUPING, username[1], grouperConfiguration.getListserv(), false);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv()));
    }

    @Test
    public void changeUhReleasedGroupingsStatusTest() {
        //todo
    }

    @Test
    public void changeOptInStatusTest() {
        //expect these to fail
        List<GroupingsServiceResult> optInFail;

        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptInStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        try {
            optInFail = membershipService.optIn(username[4], GROUPING);
        } catch (GroupingsServiceResultException gsre) {
            optInFail = new ArrayList<>();
            optInFail.add(gsre.getGsr());
        }

        assertTrue(optInFail.get(0).getResultCode().startsWith(grouperConfiguration.getFailure()
        ));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        groupAttributeService.changeOptInStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        try {
            optInFail = groupAttributeService.changeOptInStatus(GROUPING, username[1], true);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }
        assertTrue(optInFail.get(0).getResultCode().startsWith(grouperConfiguration.getFailure()
        ));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
        try {
            optInFail = groupAttributeService.changeOptInStatus(GROUPING, username[1], false);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }
        assertTrue(optInFail.get(0).getResultCode().startsWith(grouperConfiguration.getFailure()
        ));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptIn()));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void changeOptOutStatusTest() {
        //expect this to fail
        List<GroupingsServiceResult> optOutFail;

        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptOutStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        try {
            optOutFail = membershipService.optOut(username[1], GROUPING);
        } catch (GroupingsServiceResultException gsre) {
            optOutFail = new ArrayList<>();
            optOutFail.add(gsre.getGsr());
        }

        assertTrue(optOutFail.get(0).getResultCode().startsWith(grouperConfiguration.getFailure()
        ));
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));

        try {
            groupAttributeService.changeOptOutStatus(GROUPING, username[1], true);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }

        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        try {
            groupAttributeService.changeOptOutStatus(GROUPING, username[1], false);
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }

        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getOptOut()));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

    }

    @Test
    public void updateDescriptionTest() {

        GroupingsServiceResult groupingsServiceResult;

        // Sets the description to the default
        String defaultDescription = "Test Many Groups In Basis";
        groupAttributeService.updateDescription(GROUPING, grouperConfiguration.getTestAdminUser()
                , defaultDescription);

        //Test to make sure description is set to the default.
        String description = grouperFactoryService.getDescription(GROUPING);
        assertThat(defaultDescription, containsString(description));

        //Try to update grouping while user isn't owner or grouperConfiguration.getTestAdminUser()

        try {
            groupingsServiceResult = groupAttributeService.updateDescription(GROUPING, username[3], defaultDescription
                    + " modified");
        } catch (AccessDeniedException ade) {
            assertThat(ade.getMessage(), equalTo(grouperConfiguration.getInsufficientPrivileges()
            ));
        }

        //Testing with grouperConfiguration.getTestAdminUser()

        groupingsServiceResult = groupAttributeService
                .updateDescription(GROUPING, grouperConfiguration.getTestAdminUser(), defaultDescription + " modified");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        //Testing with owner
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING, username[0], defaultDescription + " modifiedTwo");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        // Test with empty string
        groupingsServiceResult =
                groupAttributeService.updateDescription(GROUPING, grouperConfiguration.getTestAdminUser(), "");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(grouperConfiguration.getSuccess()));

        //Revert any changes
        groupAttributeService.updateDescription(GROUPING, grouperConfiguration.getTestAdminUser(), defaultDescription);

    }

    //todo Test to play around with GroupAttribute methods
    @Ignore
    @Test
    public void changeGroupAttributeStatusTest() {
        boolean isInitial = groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv());
        GroupingsServiceResult gsr = groupAttributeService
                .changeGroupAttributeStatus(GROUPING, grouperConfiguration.getTestAdminUser(),
                        grouperConfiguration.getListserv(), true);
        boolean isAfter = groupAttributeService.isGroupAttribute(GROUPING, grouperConfiguration.getListserv());
        assertTrue(true);
    }
}
