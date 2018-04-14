package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
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

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    //@Value("${grouping.api.test.uuids")
    //private String[] uuid;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

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
        groupAttributeService.changeListservStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);

        //put in include
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[0]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[1]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[2]);

        //remove from exclude
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[4]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[5]);

        //add to exclude
        membershipService.deleteGroupingMemberByUsername(username[0], GROUPING, username[3]);

        //remove from owners
        memberAttributeService.removeOwnership(GROUPING, username[0], username[1]);
    }

    @Test
    public void isOwnerTest() {
        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
    }

    @Test
    public void assignRemoveOwnershipTest() {
        //expect to fail
        GroupingsServiceResult assignOwnershipFail;
        GroupingsServiceResult removeOwnershipFail;

        assertTrue(memberAttributeService.isOwner(GROUPING, username[0]));
        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        try {
            assignOwnershipFail = memberAttributeService.assignOwnership(GROUPING, username[1], username[1]);
        } catch (GroupingsServiceResultException gsre) {
            assignOwnershipFail = gsre.getGsr();
        }
        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        assertTrue(assignOwnershipFail.getResultCode().startsWith(FAILURE));

        GroupingsServiceResult assignOwnershipSuccess =
                memberAttributeService.assignOwnership(GROUPING, username[0], username[1]);
        assertTrue(memberAttributeService.isOwner(GROUPING, username[1]));
        assertTrue(assignOwnershipSuccess.getResultCode().startsWith(SUCCESS));

        try {
            removeOwnershipFail = memberAttributeService.removeOwnership(GROUPING, username[2], username[1]);
        } catch (GroupingsServiceResultException gsre) {
            removeOwnershipFail = gsre.getGsr();
        }

        assertTrue(memberAttributeService.isOwner(GROUPING, username[1]));
        assertTrue(removeOwnershipFail.getResultCode().startsWith(FAILURE));

        GroupingsServiceResult removeOwnershipSuccess =
                memberAttributeService.removeOwnership(GROUPING, username[0], username[1]);
        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));
        assertTrue(removeOwnershipSuccess.getResultCode().startsWith(SUCCESS));

        //have an owner remove itself
        assignOwnershipSuccess = memberAttributeService.assignOwnership(GROUPING, username[0], username[1]);
        assertTrue(memberAttributeService.isOwner(GROUPING, username[1]));
        removeOwnershipSuccess = memberAttributeService.removeOwnership(GROUPING, username[1], username[1]);
        assertFalse(memberAttributeService.isOwner(GROUPING, username[1]));

    }

    @Test
    public void isMemberTest() {
        //test isMember with username
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));

        //todo NPE The person is required to have either a username or UUID
        //test isMember with Person
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, new Person(username[1])));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, new Person(username[3])));

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, new Person(username[3])));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, new Person(username[1])));
    }

    @Test
    public void isSelfOptedTest() {
        //todo How to change/know if user is SelfOpted/Admin/Appuser etc.

        // User is not self opted
        assertFalse(memberAttributeService.isSelfOpted(GROUPING, username[1]));

        // User is self opted
        //WsAttributeAssign att = new WsAttributeAssign();
        //att.setAttributeDefName(SELF_OPTED);
        //membershipService.addSelfOpted(GROUPING, username[1]);
        Membership membership = new Membership();
        //membership = membershipService.addSelfOpted(GROUPING, username[1]);
        membership.setSelfOpted(true);

        assertTrue(memberAttributeService.isSelfOpted(GROUPING, username[1]));

        // User does not exist
        try {
            assertFalse(memberAttributeService.isSelfOpted(GROUPING, "someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // User is null
        try {
            assertFalse(memberAttributeService.isSelfOpted(GROUPING, null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Group does not exist

        // Group path is null

    }

    @Test
    public void isAppTest() {
        //todo

        // User is not app user
        assertFalse(memberAttributeService.isApp(username[1]));

        // User is app user
        //assertTrue(memberAttributeService.isApp(username[1]));

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
    public void isSuperUserTest() {
        //todo

        // User is not super user
        assertFalse(memberAttributeService.isSuperuser(username[1]));

        // User is super user
        //assertTrue(memberAttributeService.isSuperuser(username[1]));

        // User does not exist
        try {
            assertFalse(memberAttributeService.isSuperuser("someName"));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // User is null
        try {
            assertFalse(memberAttributeService.isSuperuser(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void isAdminTest() {
        //todo

        // User is not admin
        assertFalse(memberAttributeService.isAdmin(username[1]));

        // User is admin
        //assertTrue(memberAttributeService.isSuperuser(username[1]));

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
        //todo
        WsAttributeAssign[] assigns = memberAttributeService.getMembershipAttributes("type", "uuid", "memberid");

    }
}
