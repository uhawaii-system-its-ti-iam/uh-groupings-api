package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MembershipServiceTest {

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.not_in_group}")
    private String NOT_IN_GROUP;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = ":include";
    private static final String EXCLUDE = ":exclude";
    private static final String OWNERS = ":owners";
    private static final String BASIS = ":basis";

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;
    private static final String GROUPING_4_PATH = PATH_ROOT + 4;

    private static final String GROUPING_1_INCLUDE_PATH = GROUPING_1_PATH + INCLUDE;
    private static final String GROUPING_1_EXCLUDE_PATH = GROUPING_1_PATH + EXCLUDE;
    private static final String GROUPING_1_OWNERS_PATH = GROUPING_1_PATH + OWNERS;

    private static final String GROUPING_2_EXCLUDE_PATH = GROUPING_2_PATH + EXCLUDE;

    private static final String GROUPING_3_INCLUDE_PATH = GROUPING_3_PATH + INCLUDE;
    private static final String GROUPING_3_EXCLUDE_PATH = GROUPING_3_PATH + EXCLUDE;
    private static final String GROUPING_3_BASIS_PATH = GROUPING_3_PATH + BASIS;

    private static final String GROUPING_4_INCLUDE_PATH = GROUPING_4_PATH + INCLUDE;
    private static final String GROUPING_4_EXCLUDE_PATH = GROUPING_4_PATH + EXCLUDE;

    private static final String ADMIN_USER = "admin";
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private static final String APP_USER = "app";
    private List<Person> apps = new ArrayList<>();
    private Group appGroup = new Group();

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Autowired
    private HelperService helperService;

    @Before
    public void setup() {

        // todo not sure if preserving admins/adminGroup/appGroup fuctionality is necessary
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(membershipService);
        assertNotNull(helperService);
    }

    @Test
    public void isUhUuidTest() {
        // Invalid UhUuid.
        assertThat(membershipService.isUhUuid("username"), is(false));
        // Valid UhUuid.
        assertThat(membershipService.isUhUuid("0000"), is(true));
        // Null.
        assertThat(membershipService.isUhUuid(null), is(false));
    }

    @Test
    public void getMembershipResultsTest() {
        try {
            String ownerUsername = ADMIN;
            String uid = "iamtst01";
            membershipService.getMembershipResults(ownerUsername, uid);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void addGroupingMembersTest() {
        List<AddMemberResult> addMemberResults;
        List<String> usersToAdd = new ArrayList<>();

        String ownerUsername = users.get(0).getUsername();

        usersToAdd.add(users.get(2).getUsername());
        usersToAdd.add(users.get(3).getUsername());

        addMemberResults = membershipService.addGroupingMembers(ownerUsername, GROUPING_3_INCLUDE_PATH, usersToAdd);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
        }
        addMemberResults = membershipService.addGroupingMembers(ownerUsername, GROUPING_3_EXCLUDE_PATH, usersToAdd);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
        }
        try {
            membershipService.addGroupingMembers("zzzzz", GROUPING_3_EXCLUDE_PATH, usersToAdd);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }

    @Test
    public void removeGroupingMembersTest() {
        List<RemoveMemberResult> removeMemberResults;
        List<String> usersToRemove = new ArrayList<>();

        String ownerUsername = users.get(0).getUsername();

        usersToRemove.add(users.get(2).getUsername());
        usersToRemove.add(users.get(3).getUsername());

        removeMemberResults =
                membershipService.removeGroupingMembers(ownerUsername, GROUPING_3_INCLUDE_PATH, usersToRemove);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(FAILURE, removeMemberResult.getResult());
        }
        removeMemberResults =
                membershipService.removeGroupingMembers(ownerUsername, GROUPING_3_EXCLUDE_PATH, usersToRemove);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(FAILURE, removeMemberResult.getResult());
        }
        try {
            membershipService.removeGroupingMembers("zzzzz", GROUPING_3_EXCLUDE_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }

    @Test
    public void addAdminTest() {

        try {
            // user is not super user.
            membershipService.addAdmin(users.get(9).getUsername(), users.get(9).getUsername());
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        // user is super user.
        GroupingsServiceResult gsr = membershipService.addAdmin(ADMIN_USER, users.get(9).getUsername());
        assertThat(gsr.getResultCode(), is(SUCCESS));

        // users.get(9) is already and admin.
        gsr = membershipService.addAdmin(ADMIN_USER, users.get(9).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

    }

    @Test
    public void deleteAdminTest() {
        // usernameToDelete is not a superuser.
        String usernameToDelete = users.get(9).getUsername();

        try {
            // User is not super user.
            membershipService.deleteAdmin(usernameToDelete, ADMIN_USER);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        // user is super user usernameToDelete is not superuser.
        GroupingsServiceResult gsr = membershipService.deleteAdmin(ADMIN_USER, usernameToDelete);
        assertThat(gsr.getResultCode(), is(SUCCESS));

        // make usernameToDelete a superuser.
        membershipService.addAdmin(ADMIN_USER, usernameToDelete);
        assertTrue(memberAttributeService.isAdmin(usernameToDelete));

        // user is super user usernameToDelete is not superuser.
        gsr = membershipService.deleteAdmin(ADMIN_USER, usernameToDelete);
        assertThat(gsr.getResultCode(), is(SUCCESS));

    }

    @Test
    public void removeFromGroupsTest() {
        String userToRemove = users.get(0).getUsername();
        List<String> GroupPaths = new ArrayList<String>();
        GroupPaths.add(GROUPING_1_PATH);
        GroupPaths.add(GROUPING_2_PATH);
        GroupPaths.add(GROUPING_3_PATH);
        List<GroupingsServiceResult> gsr = membershipService.removeFromGroups(ADMIN_USER, userToRemove, GroupPaths);
        assertThat(gsr.get(0).getResultCode(), is(SUCCESS));
        assertThat(gsr.get(1).getResultCode(), is(SUCCESS));
        assertThat(gsr.get(2).getResultCode(), is(SUCCESS));
    }

    /*
    @Test
    public void optInTest() {
        List<GroupingsServiceResult> optInResults;

        try {
            // opt in Permission for include group false.
            optInResults = membershipService.optIn(users.get(2).getUsername(), GROUPING_2_PATH);
        } catch (GroupingsServiceResultException gsre) {
            optInResults = new ArrayList<>();
            optInResults.add(gsre.getGsr());
        }
        assertTrue(optInResults.get(0).getResultCode().startsWith(FAILURE));

        // opt in Permission for include group true and not in group, but in basis.
        optInResults = membershipService.optIn(users.get(1).getUsername(), GROUPING_1_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertThat(optInResults.size(), is(1));

        // opt in Permission for include group true but already in group, not self opted.
        optInResults = membershipService.optIn(users.get(9).getUsername(), GROUPING_0_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));

        // opt in Permission for include group true, but already self-opted.
        optInResults = membershipService.optIn(users.get(9).getUsername(), GROUPING_0_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));

        // non super users should not be able to opt in other users.
        optInResults = membershipService.optIn(users.get(0).getUsername(), GROUPING_0_PATH, users.get(1).getUsername());
        assertThat(optInResults.size(), is(1));
        assertTrue(optInResults.get(0).getResultCode().startsWith(FAILURE));

        // super users should be able to opt in other users.
        optInResults = membershipService.optIn(ADMIN_USER, GROUPING_0_PATH, users.get(2).getUsername());
        assertThat(optInResults.size(), is(2));
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));
    }


    @Test
    public void optOutTest() {
        List<GroupingsServiceResult> optOutResults;
        try {
            // opt out Permission for exclude group false.
            optOutResults = membershipService.optOut(users.get(1).getUsername(), GROUPING_0_PATH);
        } catch (GroupingsServiceResultException gsre) {
            optOutResults = new ArrayList<>();
            optOutResults.add(gsre.getGsr());
        }
        assertTrue(optOutResults.get(0).getResultCode().startsWith(FAILURE));

        // opt out Permission for exclude group true.

        optOutResults = membershipService.optOut(users.get(1).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        // opt out Permission for exclude group true, but already in the exclude group.
        optOutResults = membershipService.optOut(users.get(2).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        // opt out Permission for exclude group true, but already self-opted.
        optOutResults = membershipService.optOut(users.get(2).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        // non super users should not be able to opt out other users.
        optOutResults =
                membershipService.optOut(users.get(0).getUsername(), GROUPING_1_PATH, users.get(1).getUsername());
        assertThat(optOutResults.size(), is(1));
        assertTrue(optOutResults.get(0).getResultCode().startsWith(FAILURE));

        // super users should be able to opt in other users.
        optOutResults = membershipService.optOut(ADMIN_USER, GROUPING_1_PATH, users.get(6).getUsername());
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertThat(optOutResults.size(), is(1));
    }
     */

    @Test
    public void selfOptedTest() {
        Group group = groupRepository.findByPath(GROUPING_4_EXCLUDE_PATH);

        GroupingsServiceResult gsr;

        try {
            // member is not in group.
            gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(5).getUsername());
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }
        assertTrue(gsr.getResultCode().startsWith(FAILURE));

        // member is not self-opted.
        gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(4).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

        // make member self-opted.
        Membership membership = membershipRepository.findByPersonAndGroup(users.get(4), group);
        membership.setSelfOpted(true);
        membershipRepository.save(membership);

        // member is self-opted.
        gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(4).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

        // addselfopted on not a member.
        try {
            gsr = membershipService.addSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(5).getUsername());
            assertFalse(gsr.getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            gsre.getGsr();
        }

    }

    @Test
    public void groupOptOutPermissionTest() {
        boolean isOop = membershipService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_2_EXCLUDE_PATH);
        assertThat(isOop, is(false));

        isOop = membershipService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_1_EXCLUDE_PATH);
        assertThat(isOop, is(true));
    }
}
