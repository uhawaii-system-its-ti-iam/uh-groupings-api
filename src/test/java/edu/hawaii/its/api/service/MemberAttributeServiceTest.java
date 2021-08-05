package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalToObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberAttributeServiceTest {

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

    @Value("${groupings.api.test.uhuuid}")
    private String UUID;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = ":include";
    private static final String OWNERS = ":owners";

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;

    private static final String GROUPING_0_INCLUDE_PATH = GROUPING_0_PATH + INCLUDE;
    private static final String GROUPING_0_OWNERS_PATH = GROUPING_0_PATH + OWNERS;

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
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(memberAttributeService);
    }

    @Test
    public void assignOwnershipTest() {
        //expect this to fail
        GroupingsServiceResult randomUserAdds;

        Person randomUser = personRepository.findByUsername(users.get(1).getUsername());
        Grouping grouping = groupingRepository.findByPath(GROUPING_0_PATH);

        assertFalse(grouping.getOwners().getMembers().contains(randomUser));
        assertFalse(grouping.getOwners().isMember(randomUser));

        try {
            randomUserAdds = memberAttributeService
                    .assignOwnership(GROUPING_0_PATH, randomUser.getUsername(), randomUser.getUsername());
            assertTrue(randomUserAdds.getResultCode().startsWith(FAILURE));
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertFalse(grouping.getOwners().getMembers().contains(randomUser));
        assertFalse(grouping.getOwners().isMember(randomUser));

        GroupingsServiceResult ownerAdds =
                memberAttributeService
                        .assignOwnership(GROUPING_0_PATH, users.get(0).getUsername(), randomUser.getUsername());
        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertTrue(grouping.getOwners().getMembers().contains(randomUser));
        assertTrue(grouping.getOwners().isMember(randomUser));
        assertThat(SUCCESS, is(ownerAdds.getResultCode()));

        GroupingsServiceResult adminAdds =
                memberAttributeService.assignOwnership(GROUPING_0_PATH, ADMIN_USER, randomUser.getUsername());
        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertTrue(grouping.getOwners().getMembers().contains(randomUser));
        assertTrue(grouping.getOwners().isMember(randomUser));
        assertThat(adminAdds.getResultCode(), is(SUCCESS));

        //Test to make sure UUID works
        GroupingsServiceResult uuidAdds = memberAttributeService.assignOwnership(GROUPING_0_PATH, ADMIN_USER, "1234");
        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertTrue(grouping.getOwners().getMembers().contains(randomUser));
        assertTrue(grouping.getOwners().isMember(randomUser));
        assertThat(uuidAdds.getResultCode(), is(SUCCESS));
    }

    @Test
    public void getIsOwnerTest() {
        assertTrue(memberAttributeService.getIsOwner(users.get(0).getUsername(), users.get(0).getUsername()));
        assertFalse(memberAttributeService.getIsOwner(users.get(0).getUsername(), users.get(1).getUsername()));

        try {
            memberAttributeService.getIsOwner("zz_zz", users.get(0).getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        try {
            memberAttributeService.getIsOwner(users.get(0).getUsername(), "zz_zz");
        } catch (AccessDeniedException | GcWebServiceError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getIsAdminTest() {
        assertTrue(memberAttributeService.getIsAdmin(ADMIN_USER, ADMIN_USER));
        assertFalse(memberAttributeService.getIsAdmin(ADMIN_USER, users.get(0).getUsername()));

        try {
            memberAttributeService.getIsAdmin("zz_zz", users.get(0).getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
        try {
            memberAttributeService.getIsAdmin(ADMIN_USER, "zz_zz");
        } catch (AccessDeniedException | GcWebServiceError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void checkSelfOptedTest() {

        //user is not in group
        boolean isSelfOpted = memberAttributeService.isSelfOpted(GROUPING_0_INCLUDE_PATH, users.get(2).getUsername());
        assertFalse(isSelfOpted);

        //user has not self opted
        isSelfOpted = memberAttributeService.isSelfOpted(GROUPING_0_INCLUDE_PATH, users.get(5).getUsername());
        assertFalse(isSelfOpted);

        //user has self opted
        Person person = personRepository.findByUsername(users.get(5).getUsername());
        Group group = groupRepository.findByPath(GROUPING_0_INCLUDE_PATH);
        Membership membership = membershipRepository.findByPersonAndGroup(person, group);
        membership.setSelfOpted(true);
        membershipRepository.save(membership);

        isSelfOpted = memberAttributeService.isSelfOpted(GROUPING_0_INCLUDE_PATH, users.get(5).getUsername());
        assertTrue(isSelfOpted);
    }

    @Test
    public void isMemberTest() {
        //test with username
        Person person2 = users.get(2);
        Person person5 = users.get(5);

        assertFalse(memberAttributeService.isMember(GROUPING_0_PATH, person2));
        assertTrue(memberAttributeService.isMember(GROUPING_0_PATH, person5));

        //test with null username
        person2.setUsername(null);
        person5.setUsername(null);

        assertFalse(memberAttributeService.isMember(GROUPING_0_PATH, person2));
        assertTrue(memberAttributeService.isMember(GROUPING_0_PATH, person5));

        //test with uuid
        assertTrue(memberAttributeService.isMember(GROUPING_0_PATH, "5"));
        assertFalse(memberAttributeService.isMember(GROUPING_0_PATH, "1234"));
    }

    @Test
    public void isMemberUuidTest() {

        Person person2 = users.get(2);
        Person person5 = users.get(5);

        assertFalse(memberAttributeService.isMemberUuid(GROUPING_0_PATH, person2.getUhUuid()));
        assertTrue(memberAttributeService.isMemberUuid(GROUPING_0_PATH, person5.getUhUuid()));
    }

    @Test
    public void isOwnerTest() {

        assertFalse(memberAttributeService.isOwner(GROUPING_0_PATH, users.get(1).getUsername()));
        assertTrue(memberAttributeService.isOwner(GROUPING_0_PATH, users.get(0).getUsername()));

    }

    @Test
    public void isAdminTest() {
        assertFalse(memberAttributeService.isAdmin(users.get(1).getUsername()));
        assertTrue(memberAttributeService.isAdmin(ADMIN_USER));
    }

    @Test
    public void isAppTest() {
        assertFalse(memberAttributeService.isApp(users.get(2).getUsername()));
        assertTrue(memberAttributeService.isApp(APP_USER));
    }

    @Test
    public void getMemberAttributesTest() {

        Person personFive = personRepository.findByUsername(users.get(5).getUsername());
        Map<String, String> attributes =
                memberAttributeService.getMemberAttributes(ADMIN_USER, personFive.getUsername());

        assertEquals(personFive.getUsername(), attributes.get(UID));
        assertEquals(personFive.getName(), attributes.get(COMPOSITE_NAME));
        assertEquals(personFive.getUhUuid(), attributes.get(UHUUID));
        assertEquals(personFive.getFirstName(), attributes.get(FIRST_NAME));
        assertEquals(personFive.getLastName(), attributes.get(LAST_NAME));

        String attribute = memberAttributeService.getSpecificUserAttribute(ADMIN_USER, personFive.getUsername(), 0);
        assertThat(attribute, equalTo(personFive.getUsername()));

        // Bogus admin throws an Access Denied Exception.
        try {
            memberAttributeService.getMemberAttributes("bogus admin", personFive.getUsername());
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        // Bogus user returns a map filled with null values.
        Map<String, String> bogusUser = memberAttributeService.getMemberAttributes(ADMIN_USER, "bogus user");
        assertTrue(bogusUser.values().stream().allMatch(Objects::isNull));

    }

    @Test
    public void searchMembersTest() {

        List<Person> membersResults = memberAttributeService.searchMembers(GROUPING_0_PATH, users.get(5).getUsername());
        Person testPerson = personRepository.findByUsername(users.get(5).getUsername());

        assertThat(membersResults.get(0).getUsername(), equalTo(testPerson.getUsername()));
        assertThat(membersResults.get(0).getUhUuid(), equalTo(testPerson.getUhUuid()));
        assertThat(membersResults.get(0).getName(), equalTo(testPerson.getName()));
    }

    @Test
    public void getOwnedGroupingsTest() {
        assertTrue(memberAttributeService.getOwnedGroupings(ADMIN_USER, users.get(0).getUsername()).size() > 0);
        assertFalse(memberAttributeService.getOwnedGroupings(ADMIN_USER, users.get(1).getUsername()).size() > 0);

        try {
            memberAttributeService.getOwnedGroupings(users.get(1).getUsername(), users.get(0).getUsername());
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }

    }

    //assertThat(some long, equalTo(another long));
    @Test
    public void getNumberOfGroupingsTest() {
        List<GroupingPath> groupingPathList = memberAttributeService.getOwnedGroupings(ADMIN_USER, users.get(0).getUsername());

        //Test a owner that owns 5 groupings
        assertThat(groupingPathList.size(), equalTo(memberAttributeService.getNumberOfGroupings(ADMIN_USER, users.get(0).getUsername())));

        //Test a owner that owns 0 groupings
        groupingPathList = memberAttributeService.getOwnedGroupings(ADMIN_USER, users.get(3).getUsername());
        assertThat(groupingPathList.size(), equalTo(memberAttributeService.getNumberOfGroupings(ADMIN_USER, users.get(3).getUsername())));

        //Test that two different users w/ different number of groupings don't return the same number of groupings
        groupingPathList = memberAttributeService.getOwnedGroupings(ADMIN_USER, users.get(1).getUsername());
        assertThat(groupingPathList.size(), is(not(memberAttributeService.getNumberOfGroupings(ADMIN_USER, users.get(0).getUsername()))));

    }
}
