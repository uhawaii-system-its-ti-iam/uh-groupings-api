package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    private PersonRepository personRepository;

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
        assertThat(memberAttributeService.isUhUuid("username"), is(false));
        // Valid UhUuid.
        assertThat(memberAttributeService.isUhUuid("0000"), is(true));
        // Null.
        assertThat(memberAttributeService.isUhUuid(null), is(false));
    }

    @Test
    public void getMembershipResultsTest() {
        // A user can access their own memberships.
        List<Membership> memberships =
                memberAttributeService.getMembershipResults(users.get(0).getUsername(), users.get(0).getUsername());
        assertNotNull(memberships);
        for (Membership membership : memberships) {
            assertNotNull(membership);
            assertNotNull(membership.getPath());
            assertNotNull(membership.getName());
            assertEquals((GROUPING_0_PATH.substring(0, GROUPING_0_PATH.length() - 1)),
                    membership.getPath().substring(0, membership.getPath().length() - 1));
            assertTrue(membership.getPath().endsWith(membership.getName()));
            assertNull(membership.getPerson());
            assertNull(membership.getIdentifier());
            assertFalse(membership.isSelfOpted());
            assertFalse(membership.isOptInEnabled());
            assertFalse(membership.isInInclude());
            assertFalse(membership.isInExclude());
            assertTrue(membership.isInBasis());
            assertTrue(membership.isInOwner());
        }
        // Admins can access anyone's memberships.
        for (int i = 0; i < 5; i++) {
            memberships = memberAttributeService.getMembershipResults(ADMIN_USER, users.get(i).getUsername());
            assertNotNull(memberships);
        }
        // A non-admin user cannot access another users memberships.
        try {
            memberAttributeService.getMembershipResults(users.get(0).getUsername(), users.get(1).getUsername());
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Admins accessing an invalid user will return an empty list.
        memberships = memberAttributeService.getMembershipResults(ADMIN_USER, "zzzzzzzzzzzzzzzzzz");
        assertNotNull(memberships);
        assertTrue(memberships.isEmpty());
    }

    @Test
    public void addGroupingMembersTest() {
        List<AddMemberResult> addMemberResults;
        List<String> usersToAdd = new ArrayList<>();

        String ownerUsername = users.get(0).getUsername();

        usersToAdd.add(users.get(2).getUsername());
        usersToAdd.add(users.get(3).getUsername());

        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_3_INCLUDE_PATH, usersToAdd);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
        }
        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_3_EXCLUDE_PATH, usersToAdd);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
        }
        try {
            membershipService.addGroupMembers("zz_zzz", GROUPING_3_EXCLUDE_PATH, usersToAdd);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // A group path ending in anything other than include or exclude should 404.
        try {
            membershipService.addGroupMembers(ownerUsername, GROUPING_1_OWNERS_PATH, usersToAdd);
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }
    }

    @Test
    public void addIncludeMembersTest() {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add(users.get(2).getUsername());
        usersToAdd.add(users.get(3).getUsername());
        try {
            membershipService.addIncludeMembers("zz_zzz", GROUPING_3_PATH, usersToAdd);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // Non-owner/admin attempts add.
        try {
            membershipService.addIncludeMembers(users.get(2).getUsername(), GROUPING_3_PATH, usersToAdd);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }

    @Test
    public void addExcludeMembersTest() {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add(users.get(2).getUsername());
        usersToAdd.add(users.get(3).getUsername());

        // Bogus owner attempts to add.
        try {
            membershipService.addExcludeMembers("zz_zzz", GROUPING_3_PATH, usersToAdd);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // Non-owner/admin attempts add.
        try {
            membershipService.addExcludeMembers(users.get(2).getUsername(), GROUPING_3_PATH, usersToAdd);
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
                membershipService.removeGroupMembers(ownerUsername, GROUPING_3_INCLUDE_PATH, usersToRemove);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(FAILURE, removeMemberResult.getResult());
        }
        removeMemberResults =
                membershipService.removeGroupMembers(ownerUsername, GROUPING_3_EXCLUDE_PATH, usersToRemove);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(FAILURE, removeMemberResult.getResult());
        }
        try {
            membershipService.removeGroupMembers("zz_zzz", GROUPING_3_EXCLUDE_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        try {
            membershipService.removeGroupMembers(ownerUsername, GROUPING_1_OWNERS_PATH, usersToRemove);
        }catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }
    }

    @Test
    public void removeIncludeMembersTest() {
        List<String> usersToRemove = new ArrayList<>();
        usersToRemove.add(users.get(2).getUsername());
        usersToRemove.add(users.get(3).getUsername());
        // Bogus owner.
        try {
            membershipService.removeIncludeMembers("zz_zzz", GROUPING_3_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        // Non-owner/admin attempts remove.
        try {
            membershipService.removeExcludeMembers(users.get(2).getUsername(), GROUPING_3_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }

    @Test
    public void removeExcludeMembersTest() {
        List<String> usersToRemove = new ArrayList<>();
        usersToRemove.add(users.get(2).getUsername());
        usersToRemove.add(users.get(3).getUsername());
        // Bogus owner.
        try {
            membershipService.removeExcludeMembers("zz_zzz", GROUPING_3_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // Non-owner/admin attempts remove.
        try {
            membershipService.removeExcludeMembers(users.get(2).getUsername(), GROUPING_3_PATH, usersToRemove);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }

    @Test
    public void addOwnerTest() {
        //expect this to fail
        List<AddMemberResult> randomUserAdds;

        Person randomUser = personRepository.findByUsername(users.get(1).getUsername());
        Grouping grouping = groupingRepository.findByPath(GROUPING_0_PATH);

        assertFalse(grouping.getOwners().getMembers().contains(randomUser));
        assertFalse(grouping.getOwners().isMember(randomUser));

        try {
            randomUserAdds = membershipService
                    .addOwners(GROUPING_0_PATH, randomUser.getUsername(),
                            Collections.singletonList(randomUser.getUsername()));
            assertTrue(randomUserAdds.get(0).getResult().startsWith(FAILURE));
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            membershipService.addOwners(GROUPING_0_PATH, users.get(0).getUsername(),
                    Collections.singletonList(randomUser.getUsername()));
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertFalse(grouping.getOwners().getMembers().contains(randomUser));
        assertFalse(grouping.getOwners().isMember(randomUser));
    }

    @Test
    public void optInTest() {
        // Invalid user attempts to opt.
        try {
            membershipService.optIn("zz_zzz", GROUPING_3_PATH, users.get(2).getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        // Non-owner/admin attempts to opt.
        try {
            membershipService.optIn(users.get(2).getUsername(), GROUPING_3_PATH, users.get(2).getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

    }

    @Test
    public void optOutTest() {
        // Invalid user attempts to opt.
        try {
            membershipService.optOut("zz_zzz", GROUPING_3_PATH, users.get(2).getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // Non-owner/admin attempts to opt.
        try {
            membershipService.optOut(users.get(2).getUsername(), GROUPING_3_PATH, users.get(2).getUsername());
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
    public void removeAdminTest() {
        // usernameToRemove is not a superuser.
        String usernameToRemove = users.get(9).getUsername();

        try {
            // User is not super user.
            membershipService.removeAdmin(usernameToRemove, ADMIN_USER);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        // user is super user usernameToRemove is not superuser.
        GroupingsServiceResult gsr = membershipService.removeAdmin(ADMIN_USER, usernameToRemove);
        assertThat(gsr.getResultCode(), is(SUCCESS));

        // make usernameToRemove a superuser.
        membershipService.addAdmin(ADMIN_USER, usernameToRemove);
        assertTrue(memberAttributeService.isAdmin(usernameToRemove));

        // user is super user usernameToRemove is not superuser.
        gsr = membershipService.removeAdmin(ADMIN_USER, usernameToRemove);
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
        boolean isOop = memberAttributeService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_2_EXCLUDE_PATH);
        assertThat(isOop, is(false));

        isOop = memberAttributeService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_1_EXCLUDE_PATH);
        assertThat(isOop, is(true));
    }

    @Test
    public void getNumberOfMembershipsTest(){
        String user = users.get(10).getUsername();

        assertThat(memberAttributeService.getNumberOfMemberships(ADMIN_USER, user), is(0));

        membershipService.optIn(ADMIN_USER, GROUPING_0_PATH, user);
        membershipService.optIn(ADMIN_USER, GROUPING_1_PATH, user);
        membershipService.optIn(ADMIN_USER, GROUPING_2_PATH, user);
        membershipService.optIn(ADMIN_USER, GROUPING_3_PATH, user);

        assertThat(memberAttributeService.getNumberOfMemberships(ADMIN_USER, user), is(4));

        membershipService.optOut(ADMIN_USER, GROUPING_1_PATH, user);
        membershipService.optOut(ADMIN_USER, GROUPING_3_PATH, user);

        assertThat(memberAttributeService.getNumberOfMemberships(ADMIN_USER, user), is(2));

        membershipService.optIn(ADMIN_USER, GROUPING_1_PATH, user);

        assertThat(memberAttributeService.getNumberOfMemberships(ADMIN_USER, user), is(3));
    }
}