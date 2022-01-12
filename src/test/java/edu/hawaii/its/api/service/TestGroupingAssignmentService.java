package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Person;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingAssignmentService {

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

    @Value("${groupings.api.test.grouping_store_empty}")
    private String GROUPING_STORE_EMPTY;
    @Value("${groupings.api.test.grouping_store_empty_include}")
    private String GROUPING_STORE_EMPTY_INCLUDE;
    @Value("${groupings.api.test.grouping_store_empty_exclude}")
    private String GROUPING_STORE_EMPTY_EXCLUDE;
    @Value("${groupings.api.test.grouping_store_empty_owners}")
    private String GROUPING_STORE_EMPTY_OWNERS;

    @Value("${groupings.api.test.grouping_true_empty}")
    private String GROUPING_TRUE_EMPTY;
    @Value("${groupings.api.test.grouping_true_empty_include}")
    private String GROUPING_TRUE_EMPTY_INCLUDE;
    @Value("${groupings.api.test.grouping_true_empty_exclude}")
    private String GROUPING_TRUE_EMPTY_EXCLUDE;
    @Value("${groupings.api.test.grouping_true_empty_owners}")
    private String GROUPING_TRUE_EMPTY_OWNERS;

    @Value("${groupings.api.test.grouping_timeout_test}")
    private String GROUPING_TIMEOUT;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.test.usernames}")
    private String[] usernames;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    public final Log logger = LogFactory.getLog(GroupingAssignmentServiceImpl.class);

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private HelperService helperService;

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
        // assign ownership
        memberAttributeService.assignOwnership(GROUPING_STORE_EMPTY, ADMIN, usernames[0]);
        memberAttributeService.assignOwnership(GROUPING_TRUE_EMPTY, ADMIN, usernames[0]);
        memberAttributeService.assignOwnership(GROUPING, ADMIN, usernames[0]);

        // update statuses
        groupAttributeService.changeGroupAttributeStatus(GROUPING, usernames[0], LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, usernames[0], true);

        // put in include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(usernames[0]);
        includeNames.add(usernames[1]);
        includeNames.add(usernames[2]);
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, includeNames);

        // remove from exclude
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[4]));
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[5]));

        // add to exclude
        membershipService.addGroupMembers(usernames[0], GROUPING_EXCLUDE, Collections.singletonList(usernames[3]));

    }

    @Test
    public void adminListsTest() {
        try {
            // Try with non-admin.
            groupingAssignmentService.adminLists(usernames[0]);
            fail("shouldn't be here");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        try {
            // Try with admin.
            AdminListsHolder adminList = groupingAssignmentService.adminLists(ADMIN);
            Group adminGroup = adminList.getAdminGroup();

            // Assert that admins list was retrieved.
            assertThat(adminGroup.getUsernames().size(), is(not(0)));
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void getGroupingTest() {

        // usernames[4] does not own grouping, method should return empty grouping
        try {
            groupingAssignmentService.getGrouping(GROUPING, usernames[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        Grouping grouping = groupingAssignmentService.getGrouping(GROUPING, usernames[0]);

        assertThat(GROUPING, is(grouping.getPath()));

        // Testing for garbage uuid basis bug fix
        // List<String> list = grouping.getBasis().getUuids();

        assertTrue(grouping.getBasis().getUsernames().contains(usernames[3]));
        assertTrue(grouping.getBasis().getUsernames().contains(usernames[4]));
        assertTrue(grouping.getBasis().getUsernames().contains(usernames[5]));

        assertTrue(grouping.getComposite().getUsernames().contains(usernames[0]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[1]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[2]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[4]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[5]));

        assertTrue(grouping.getExclude().getUsernames().contains(usernames[3]));

        assertTrue(grouping.getInclude().getUsernames().contains(usernames[0]));
        assertTrue(grouping.getInclude().getUsernames().contains(usernames[1]));
        assertTrue(grouping.getInclude().getUsernames().contains(usernames[2]));

        assertTrue(grouping.getOwners().getUsernames().contains(usernames[0]));
    }

    @Test
    public void getPaginatedGroupingTest() {

        // Paging starts at 1 D:
        // Page 1 contains 3 stale subjects, should return 17
        Grouping paginatedGroupingPage1 =
                groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 1, 20, "name", true);
        //        // Page 2 contains 1 stale subject, should return 19
        Grouping paginatedGroupingPage2 =
                groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 2, 20, "name", false);

        Grouping normalGrouping =
                groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], null, null, null, null);

        // Check to see the pages come out the right sizes
        assertThat(paginatedGroupingPage1.getBasis().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getInclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getExclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getComposite().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getOwners().getMembers().size(), lessThanOrEqualTo(20));

        assertThat(paginatedGroupingPage2.getBasis().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getInclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getExclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getComposite().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getOwners().getMembers().size(), lessThanOrEqualTo(20));

        // Both pages should not be the same (assuming no groups are empty)
        assertThat(paginatedGroupingPage1.getBasis(), not(paginatedGroupingPage2.getBasis()));
        assertThat(paginatedGroupingPage1.getInclude(), not(paginatedGroupingPage2.getInclude()));
        assertThat(paginatedGroupingPage1.getExclude(), not(paginatedGroupingPage2.getExclude()));
        assertThat(paginatedGroupingPage1.getComposite(), not(paginatedGroupingPage2.getComposite()));
        assertThat(paginatedGroupingPage1.getOwners(), not(paginatedGroupingPage2.getOwners()));

        // Test if sorted properly (sorted ascending should have the names start with "A", sorted descending should not)
        assertThat(paginatedGroupingPage1.getBasis().getMembers().get(0).getName(), startsWith("A"));
        assertThat(paginatedGroupingPage2.getBasis().getMembers().get(0).getName(), not(startsWith("A")));

        // Test paging without proper permissions (should return empty)
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[1], 1, 20, "name", true);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    // Testing why getting a grouping returns different results for a page of the size of the entire grouping
    // Results are the pagination automatically removes stale subjects for us, but doesn't get the full page
    // Plan is to leave as is, and some pages will be shorter than others
    // Maybe UI can show messages of some sort to say this is the case
    @Ignore
    @Test
    public void paginatedVersusNonpaginatedTest() {
        Grouping groupingNonPaginated = groupingAssignmentService.getGrouping(GROUPING, usernames[0]);
        groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 1, 369, null, null);

        List<Person> nonPaginatedBasisMembers = groupingNonPaginated.getBasis().getMembers();

        List<String> uuids = new ArrayList<>();

        for (Person p : nonPaginatedBasisMembers) {
            uuids.add(p.getUhUuid());
        }

        Collections.sort(uuids);
    }

    @Test
    public void paginatedLargeGroupingTest() {

        for (int i = 1; i <= 150; i++) {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], i, 20, "name", true);
        }
    }

    @Test
    public void groupingsOptedTest() {
        // Create groupings list, then add 3 test groupings to the list.
        List<String> groupings = new ArrayList<>();
        groupings.add(GROUPING_INCLUDE);
        groupings.add(GROUPING_STORE_EMPTY_INCLUDE);
        groupings.add(GROUPING_TRUE_EMPTY_INCLUDE);

        // Add user to individual group then set then assign the self opted attribute to user.
        membershipService.addGroupMembers(usernames[0], GROUPING_STORE_EMPTY_INCLUDE,
                Collections.singletonList(usernames[0]));
        membershipService.addSelfOpted(GROUPING_STORE_EMPTY_INCLUDE, usernames[0]);

        // Add user to individual group then set then assign the self opted attribute to user.
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[0]));
        membershipService.addSelfOpted(GROUPING_INCLUDE, usernames[0]);

        // Add user to individual group then set then assign self opted attribute to user.
        membershipService
                .addGroupMembers(usernames[0], GROUPING_TRUE_EMPTY_INCLUDE, Collections.singletonList(usernames[0]));
        membershipService.addSelfOpted(GROUPING_TRUE_EMPTY_INCLUDE, usernames[0]);

        // Call groupingsOpted, passing in the list of groups just constructed which will return a list of opted groupings.
        List<Grouping> optedGroups = groupingAssignmentService.groupingsOpted("include", usernames[0], groupings);

        // Returned opted groups, should be 3.
        assertTrue(optedGroups.size() == 3);

        // Opt out one of the groups.
        membershipService.optOut(usernames[0], GROUPING, usernames[0]);

        // Call groupingsOpted once more to get refreshed list of opted groups.
        optedGroups = groupingAssignmentService.groupingsOpted("include", usernames[0], groupings);

        // Amount of opted groups return should be 1 less.
        assertTrue(optedGroups.size() == 2);
    }

    @Test
    public void getOptOutGroupsTest() {
        List<String> optOutPaths = groupingAssignmentService.getOptOutGroups(usernames[0], usernames[1]);
        assertTrue(optOutPaths.contains(GROUPING));
        Set<String> pathMap = new HashSet<>();
        for (String path : optOutPaths) {
            // The path should be a parent path.
            assertFalse(path.endsWith(INCLUDE));
            assertFalse(path.endsWith(EXCLUDE));
            assertFalse(path.endsWith(BASIS));
            assertFalse(path.endsWith(OWNERS));
            // Check for duplicates.
            assertTrue(pathMap.add(path));
        }
    }

    @Test
    public void getOptInGroupsTest() {
        List<String> optInPaths = groupingAssignmentService.getOptInGroups(usernames[0], usernames[1]);
        assertTrue(optInPaths.contains(GROUPING));
        Set<String> pathMap = new HashSet<>();
        for (String path : optInPaths) {
            // The path should be a parent path.
            assertFalse(path.endsWith(INCLUDE));
            assertFalse(path.endsWith(EXCLUDE));
            assertFalse(path.endsWith(BASIS));
            assertFalse(path.endsWith(OWNERS));
            // Check for duplicates.
            assertTrue(pathMap.add(path));
        }
    }

    @Test
    public void getMembersTest() {

        // Testing for garbage uuid basis bug fix
        // Group testGroup = groupingAssignmentService.getMembers(usernames[0], GROUPING_BASIS);

        List<String> groupings = new ArrayList<>();
        groupings.add(GROUPING);
        Group group = groupingAssignmentService.getMembers(usernames[0], groupings).get(GROUPING);
        List<String> usernames = group.getUsernames();

        assertTrue(usernames.contains(this.usernames[0]));
        assertTrue(usernames.contains(this.usernames[1]));
        assertTrue(usernames.contains(this.usernames[2]));
        assertFalse(usernames.contains(this.usernames[3]));
        assertTrue(usernames.contains(this.usernames[4]));
        assertTrue(usernames.contains(this.usernames[5]));
    }

    @Test
    public void getGroupNamesTest() {
        List<String> groupNames1 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[1]);
        List<String> groupNames3 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[3]);

        //usernames[1] should be in the composite and the include, not basis or exclude
        assertTrue(groupNames1.contains(GROUPING));
        assertTrue(groupNames1.contains(GROUPING_INCLUDE));
        assertFalse(groupNames1.contains(GROUPING_BASIS));
        assertFalse(groupNames1.contains(GROUPING_EXCLUDE));

        //usernames[3] should be in the basis and exclude, not the composite or include
        assertTrue(groupNames3.contains(GROUPING_BASIS));
        assertTrue(groupNames3.contains(GROUPING_EXCLUDE));
        assertFalse(groupNames3.contains(GROUPING));
        assertFalse(groupNames3.contains(GROUPING_INCLUDE));
    }

    @Test
    public void getGroupPathsTest() {
        List<String> paths = groupingAssignmentService.getGroupPaths(ADMIN, "gilbertz");
        for (String path : paths) {
            System.err.println(path);
        }
    }

    @Test
    public void getGroupNames() {
        List<String> groups = groupingAssignmentService.getGroupPaths(ADMIN, usernames[0]);

        assertTrue(groups.contains(GROUPING_OWNERS));
        assertTrue(groups.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups2 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[1]);

        assertFalse(groups2.contains(GROUPING_OWNERS));
        assertFalse(groups2.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertFalse(groups2.contains(GROUPING_TRUE_EMPTY_OWNERS));
    }

    @Test
    public void getGroupPathsPermissionsTest() {
        List<String> groups = groupingAssignmentService.getGroupPaths(ADMIN, usernames[0]);

        assertTrue(groups.contains(GROUPING_OWNERS));
        assertTrue(groups.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups2 = groupingAssignmentService.getGroupPaths(usernames[0], usernames[0]);

        assertTrue(groups2.contains(GROUPING_OWNERS));
        assertTrue(groups2.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups2.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups3 = groupingAssignmentService.getGroupPaths(usernames[1], usernames[0]);
        assertThat(groups3.size(), equalTo(0));
    }

    @Test
    public void makeGroupingsTest() {
        List<String> groupingPaths = new ArrayList<>();
        groupingPaths.add(GROUPING);
        groupingPaths.add(GROUPING_STORE_EMPTY);
        groupingPaths.add(GROUPING_TRUE_EMPTY);

        List<Grouping> groupings = helperService.makeGroupings(groupingPaths);

        assertTrue(groupings.size() == 3);
    }
}
