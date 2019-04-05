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

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
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

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @Value("${groupings.api.test.uuid}")
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
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
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
        assertEquals(ownerAdds.getResultCode(), SUCCESS);

        GroupingsServiceResult adminAdds =
                memberAttributeService.assignOwnership(GROUPING_0_PATH, ADMIN_USER, randomUser.getUsername());
        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertTrue(grouping.getOwners().getMembers().contains(randomUser));
        assertTrue(grouping.getOwners().isMember(randomUser));
        assertEquals(SUCCESS, adminAdds.getResultCode());

        //Test to make sure UUID works
        GroupingsServiceResult uuidAdds = memberAttributeService.assignOwnership(GROUPING_0_PATH, ADMIN_USER, "1234");
        grouping = groupingRepository.findByPath(GROUPING_0_PATH);
        assertTrue(grouping.getOwners().getMembers().contains(randomUser));
        assertTrue(grouping.getOwners().isMember(randomUser));
        assertEquals(SUCCESS, uuidAdds.getResultCode());
    }

    @Test
    public void removeOwnershipTest() {
        GroupingsServiceResult randomUserRemoves;

        try {
            //non-owner/non-admin tries to remove ownership
            randomUserRemoves = memberAttributeService
                    .removeOwnership(GROUPING_0_PATH, users.get(1).getUsername(), users.get(1).getUsername());
        } catch (AccessDeniedException ade) {
            assertEquals(ade.getMessage(), INSUFFICIENT_PRIVILEGES);
        }

        //add owner for owner to remove
        membershipService.addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_0_OWNERS_PATH,
                users.get(1).getUsername());

        //owner tries to remove other ownership
        GroupingsServiceResult ownerRemoves = memberAttributeService
                .removeOwnership(GROUPING_0_PATH, users.get(0).getUsername(), users.get(1).getUsername());
        assertEquals(SUCCESS, ownerRemoves.getResultCode());

        //try to remove ownership from user that is not an owner
        GroupingsServiceResult ownerRemovesNonOwner = memberAttributeService
                .removeOwnership(GROUPING_0_PATH, users.get(0).getUsername(), users.get(1).getUsername());
        assertEquals(SUCCESS, ownerRemovesNonOwner.getResultCode());

        //add owner for admin to remove
        membershipService.addGroupMemberByUsername(users.get(0).getUsername(), GROUPING_0_OWNERS_PATH,
                users.get(1).getUsername());

        //admin tries to remove ownership
        GroupingsServiceResult adminRemoves =
                memberAttributeService.removeOwnership(GROUPING_0_PATH, ADMIN_USER, users.get(1).getUsername());
        assertEquals(adminRemoves.getResultCode(), SUCCESS);
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

        assertFalse(memberAttributeService.isMemberUuid(GROUPING_0_PATH, person2.getUuid()));
        assertTrue(memberAttributeService.isMemberUuid(GROUPING_0_PATH, person5.getUuid()));
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
    public void isSuperuserTest() {
        assertFalse(memberAttributeService.isSuperuser(users.get(2).getUsername()));
        assertTrue(memberAttributeService.isSuperuser(ADMIN_USER));

        assertTrue(memberAttributeService.isSuperuser(APP_USER));
    }

    @Test
    public void getUserAttributesTest() {

        String username = users.get(5).getUsername();
        Person personFive = personRepository.findByUsername(users.get(5).getUsername());

        Map<String, String> attributes = memberAttributeService.getUserAttributes(ADMIN_USER, username);

        assertThat(attributes.get(UID), equalTo(personFive.getUsername()));
        assertThat(attributes.get(COMPOSITE_NAME), equalTo(personFive.getName()));
        assertThat(attributes.get(UHUUID), equalTo(personFive.getUuid()));
        assertThat(attributes.get(FIRST_NAME), equalTo(personFive.getFirstName()));
        assertThat(attributes.get(LAST_NAME), equalTo(personFive.getLastName()));

        // Test with user that owns no groupings
        Map<String, String> emptyAttributes = memberAttributeService.getUserAttributes(users.get(3).getUsername(), username);

        assertThat(emptyAttributes.get(UID), equalTo(""));
        assertThat(emptyAttributes.get(COMPOSITE_NAME), equalTo(""));
        assertThat(emptyAttributes.get(UHUUID), equalTo(""));
        assertThat(emptyAttributes.get(FIRST_NAME), equalTo(""));
        assertThat(emptyAttributes.get(LAST_NAME), equalTo(""));

        // Test with null username
        try{
            Map<String, String> nullPersonAttributes = memberAttributeService.getUserAttributes(ADMIN_USER, null);
            fail("Shouldn't be here.");
        } catch (GcWebServiceError gce) {
            assertThat(gce.getContainerResponseObject(), equalTo("Error 404 Not Found"));
        }
    }

    @Test
    public void searchMembersTest() {

        List<Person> membersResults = memberAttributeService.searchMembers(GROUPING_0_PATH, users.get(5).getUsername());
        Person testPerson = personRepository.findByUsername(users.get(5).getUsername());

        assertThat(membersResults.get(0).getUsername(), equalTo(testPerson.getUsername()));
        assertThat(membersResults.get(0).getUuid(), equalTo(testPerson.getUuid()));
        assertThat(membersResults.get(0).getName(), equalTo(testPerson.getName()));
    }
}
