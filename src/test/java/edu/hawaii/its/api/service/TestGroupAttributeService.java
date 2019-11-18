package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.SyncDestination;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
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
    public void setUp() {
        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV,true);
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);

        //put in include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(username[0]);
        includeNames.add(username[1]);
        includeNames.add(username[2]);
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, includeNames);

        //remove from exclude
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[4]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[5]);

        //add to exclude
        membershipService.deleteGroupingMemberByUsername(username[0], GROUPING, username[3]);

        //remove from owners
        memberAttributeService.removeOwnership(GROUPING, username[0], username[1]);
    }

    @Test
    public void getSyncDestinationsTest() {
        //todo find a more specific way to test this

        // test with admin
        List<SyncDestination> destinations = groupAttributeService.getAllSyncDestinations(ADMIN);
        assertTrue(destinations.size() > 0);

        // test with owner
        destinations = groupAttributeService.getAllSyncDestinations(username[0]);
        assertTrue(destinations.size() > 0);

        // make sure username[6] doesn't own anything
        List<String> ownedGroupings = membershipService.listOwned(ADMIN, username[5]);
        for (String grouping : ownedGroupings) {
            memberAttributeService.removeOwnership(grouping, ADMIN, username[5]);
        }

        try {
            groupAttributeService.getAllSyncDestinations(username[5]);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
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

        //get last modified time
        WsGetAttributeAssignmentsResults attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV,true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));


        //get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertEquals(lastModTime, lastModTime2);

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV,false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING,LISTSERV));

        //todo get last modified time and make sure that it has changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime3 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime2, lastModTime3);

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV,false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));

        //todo get last modified time and make sure that it hasn't changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }
        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime4 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertEquals(lastModTime3, lastModTime4);

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, username[1], LISTSERV,true);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV,true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        try {
            groupAttributeService.changeGroupAttributeStatus(GROUPING, username[1], LISTSERV,false);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
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
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptInStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        try {
            optInFail = membershipService.optIn(username[4], GROUPING);
        } catch (GroupingsServiceResultException gsre) {
            optInFail = new ArrayList<>();
            optInFail.add(gsre.getGsr());
        }
        assertTrue(optInFail.get(0).getResultCode().startsWith(FAILURE));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        groupAttributeService.changeOptInStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        try {
            optInFail = groupAttributeService.changeOptInStatus(GROUPING, username[1], true);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        assertTrue(optInFail.get(0).getResultCode().startsWith(FAILURE));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
        try {
            optInFail = groupAttributeService.changeOptInStatus(GROUPING, username[1], false);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
        assertTrue(optInFail.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
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
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        groupAttributeService.changeOptOutStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        try {
            optOutFail = membershipService.optOut(username[1], GROUPING);
        } catch (GroupingsServiceResultException gsre) {
            optOutFail = new ArrayList<>();
            optOutFail.add(gsre.getGsr());
        }

        assertTrue(optOutFail.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], false);
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));

        try {
            groupAttributeService.changeOptOutStatus(GROUPING, username[1], true);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertFalse(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertFalse(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

        try {
            groupAttributeService.changeOptOutStatus(GROUPING, username[1], false);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));

    }

    @Test
    public void updateDescriptionTest(){

        GroupingsServiceResult groupingsServiceResult;

        // Sets the description to the default
        groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION);

        //Test to make sure description is set to the default.
        String description = grouperFactoryService.getDescription(GROUPING);
        assertThat(DEFAULT_DESCRIPTION, containsString(description));

        //Try to update grouping while user isn't owner or admin
        try {
            groupingsServiceResult = groupAttributeService.updateDescription(GROUPING, username[3], DEFAULT_DESCRIPTION + " modified");
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        //Testing with admin
        groupingsServiceResult = groupAttributeService.updateDescription(GROUPING, ADMIN, DEFAULT_DESCRIPTION + " modified");
        assertThat(groupingsServiceResult.getResultCode(), startsWith(SUCCESS));

        //Testing with owner
        groupingsServiceResult = groupAttributeService.updateDescription(GROUPING, username[0], DEFAULT_DESCRIPTION + " modifiedTwo");
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
        boolean isInitial = groupAttributeService.isGroupAttribute(GROUPING, LISTSERV);
        GroupingsServiceResult gsr = groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, LISTSERV, true);
        boolean isAfter = groupAttributeService.isGroupAttribute(GROUPING, LISTSERV);
        assertTrue(true);
    }
}
