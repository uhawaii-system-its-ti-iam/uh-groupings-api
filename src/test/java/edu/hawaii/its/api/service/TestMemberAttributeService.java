package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
        memberAttributeService.assignOwnership(GROUPING, ADMIN_USER, usernames[0]);

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
        memberAttributeService.removeOwnership(GROUPING, ADMIN_USER, usernames[1]);

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
    public void getIsOwnerTest() {
        assertFalse(memberAttributeService.getIsOwner(ADMIN_USER, "zz_zz"));

        assertTrue(memberAttributeService.isOwner(ADMIN_USER));
        assertFalse(memberAttributeService.isOwner("zz_zz"));
        Boolean[] assumptions = new Boolean[] { true, false, false, false, true, false };
        for (int i = 0; i < 6; i++) {
            assertEquals(assumptions[i], memberAttributeService.getIsOwner(ADMIN_USER, usernames[i]));
        }

        try {
            memberAttributeService.getIsOwner("zz_zz", usernames[0]);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        try {
            assertFalse(memberAttributeService.getIsOwner(ADMIN_USER, "zz_zz"));
        } catch (AccessDeniedException | GcWebServiceError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getIsAdminTest() {
        assertTrue(memberAttributeService.getIsAdmin(ADMIN_USER, ADMIN_USER));
        assertFalse(memberAttributeService.getIsAdmin(ADMIN_USER, "zzz"));

        if (memberAttributeService.getIsAdmin(ADMIN_USER, usernames[0])) {
            membershipService.deleteAdmin(ADMIN_USER, usernames[0]);
        }
        assertFalse(memberAttributeService.getIsAdmin(ADMIN_USER, usernames[0]));

        membershipService.addAdmin(ADMIN_USER, usernames[0]);
        assertTrue(memberAttributeService.getIsAdmin(ADMIN_USER, usernames[0]));
        membershipService.deleteAdmin(ADMIN_USER, usernames[0]);
        assertFalse(memberAttributeService.getIsAdmin(ADMIN_USER, usernames[0]));

        try {
            memberAttributeService.getIsAdmin("zz_zz", usernames[0]);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        try {
            assertFalse(memberAttributeService.getIsOwner(ADMIN_USER, "zz_zz"));
        } catch (AccessDeniedException | GcWebServiceError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isOwnerTest() {
        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[0]));
    }

    @Test
    public void assignRemoveOwnershipTest() {

        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[0]));
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[2]));

        try {
            memberAttributeService.assignOwnership(GROUPING, usernames[1], usernames[1]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));

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

        GroupingsServiceResult assignOwnershipSuccess =
                memberAttributeService.assignOwnership(GROUPING, usernames[0], usernames[1]);
        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[1]));
        assertTrue(assignOwnershipSuccess.getResultCode().startsWith(SUCCESS));

        attributes = groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, GROUPING, YYYYMMDDTHHMM);
        String lastModTime2 = attributes.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();
        assertNotEquals(lastModTime1, lastModTime2);

        try {
            memberAttributeService.removeOwnership(GROUPING, usernames[2], usernames[1]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[1]));

        GroupingsServiceResult removeOwnershipSuccess =
                memberAttributeService.removeOwnership(GROUPING, usernames[0], usernames[1]);
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));
        assertTrue(removeOwnershipSuccess.getResultCode().startsWith(SUCCESS));

        //have an owner remove itself
        memberAttributeService.assignOwnership(GROUPING, usernames[0], usernames[1]);
        assertTrue(memberAttributeService.isOwner(GROUPING, usernames[1]));
        memberAttributeService.removeOwnership(GROUPING, usernames[1], usernames[1]);
        assertFalse(memberAttributeService.isOwner(GROUPING, usernames[1]));
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

        //WsMembership membership = new WsMembership();
        //membershipID = membership.getMembershipId();
        //uuid = membership.getOwnerNameOfAttributeDef();
        //membership.setOwnerStemId(OPT_IN);
        //membership.setMembershipId(membershipID);

        WsAttributeAssign[] assigns = memberAttributeService.getMembershipAttributes(type, uuid, membershipID);
        //logger.info("Assigns length is " + assigns.length + ";");
        assertTrue(assigns.length == 1);

        // Test with invalid fields
        try {
            assigns = memberAttributeService.getMembershipAttributes("type", "uuid", "memberid");
            assertTrue(assigns.length == 0);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Test with null fields
        try {
            assigns = memberAttributeService.getMembershipAttributes(null, null, null);
            assertTrue(assigns.length == 0);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void getUserAttributesTest() {

        Map<String, String> attributes = memberAttributeService.getMemberAttributes(ADMIN_USER, usernames[1]);
        assertEquals("iamtst02", attributes.get("uid"));
        assertEquals("tst02name", attributes.get("cn"));
        assertEquals("tst02name", attributes.get("sn"));
        assertEquals("tst02name", attributes.get("givenName"));
        assertEquals("iamtst02", attributes.get("uhUuid"));

        attributes = memberAttributeService.getMemberAttributes("iamtst01", usernames[1]);
        assertEquals("iamtst02", attributes.get("uid"));
        assertEquals("tst02name", attributes.get("cn"));
        assertEquals("tst02name", attributes.get("sn"));
        assertEquals("tst02name", attributes.get("givenName"));
        assertEquals("iamtst02", attributes.get("uhUuid"));

        // Passing an invalid user should return a map of null values.
        attributes = memberAttributeService.getMemberAttributes("iamtst03", usernames[1]);
        assertNull(attributes.get("uid"));
        assertNull(attributes.get("cn"));
        assertNull(attributes.get("sn"));
        assertNull(attributes.get("givenName"));
        assertNull(attributes.get("uhUuid"));

        // Test with invalid username
        attributes = memberAttributeService.getMemberAttributes(ADMIN_USER, "bogusUser");
        assertNull(attributes.get("uid"));
        assertNull(attributes.get("cn"));
        assertNull(attributes.get("sn"));
        assertNull(attributes.get("givenName"));
        assertNull(attributes.get("uhUuid"));
    }

    @Test
    public void getOwnedGroupingsTest() {
        assertTrue(memberAttributeService.getOwnedGroupings(ADMIN_USER, usernames[0]).size() > 0);
        assertFalse(memberAttributeService.getOwnedGroupings(ADMIN_USER, usernames[1]).size() > 0);

        try {
            memberAttributeService.getOwnedGroupings(usernames[1], usernames[0]);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
    }
}
