package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Grouping;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestHelperService {

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MembershipService membershipService;

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
    public void setUp() {
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, ADMIN, true);
        groupAttributeService.changeOptOutStatus(GROUPING, ADMIN, true);

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

    @Test
    public void membershipResultsTest() {
        // username is in group
        membershipService.addGroupMember(ADMIN, GROUPING_INCLUDE, username[1]);
        WsGetMembershipsResults results = helperService.membershipsResults(username[1], GROUPING);
        assertFalse(results.getWsMemberships().equals(null));

        // username is not in group
        membershipService.deleteGroupingMemberByUsername(ADMIN, GROUPING, username[3]);
        try {
            results = helperService.membershipsResults(username[3], GROUPING);
            assertTrue(results.getWsMemberships().equals(null));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        // username doesn't exist
        try {
            results = helperService.membershipsResults("someName", GROUPING);
            assertTrue(results.getWsMemberships().equals(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // username is null
        try {
            results = helperService.membershipsResults(null, GROUPING);
            assertTrue(results.getWsMemberships().equals(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // group doesn't exist
        try {
            results = helperService.membershipsResults(username[1], "someGroup");
            assertTrue(results.getWsMemberships().equals(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // group is null
        try {
            results = helperService.membershipsResults(username[1], null);
            assertTrue(results.getWsMemberships().equals(null));
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Test
    public void extractGroupingsTest() {

        List<String> groupPaths = new ArrayList<>();

        // Empty list
        List<String> groupings = helperService.extractGroupings(groupPaths);
        assertTrue(groupings.size() == 0);

        // Extract Groups that exist
        groupPaths.add(GROUPING);
        groupPaths.add(GROUPING_STORE_EMPTY);
        groupPaths.add(GROUPING_TRUE_EMPTY);
        groupings = helperService.extractGroupings(groupPaths);
        assertTrue(groupings.size() == 3);

        //Extract group that doesn't exist
        try {
            List<String> groupPathsDontExist = new ArrayList<>();
            groupPathsDontExist.add("someGroup");
            groupings = helperService.extractGroupings(groupPathsDontExist);
            assertTrue(groupings.size() == 0);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        //Extract null group
        try {
            List<String> groupPathsNull = new ArrayList<>();
            groupPathsNull.add(null);
            groupings = helperService.extractGroupings(groupPathsNull);
            assertTrue(groupings.size() == 0);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }
}
