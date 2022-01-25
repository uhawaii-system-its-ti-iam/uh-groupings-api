package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Grouping;

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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

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

    @Test
    public void nameGroupingPathTest() {
        assertEquals("grouping-test-path", helperService.nameGroupingPath("test:grouping-test-path:include"));
    }

    @Test public void memberAttributeMapSetKeysTest() {
        Map<String, String> map = helperService.memberAttributeMapSetKeys();

        // Check if keys are correct, and that values are set to null.
        String[] subjectAttributeNames = { UID, COMPOSITE_NAME, LAST_NAME, FIRST_NAME, UHUUID };
        for (String subjectAttributeName : subjectAttributeNames) {
            assertTrue(map.containsKey(subjectAttributeName));
            assertNull(map.get(subjectAttributeName));
        }
    }
}
