package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Membership;
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

import static org.junit.Assert.*;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MembershipServiceTest {

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

    private static final String GROUPING_4_EXCLUDE_PATH = GROUPING_4_PATH + EXCLUDE;

    private static final String ADMIN_USER = "admin";
    private static final Person ADMIN_PERSON = new Person(ADMIN_USER, ADMIN_USER, ADMIN_USER);
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private static final String APP_USER = "app";
    private static final Person APP_PERSON = new Person(APP_USER, APP_USER, APP_USER);
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
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {

        // todo not sure if preserving admins/adminGroup/appGroup fuctionality is necessary
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(membershipService);
    }

    @Test
    public void listOwnedTest() {

        // Tests that when there is no groups owned, the list is empty
        assertTrue(membershipService.listOwned(ADMIN_USER, users.get(1).getUsername()).isEmpty());

        // Adds user to owners of GROUPING 1
        membershipService
                .addGroupMember(users.get(0).getUsername(), GROUPING_1_OWNERS_PATH, users.get(1).getUsername());

        // Tests that the list now contains the path to GROUPING 1 since user is now an owner
        assertTrue(membershipService.listOwned(ADMIN_USER, users.get(1).getUsername()).get(0).equals(GROUPING_1_PATH));

        // Tests if a non admin can access users groups owned
        try {
            membershipService.listOwned(users.get(0).getUsername(), users.get(1).getUsername());
            // should get access denied exception
            fail();
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

    }

    @Test
    public void addGroupingMemberTest() {

        List<GroupingsServiceResult> listGsr;

        String ownerUsername = users.get(0).getUsername();
        String groupingPath = GROUPING_3_PATH;
        String userToAdd = users.get(5).getUsername();
        String uuidToAdd = users.get(5).getUhUuid();

        listGsr = membershipService.addGroupingMember(ownerUsername, groupingPath, userToAdd);
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        listGsr = membershipService.addGroupingMember(ownerUsername, groupingPath, uuidToAdd);
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
    }

    // Debug statement to look at contents of database
    // Delete user from include group to remove them
    // Use user number not slot in array
    // Use assert to check if it worked
    @Test
    public void deleteGroupingMemberByUuidTest() {
        Iterable<Grouping> group = groupingRepository.findAll();
        List<GroupingsServiceResult> listGsr;
        GroupingsServiceResult gsr;

        // Base test
        // Remove person from include and composite
        listGsr = membershipService.deleteGroupingMemberByUuid(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(5).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        // If person is in composite and basis, add to exclude group
        listGsr = membershipService.deleteGroupingMemberByUuid(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(1).getUhUuid());
        for (GroupingsServiceResult gsrFor : listGsr) {
            assertTrue(gsrFor.getResultCode().startsWith(SUCCESS));
        }

        // Not in composite, do nothing but return success
        listGsr = membershipService.deleteGroupingMemberByUuid(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(2).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        // todo Can't test with current database setup
        // Not in basis, but in exclude
        // Can't happen with current database

        // Test if user is not an owner
        try {
            listGsr = membershipService.deleteGroupingMemberByUuid(users.get(5).getUsername(), GROUPING_3_PATH,
                    users.get(6).getUhUuid());
            assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        // Test if user is admin
        listGsr = membershipService.deleteGroupingMemberByUuid(ADMIN_USER, GROUPING_3_PATH,
                users.get(6).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        // Test if path is not allowed to delete from
        try {
            gsr = membershipService.deleteGroupMemberByUuid(users.get(0).getUsername(), GROUPING_3_BASIS_PATH,
                    users.get(6).getUsername());
            assertTrue(gsr.getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }
    }

    @Test
    public void addGroupingMemberbyUuidTest() {
        Iterable<Grouping> group = groupingRepository.findAll();
        List<GroupingsServiceResult> listGsr;
        GroupingsServiceResult gsr;

        // Base test
        // Remove person who's not in composite from exclude and return SUCCESS
        listGsr = membershipService.addGroupingMemberByUuid(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(3).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //todo Case where !inComposite && !inBasis is impossible w/ current db

        // In composite
        listGsr = membershipService.addGroupingMemberByUuid(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(5).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //todo Case where inBasis && inInclude is impossible w/ current db

        // Test if user is not an owner
        try {
            listGsr = membershipService.addGroupingMemberByUuid(users.get(5).getUsername(), GROUPING_3_PATH,
                    users.get(3).getUhUuid());
            assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        // Test if user is admin
        listGsr = membershipService.addGroupingMemberByUuid(ADMIN_USER, GROUPING_3_PATH,
                users.get(3).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void addGroupingMemberbyUsernameTest() {
        Iterable<Grouping> group = groupingRepository.findAll();
        List<GroupingsServiceResult> listGsr;
        GroupingsServiceResult gsr;

        // Base test
        // Remove person who's not in composite from exclude and return SUCCESS
        listGsr = membershipService.addGroupingMemberByUsername(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(2).getUsername());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //todo Case where !inComposite && !inBasis is impossible w/ current db

        // In composite
        listGsr = membershipService.addGroupingMemberByUsername(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(5).getUsername());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //todo Case where inBasis && inInclude is impossible w/ current db

        // Test if user is not an owner
        try {
            listGsr = membershipService.addGroupingMemberByUsername(users.get(5).getUsername(), GROUPING_3_PATH,
                    users.get(3).getUsername());
            assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        // Test if user is admin
        listGsr = membershipService.addGroupingMemberByUsername(ADMIN_USER, GROUPING_3_PATH,
                users.get(3).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

    }

    @Test
    public void deleteGroupingMemberbyUsernameTest() {
        Iterable<Grouping> group = groupingRepository.findAll();
        List<GroupingsServiceResult> listGsr;
        GroupingsServiceResult gsr;

        // Base test
        // Remove person from include and composite
        listGsr = membershipService.deleteGroupingMemberByUsername(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(5).getUsername());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        // If person is in composite and basis, add to exclude group
        listGsr = membershipService.deleteGroupingMemberByUsername(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(1).getUsername());
        for (GroupingsServiceResult gsrFor : listGsr) {
            assertTrue(gsrFor.getResultCode().startsWith(SUCCESS));
        }

        // Not in composite, do nothing but return success
        listGsr = membershipService.deleteGroupingMemberByUsername(users.get(0).getUsername(), GROUPING_3_PATH,
                users.get(2).getUsername());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        // todo Can't test with current database setup
        // Not in basis, but in exclude
        // Can't happen with current database

        // Test if user is not an owner
        try {
            listGsr = membershipService.deleteGroupingMemberByUsername(users.get(5).getUsername(), GROUPING_3_PATH,
                    users.get(6).getUhUuid());
            assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        // Test if user is admin
        listGsr = membershipService.deleteGroupingMemberByUsername(ADMIN_USER, GROUPING_3_PATH,
                users.get(6).getUhUuid());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //Test if wrong path is given
        try {
            gsr = membershipService.deleteGroupMemberByUsername(users.get(0).getUsername(), GROUPING_3_BASIS_PATH,
                    users.get(6).getUsername());
            assertTrue(gsr.getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }

    }

    @Test
    public void addGroupMemberTest() {

        List<GroupingsServiceResult> listGsr;

        String ownerUsername = users.get(0).getUsername();
        String groupPath = GROUPING_3_INCLUDE_PATH;
        String userToAdd = users.get(2).getUsername();
        String uuidToAdd = users.get(2).getUhUuid();

        listGsr = membershipService.addGroupMember(ownerUsername, groupPath, userToAdd);
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        listGsr = membershipService.addGroupMember(ownerUsername, groupPath, uuidToAdd);
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

    }

    @Test
    public void addGroupMembersTest() {

        List<GroupingsServiceResult> listGsr;
        List<String> usersToAdd = new ArrayList<String>();
        List<String> uuidsToAdd = new ArrayList<String>();

        String ownerUsername = users.get(0).getUsername();
        String groupPath = GROUPING_3_INCLUDE_PATH;

        usersToAdd.add(users.get(2).getUsername());
        usersToAdd.add(users.get(3).getUsername());

        uuidsToAdd.add(users.get(2).getUhUuid());
        uuidsToAdd.add(users.get(3).getUhUuid());

        listGsr = membershipService.addGroupMembers(ownerUsername, groupPath, usersToAdd);
        for (int i = 0; i < listGsr.size(); i++) {
            assertTrue(listGsr.get(i).getResultCode().startsWith(SUCCESS));
        }

        listGsr = membershipService.addGroupMembers(ownerUsername, groupPath, uuidsToAdd);
        for (int i = 0; i < listGsr.size(); i++) {
            assertTrue(listGsr.get(i).getResultCode().startsWith(SUCCESS));
        }
    }

    @Test
    public void deleteGroupMemberTest() {

        // Passes even though 1234 is not a person
        GroupingsServiceResult gsr = membershipService.deleteGroupMember(ADMIN_USER, GROUPING_3_INCLUDE_PATH, "1234");
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));
        assertTrue(gsr.getResultCode().contains(NOT_IN_GROUP));

        GroupingsServiceResult gsr2 =
                membershipService.deleteGroupMember(ADMIN_USER, GROUPING_3_INCLUDE_PATH, users.get(5).getUsername());
        assertTrue(gsr2.getResultCode().startsWith(SUCCESS));
        assertFalse(gsr2.getResultCode().contains(NOT_IN_GROUP));
    }

    @Test
    public void addAdminTest() {

        try {
            //user is not super user
            membershipService.addAdmin(users.get(9).getUsername(), users.get(9).getUsername());
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        //user is super user
        GroupingsServiceResult gsr = membershipService.addAdmin(ADMIN_USER, users.get(9).getUsername());
        assertEquals(SUCCESS, gsr.getResultCode());

        //users.get(9) is already and admin
        gsr = membershipService.addAdmin(ADMIN_USER, users.get(9).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

    }

    @Test
    public void deleteAdminTest() {
        //usernameToDelete is not a superuser
        String usernameToDelete = users.get(9).getUsername();

        try {
            //user is not super user
            membershipService.deleteAdmin(usernameToDelete, ADMIN_USER);
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        //user is super user usernameToDelete is not superuser
        GroupingsServiceResult gsr = membershipService.deleteAdmin(ADMIN_USER, usernameToDelete);
        assertEquals(SUCCESS, gsr.getResultCode());

        //make usernameToDelete a superuser
        membershipService.addAdmin(ADMIN_USER, usernameToDelete);
        assertTrue(memberAttributeService.isAdmin(usernameToDelete));

        //user is super user usernameToDelete is not superuser
        gsr = membershipService.deleteAdmin(ADMIN_USER, usernameToDelete);
        assertEquals(SUCCESS, gsr.getResultCode());

    }

    @Test
    public void optInTest() {
        List<GroupingsServiceResult> optInResults;

        try {
            //opt in Permission for include group false
            optInResults = membershipService.optIn(users.get(2).getUsername(), GROUPING_2_PATH);
        } catch (GroupingsServiceResultException gsre) {
            optInResults = new ArrayList<>();
            optInResults.add(gsre.getGsr());
        }
        assertTrue(optInResults.get(0).getResultCode().startsWith(FAILURE));

        //opt in Permission for include group true and not in group, but in basis
        optInResults = membershipService.optIn(users.get(1).getUsername(), GROUPING_1_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertEquals(1, optInResults.size());

        //opt in Permission for include group true but already in group, not self opted
        optInResults = membershipService.optIn(users.get(9).getUsername(), GROUPING_0_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));

        //opt in Permission for include group true, but already self-opted
        optInResults = membershipService.optIn(users.get(9).getUsername(), GROUPING_0_PATH);
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));

        //non super users should not be able to opt in other users
        optInResults = membershipService.optIn(users.get(0).getUsername(), GROUPING_0_PATH, users.get(1).getUsername());
        assertEquals(1, optInResults.size());
        assertTrue(optInResults.get(0).getResultCode().startsWith(FAILURE));

        //super users should be able to opt in other users
        optInResults = membershipService.optIn(ADMIN_USER, GROUPING_0_PATH, users.get(2).getUsername());
        assertEquals(2, optInResults.size());
        assertTrue(optInResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optInResults.get(1).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void optOutTest() {
        List<GroupingsServiceResult> optOutResults;
        try {
            //opt out Permission for exclude group false
            optOutResults = membershipService.optOut(users.get(1).getUsername(), GROUPING_0_PATH);
        } catch (GroupingsServiceResultException gsre) {
            optOutResults = new ArrayList<>();
            optOutResults.add(gsre.getGsr());
        }
        assertTrue(optOutResults.get(0).getResultCode().startsWith(FAILURE));

        //opt out Permission for exclude group true
        optOutResults = membershipService.optOut(users.get(1).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        //opt out Permission for exclude group true, but already in the exclude group
        optOutResults = membershipService.optOut(users.get(2).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        //opt out Permission for exclude group true, but already self-opted
        optOutResults = membershipService.optOut(users.get(2).getUsername(), GROUPING_1_PATH);
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(optOutResults.get(2).getResultCode().startsWith(SUCCESS));

        //non super users should not be able to opt out other users
        optOutResults =
                membershipService.optOut(users.get(0).getUsername(), GROUPING_1_PATH, users.get(1).getUsername());
        assertEquals(1, optOutResults.size());
        assertTrue(optOutResults.get(0).getResultCode().startsWith(FAILURE));

        //super users should be able to opt in other users
        optOutResults = membershipService.optOut(ADMIN_USER, GROUPING_1_PATH, users.get(6).getUsername());
        assertTrue(optOutResults.get(0).getResultCode().startsWith(SUCCESS));
        assertEquals(1, optOutResults.size());
    }

    @Test
    public void selfOptedTest() {
        Group group = groupRepository.findByPath(GROUPING_4_EXCLUDE_PATH);

        GroupingsServiceResult gsr;

        try {
            //member is not in group
            gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(5).getUsername());
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }
        assertTrue(gsr.getResultCode().startsWith(FAILURE));

        //member is not self-opted
        gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(4).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

        //make member self-opted
        Membership membership = membershipRepository.findByPersonAndGroup(users.get(4), group);
        membership.setSelfOpted(true);
        membershipRepository.save(membership);

        //member is self-opted
        gsr = membershipService.removeSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(4).getUsername());
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

        //addselfopted on not a member
        try {
            gsr = membershipService.addSelfOpted(GROUPING_4_EXCLUDE_PATH, users.get(5).getUsername());
            assertFalse(gsr.getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }

    }

    @Test
    public void groupOptOutPermissionTest() {
        boolean isOop = membershipService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_2_EXCLUDE_PATH);
        assertEquals(false, isOop);

        isOop = membershipService.isGroupCanOptOut(users.get(1).getUsername(), GROUPING_1_EXCLUDE_PATH);
        assertEquals(true, isOop);
    }

    @Test
    public void addMemberByUsernameTest() {
        Grouping grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        List<GroupingsServiceResult> listGsr;
        GroupingsServiceResult gsr;

        assertFalse(grouping.getComposite().getMembers().contains(users.get(3)));

        membershipService.addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_1_INCLUDE_PATH,
                users.get(3).getUsername());
        grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        assertTrue(grouping.getComposite().getMembers().contains(users.get(3)));
        //todo Cases (inBasis && inInclude) and (!inComposite && !inBasis) not reachable w/ current DB

        //add existing Owner
        membershipService.addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_1_OWNERS_PATH,
                users.get(0).getUsername());
        grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        assertTrue(grouping.getComposite().getMembers().contains(users.get(0)));

        //add to basis path (not allowed)
        try {
            listGsr = membershipService.addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_3_BASIS_PATH,
                    users.get(6).getUsername());
            assertTrue(listGsr.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            gsr = gsre.getGsr();
        }

        //add member already in group
        listGsr = membershipService
                .addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_1_INCLUDE_PATH,
                        users.get(5).getUsername());
        assertTrue(listGsr.get(0).getResultCode().startsWith(SUCCESS));

        //member that is adding is not an owner (not allowed)
        try {
            listGsr = membershipService.addGroupMemberByUsername(users.get(2).getUsername(), GROUPING_3_INCLUDE_PATH,
                    users.get(3).getUsername());
            assertTrue(listGsr.get(0).getResultCode().startsWith(FAILURE));
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }
    }

    @Test
    public void addMembersByUsername() {
        //add all usernames
        List<String> usernames = new ArrayList<>();
        for (Person user : users) {
            usernames.add(user.getUsername());
        }

        Grouping grouping = groupingRepository.findByPath(GROUPING_3_PATH);

        //check how many members are in the basis
        int numberOfBasisMembers = grouping.getBasis().getMembers().size();

        //try to put all users into exclude group
        membershipService.addGroupMembersByUsername(users.get(0).getUsername(), GROUPING_3_EXCLUDE_PATH, usernames);
        grouping = groupingRepository.findByPath(GROUPING_3_PATH);
        //there should be no real members in composite, but it should still have the 'grouperAll' member
        assertEquals(1, grouping.getComposite().getMembers().size());
        //only the users in the basis should have been added to the exclude group
        assertEquals(numberOfBasisMembers, grouping.getExclude().getMembers().size());

        //try to put all users into the include group
        membershipService.addGroupMembersByUsername(users.get(0).getUsername(), GROUPING_3_INCLUDE_PATH, usernames);
        grouping = groupingRepository.findByPath(GROUPING_3_PATH);
        //all members should be in the group ( - 1 for 'grouperAll' in composite);
        assertEquals(usernames.size(), grouping.getComposite().getMembers().size() - 1);
        //members in basis should not have been added to the include group ( + 2 for 'grouperAll' in both groups)
        assertEquals(usernames.size() - numberOfBasisMembers + 2, grouping.getInclude().getMembers().size());
    }

    @Test
    public void addMemberByUuidTest() {
        Grouping grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        assertFalse(grouping.getComposite().getMembers().contains(users.get(3)));

        membershipService
                .addGroupMemberByUuid(users.get(0).getUsername(), GROUPING_1_INCLUDE_PATH, users.get(3).getUhUuid());
        grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        assertTrue(grouping.getComposite().getMembers().contains(users.get(3)));
    }

    @Test
    public void addMembersByUuid() {
        //add all uuids
        List<String> uuids = new ArrayList<>();
        for (Person user : users) {
            uuids.add(user.getUhUuid());
        }

        Grouping grouping = groupingRepository.findByPath(GROUPING_3_PATH);

        //check how many members are in the basis
        int numberOfBasisMembers = grouping.getBasis().getMembers().size();

        //try to put all users into exclude group
        membershipService.addGroupMembersByUuid(users.get(0).getUsername(), GROUPING_3_EXCLUDE_PATH, uuids);
        grouping = groupingRepository.findByPath(GROUPING_3_PATH);
        //there should be no real members in composite, but it should still have the 'grouperAll' member
        assertEquals(1, grouping.getComposite().getMembers().size());
        //only the users in the basis should have been added to the exclude group
        assertEquals(numberOfBasisMembers, grouping.getExclude().getMembers().size());

        //try to put all users into the include group
        membershipService.addGroupMembersByUuid(users.get(0).getUsername(), GROUPING_3_INCLUDE_PATH, uuids);
        grouping = groupingRepository.findByPath(GROUPING_3_PATH);
        //all members should be in the group ( - 1 for 'grouperAll' in composite);
        assertEquals(uuids.size(), grouping.getComposite().getMembers().size() - 1);
        //members in basis should not have been added to the include group ( + 2 for 'grouperAll' in both groups)
        assertEquals(uuids.size() - numberOfBasisMembers + 2, grouping.getInclude().getMembers().size());
    }
}
