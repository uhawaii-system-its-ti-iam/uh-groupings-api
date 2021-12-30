package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMemberAttributeService {

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
    @Value("${groupings.api.test.grouping_many_extra}")
    private String GROUPING_EXTRA;

    @Value("${groupings.api.test.grouping_timeout_test}")
    private String GROUPING_TIMEOUT;
    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;
    @Value("${groupings.api.assign_type_immediate_membership}")
    private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;
    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.test.usernames}")
    private String[] usernames;

    @Value("${grouperClient.webService.login}")
    private String APP_USER;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN_USER;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

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
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN_USER, LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, ADMIN_USER, true);
        groupAttributeService.changeOptOutStatus(GROUPING, ADMIN_USER, true);

        // add to owners
        membershipService.addOwners(GROUPING, ADMIN_USER, Collections.singletonList(usernames[0]));

        // add to basis (you cannot do this directly, so we add the user to one of the groups that makes up the basis)
        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ADMIN_USER);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[3]);

        //add to include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(usernames[0]);
        includeNames.add(usernames[1]);
        includeNames.add(usernames[2]);
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, includeNames);

        //remove from exclude
        membershipService.removeGroupMembers(ADMIN_USER, GROUPING_EXCLUDE, Collections.singletonList(usernames[4]));
        membershipService.removeGroupMembers(ADMIN_USER, GROUPING_EXCLUDE, Collections.singletonList(usernames[5]));

        //add to exclude
        membershipService.addGroupMembers(ADMIN_USER, GROUPING_EXCLUDE, Collections.singletonList(usernames[3]));

        //remove from owners
        membershipService.removeOwnerships(GROUPING, ADMIN_USER, Arrays.asList(usernames));

        // add to owners
        membershipService.addOwners(GROUPING, ADMIN_USER, Collections.singletonList(usernames[0]));

        // Remove from Exclude
        membershipService.removeGroupMembers(ADMIN_USER, GROUPING_EXCLUDE, Collections.singletonList(usernames[4]));

        // Turn off Self-Opted flags
        //todo Tests run properly without doing a isSelfOpted check on GROUPING_INCLUDE and usernames[1] for unknown reason
        membershipService.removeSelfOpted(GROUPING_INCLUDE, usernames[1]);
        if (memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, usernames[4])) {
            membershipService.removeSelfOpted(GROUPING_EXCLUDE, usernames[4]);
        }
    }

    @Test
    public void isOwnerTest() {
        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[0]));
    }

    @Test
    public void addOwners() {
        //Test non-owner adding to owners list
        try {
            membershipService.addOwners(GROUPING, usernames[1], Collections.singletonList(usernames[1]));
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));

        // get last modified time
        WsGetAttributeAssignmentsResults attributes =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime1 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        // Check that the last modified timestamp has changed after adding a user
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }

        List<AddMemberResult> addOwnerSuccess =
                membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[1]));
        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[1]));
        assertTrue(addOwnerSuccess.get(0).getResult().startsWith(SUCCESS));

        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime1, lastModTime2);

        //Test an owner adding a member to owners list
        List<AddMemberResult> ownerAdds = membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[2]));
        assertTrue(ownerAdds.get(0).isUserWasAdded());
        assertTrue(memberAttributeService.isOwner(GROUPING, ownerAdds.get(0).getUserIdentifier()));
        assertEquals(ownerAdds.get(0).getResult(), SUCCESS);

        //Test a admin adding a member to owners list
        List<AddMemberResult> adminAdds = membershipService.addOwners(GROUPING, ADMIN_USER, Collections.singletonList(usernames[3]));
        assertTrue(adminAdds.get(0).isUserWasAdded());
        assertTrue(memberAttributeService.isOwner(GROUPING, adminAdds.get(0).getUserIdentifier()));
        assertEquals(adminAdds.get(0).getResult(), SUCCESS);

        //Test adding uhuid to owners list
        List<AddMemberResult> uuidAdds = membershipService.addOwners(GROUPING, ADMIN_USER, Collections.singletonList("iamtst05"));
        assertTrue(uuidAdds.get(0).isUserWasAdded());
        assertTrue(memberAttributeService.isOwner(GROUPING, uuidAdds.get(0).getUserIdentifier()));
        assertEquals(uuidAdds.get(0).getResult(), SUCCESS);

        //Test adding multiple owners to the owners list
        List<String> addOwners = new ArrayList<>(Arrays.asList(usernames).subList(1, 6));
        membershipService.removeOwnerships(GROUPING, usernames[0], addOwners);

        List<AddMemberResult> addOwnerResults =
                membershipService.addOwners(GROUPING, usernames[0], addOwners);
        Iterator<String> iter = addOwners.iterator();

        assertEquals(5, membershipService.addOwners(GROUPING, ADMIN_USER, addOwners).size());
        for (int i = 0; i < addOwners.size(); i++) {
            assertTrue(memberAttributeService.isOwner(GROUPING, usernames[i]));
        }
        assertEquals(6, membershipService.addOwners(GROUPING, ADMIN_USER, Arrays.asList(usernames)).size());
        for (String ownersAdded : usernames) {
            assertTrue(memberAttributeService.isOwner(GROUPING, ownersAdded));
        }

        //Test results returned from adding owner to list
        for (AddMemberResult addOwnerResult : addOwnerResults) {
            assertEquals(GROUPING, addOwnerResult.getPathOfAdd());
            assertEquals(SUCCESS, addOwnerResult.getResult());
            assertNotNull(addOwnerResult.getName());
            assertNotNull(addOwnerResult.getUhUuid());
            assertEquals(iter.next(), addOwnerResult.getUid());
        }
    }

    @Test
    public void removeOwners() {
        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[1]));
        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[2]));

        //Test a non-owner removes a owner
        try {
            membershipService.removeOwnerships(GROUPING, usernames[3], Collections.singletonList(usernames[0]));
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        assertTrue(memberAttributeService.isOwner(usernames[0]));

        // get last modified time
        WsGetAttributeAssignmentsResults attributes =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime1 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        // get last modified time and make sure that it has changed
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            fail();
        }

        List<RemoveMemberResult> removeOwnerSuccess =
                membershipService.removeOwnerships(GROUPING, usernames[0], Collections.singletonList(usernames[1]));
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));
        assertTrue(removeOwnerSuccess.get(0).getResult().startsWith(SUCCESS));

        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime1, lastModTime2);

        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[1]));

        //Test an owner adding a member to owners list
        List<RemoveMemberResult> ownerRemoves =
                membershipService.removeOwnerships(GROUPING, usernames[0], Collections.singletonList(usernames[1]));
        assertTrue(ownerRemoves.get(0).isUserWasRemoved());
        assertFalse(memberAttributeService.isOwner(GROUPING, ownerRemoves.get(0).getUserIdentifier()));
        assertEquals(ownerRemoves.get(0).getResult(), SUCCESS);

        //Test a admin adding a member to owners list
        List<RemoveMemberResult> adminRemoves =
                membershipService.removeOwnerships(GROUPING, ADMIN_USER, Collections.singletonList(usernames[2]));
        assertTrue(adminRemoves.get(0).isUserWasRemoved());
        assertFalse(memberAttributeService.isOwner(GROUPING, adminRemoves.get(0).getUserIdentifier()));
        assertEquals(adminRemoves.get(0).getResult(), SUCCESS);

        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[3]));

        //Test removing a uhuid from the owners list
        List<RemoveMemberResult> uuidRemove = membershipService.removeOwnerships(GROUPING, ADMIN_USER, Collections.singletonList("iamtst04"));
        assertTrue(uuidRemove.get(0).isUserWasRemoved());
        assertFalse(memberAttributeService.isOwner(GROUPING, uuidRemove.get(0).getUserIdentifier()));
        assertEquals(uuidRemove.get(0).getResult(), SUCCESS);

        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[0]));
        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[1]));
        membershipService.addOwners(GROUPING, usernames[0], Collections.singletonList(usernames[2]));

        //Test removing multiple owners to the owners list
        List<String> removeOwners = new ArrayList<>(Arrays.asList(usernames).subList(1, 3));
        List<RemoveMemberResult> removeOwnerResults =
                membershipService.removeOwnerships(GROUPING, usernames[0], removeOwners);
        Iterator<String> iter = removeOwners.iterator();
        assertEquals(2, membershipService.removeOwnerships(GROUPING, ADMIN_USER, removeOwners).size());
        for (String ownersRemoved : removeOwners) {
            assertFalse(memberAttributeService.isOwner(GROUPING, ownersRemoved));
        }
        assertEquals(6, membershipService.removeOwnerships(GROUPING, ADMIN_USER, Arrays.asList(usernames)).size());
        for (String ownersRemoved : usernames) {
            assertFalse(memberAttributeService.isOwner(GROUPING, ownersRemoved));
        }

        //Test data returned from removing owners
        for (RemoveMemberResult removeOwnerResult : removeOwnerResults) {
            assertEquals(GROUPING, removeOwnerResult.getPathOfRemoved());
            assertEquals(SUCCESS, removeOwnerResult.getResult());
            assertNotNull(removeOwnerResult.getName());
            assertNotNull(removeOwnerResult.getUhUuid());
            assertEquals(iter.next(), removeOwnerResult.getUid());
        }

    }

    @Test
    public void isMemberTest() {
        //test isMember with username
        memberAttributeService.isMember(GROUPING, usernames[2]);

        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, usernames[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, usernames[3]));

        membershipService.addGroupMembers(usernames[0], GROUPING_EXCLUDE, Collections.singletonList(usernames[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, usernames[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, usernames[1]));

        //test isMember with Person
        Person testPersonInclude = new Person("tst01name", "iamtst01", usernames[1]);
        Person testPersonExclude = new Person("tst03name", "iamtst03", usernames[3]);
        Person testPersonNull = null;

        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, testPersonInclude));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testPersonExclude));

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, testPersonExclude));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testPersonInclude));

        // Test if username does not exist
        try {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, "someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // Test if username/person is NULL
        try {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testPersonNull));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        String nullString = null;
        try {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, nullString));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Test if grouping does not exist
        try {
            assertFalse(memberAttributeService.isMember("someGroup", usernames[1]));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        try {
            assertFalse(memberAttributeService.isMember("someGroup", testPersonExclude));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Test if grouping is NULL
        try {
            assertFalse(memberAttributeService.isSelfOpted(null, usernames[1]));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        try {
            assertFalse(memberAttributeService.isMember(null, testPersonExclude));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void isSelfOptedTest() {
        //todo How to change/know if user is SelfOpted/Admin/Appuser etc.

        // User is not self opted because user is not in group
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, usernames[4]));
        membershipService.addGroupMembers(usernames[0], GROUPING_EXCLUDE, Collections.singletonList(usernames[4]));

        // User is not self opted b/c added by owner
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, usernames[4]));

        membershipService.addSelfOpted(GROUPING_EXCLUDE, usernames[4]);

        // Alternate implementation
        //membershipService.deleteGroupMemberByUsername(usernames[0], GROUPING_EXCLUDE, usernames[4]);
        //membershipService.optOut(usernames[4], GROUPING);

        // User is self opted b/c added himself
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, usernames[4]));

        // User does not exist
        try {
            assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, "someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // User is null
        try {
            assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Group does not exist
        try {
            assertFalse(memberAttributeService.isSelfOpted("someGroup", usernames[4]));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Group path is null
        try {
            assertFalse(memberAttributeService.isSelfOpted(null, usernames[4]));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void isAppTest() {
        //todo Write in overrides file who the App User is (not directly for security reasons)
        //todo I need permissions so I know who App User and Admin User are on Grouper Test Server

        // User is not app user
        assertFalse(memberAttributeService.isApp(usernames[1]));

        // User is app user
        assertTrue(memberAttributeService.isApp(APP_USER));

        // User does not exist
        try {
            assertFalse(memberAttributeService.isApp("someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // User is null
        try {
            assertFalse(memberAttributeService.isApp(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void isAdminTest() {
        //todo

        // User is not admin
        assertFalse(memberAttributeService.isAdmin(usernames[1]));

        // User is admin
        assertTrue(memberAttributeService.isAdmin(ADMIN_USER));

        // User does not exist
        try {
            assertFalse(memberAttributeService.isAdmin("someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // User is null
        try {
            assertFalse(memberAttributeService.isAdmin(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void getMembershipAttributesTest() {
        //todo I don't know what to put as arguments (particularly membershipid)
        // Ternary Operator (for reference)
        // if(!null) return wsAttributes
        // else return grouperFS.makeEmptyWSAttributeAssignArray

        String type = ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;
        String uuid = SELF_OPTED;

        WsGetMembershipsResults results = helperService.membershipsResults(usernames[1], GROUPING_INCLUDE);
        String membershipID = helperService.extractFirstMembershipID(results);
        assertThat(memberAttributeService.getMembershipAttributes(type, uuid, membershipID).length, is(0));

        membershipService.addSelfOpted(GROUPING_INCLUDE, usernames[1]);

//        WsMembership membership = new WsMembership();
//        membershipID = membership.getMembershipId();
//        uuid = membership.getOwnerNameOfAttributeDef();
//        membership.setOwnerStemId(OPT_IN);
//        membership.setMembershipId(membershipID);

        WsAttributeAssign[] assigns = memberAttributeService.getMembershipAttributes(type, uuid, membershipID);
        //logger.info("Assigns length is " + assigns.length + ";");
        assertEquals(1, assigns.length);

        // Test with invalid fields
        try {
            assigns = memberAttributeService.getMembershipAttributes("type", "uuid", "memberid");
            assertEquals(0, assigns.length);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Test with null fields
        try {
            assigns = memberAttributeService.getMembershipAttributes(null, null, null);
            assertEquals(0, assigns.length);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void getUserAttributesTest() {

        Map<String, String> attributes = memberAttributeService.getMemberAttributes(ADMIN_USER, usernames[1]).getAttributes();
        assertEquals("iamtst02", attributes.get("uid"));
        assertEquals("tst02name", attributes.get("cn"));
        assertEquals("tst02name", attributes.get("sn"));
        assertEquals("tst02name", attributes.get("givenName"));
        assertEquals("iamtst02", attributes.get("uhUuid"));

        attributes = memberAttributeService.getMemberAttributes("iamtst01", usernames[1]).getAttributes();
        assertEquals("iamtst02", attributes.get("uid"));
        assertEquals("tst02name", attributes.get("cn"));
        assertEquals("tst02name", attributes.get("sn"));
        assertEquals("tst02name", attributes.get("givenName"));
        assertEquals("iamtst02", attributes.get("uhUuid"));

        // Passing an invalid user should return a map of null values.
        attributes = memberAttributeService.getMemberAttributes("zzz_zzz", usernames[1]).getAttributes();
        assertNull(attributes.get("uid"));
        assertNull(attributes.get("cn"));
        assertNull(attributes.get("sn"));
        assertNull(attributes.get("givenName"));
        assertNull(attributes.get("uhUuid"));

        // Test with invalid username
        attributes = memberAttributeService.getMemberAttributes(ADMIN_USER, "bogusUser").getAttributes();
        assertNull(attributes.get("uid"));
        assertNull(attributes.get("cn"));
        assertNull(attributes.get("sn"));
        assertNull(attributes.get("givenName"));
        assertNull(attributes.get("uhUuid"));
    }

    @Test
    public void getOwnedGroupingsTest() {
        // Testing with valid username
        assertTrue(memberAttributeService.getOwnedGroupings(ADMIN_USER, usernames[0]).size() > 0);
        // Test with invalid username
        assertFalse(memberAttributeService.getOwnedGroupings("zzz_zzz", usernames[1]).size() > 0);

        try {
            memberAttributeService.getOwnedGroupings(usernames[1], usernames[0]);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
    }
}