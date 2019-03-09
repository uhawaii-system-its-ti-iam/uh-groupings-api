package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
public class TestGroupingFactoryService {

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


    @Value("${groupings.api.test.grouping_new}")
    private String GROUPING_NEW;
    @Value("${groupings.api.test.grouping_new_basis}")
    private String GROUPING_NEW_BASIS;
    @Value("${groupings.api.test.grouping_new_include}")
    private String GROUPING_NEW_INCLUDE;
    @Value("${groupings.api.test.grouping_new_exclude}")
    private String GROUPING_NEW_EXCLUDE;
    @Value("${groupings.api.test.grouping_new_owners}")
    private String GROUPING_NEW_OWNERS;

    @Value("${groupings.api.test.grouping_temp_test}")
    private String TEMP_TEST;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.purge_grouping}")
    private String PURGE;

    @Value("${groupings.api.test.usernames}")
    private String[] USERNAMES;

    @Value("${grouperClient.webService.login}")
    private String APP_USER;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GroupingFactoryService groupingFactoryService;

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
        // Make sure the grouping folder is cleared
        if (!groupingFactoryService.isPathEmpty(APP_USER, TEMP_TEST)) {
            groupingFactoryService.deleteGrouping(APP_USER, TEMP_TEST);
        }
    }

    @Test
    public void constructorTest() {
        assertNotNull(groupingFactoryService);
    }

    // todo running this with out a stem gives an error "Cant find stem: ..." - make sure the code adds this stem if
    // necessary
    // todo the code should give admin privileges to the groupingSuperusers group
    // todo Don't run these tests quite yet as these functions shouldn't be used for now.
//    @Test
//    public void addGroupingTest() {
//
//        GroupingsServiceResult sResults;
//
//        //Works correctly
//        assertTrue(memberAttributeService.isSuperuser(APP_USER));
//
//        List<GroupingsServiceResult> results = groupingFactoryService.addGrouping(APP_USER, TEMP_TEST);
//
//        assertFalse(groupingFactoryService.isPathEmpty(APP_USER, TEMP_TEST));
//
//        //Fails when the grouping already exists
//        try {
//            results = groupingFactoryService.addGrouping(APP_USER, TEMP_TEST);
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            sResults = gsre.getGsr();
//            assertThat(sResults.getResultCode(), startsWith(FAILURE));
//        }
//
//        //delete the grouping
//        groupingFactoryService.deleteGrouping(APP_USER, TEMP_TEST);
//
//
//        //Fails when user trying to add grouping is not admin
//        try {
//
//            results = groupingFactoryService.addGrouping(USERNAMES[1], TEMP_TEST + ":kahlin-test");
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            sResults = gsre.getGsr();
//            assertThat(sResults.getResultCode(), startsWith(FAILURE));
//        }
//
//
//    }
//
//    @Test
//    public void deleteGroupingTest() {
//
//        List<GroupingsServiceResult> results = new ArrayList<>();
//        GroupingsServiceResult sResults;
//
//        //add the grouping
//        groupingFactoryService.addGrouping(APP_USER, TEMP_TEST);
//
//        //Works correctly
//        assertThat(memberAttributeService.isSuperuser(APP_USER), equalTo(true));
//
//        results = groupingFactoryService.deleteGrouping(APP_USER, TEMP_TEST);
//
//        assertThat(groupingFactoryService.isPathEmpty(APP_USER, TEMP_TEST),
//                equalTo(true));
//
//
//        //Fails when the grouping doesn't exists
//        try {
//            results = groupingFactoryService.deleteGrouping(APP_USER, TEMP_TEST);
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            sResults = gsre.getGsr();
//            assertThat(sResults.getResultCode(), startsWith(FAILURE));
//        }
//
//
//        //Fails when user trying to delete grouping is not admin
//        try {
//            // TODO: how do we know that this grouping exists?
//            results = groupingFactoryService.deleteGrouping(USERNAMES[1], TEMP_TEST + ":kahlin-test");
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            sResults = gsre.getGsr();
//            assertThat(sResults.getResultCode(), startsWith(FAILURE));
//        }
//    }
//
//    @Test
//    public void markPurgeTest() {
//
//        // make sure the grouping does not exist
//        assertTrue(groupingFactoryService.isPathEmpty(APP_USER, TEMP_TEST));
//
//        // add new grouping
//
//        groupingFactoryService.addGrouping(APP_USER, TEMP_TEST);
//
//        // currently we only support marking a grouping for purge if the user is a super user
//        assertTrue(memberAttributeService.isSuperuser(APP_USER));
//
//        groupingFactoryService.markGroupForPurge(APP_USER, TEMP_TEST);
//
//        // todo change this to has purge attribute
//        assertTrue(groupAttributeService.isGroupHasAttribute(TEMP_TEST, PURGE));
//
//        //Fails when the grouping doesn't exists
//        try {
//            // delete grouping
//            groupingFactoryService.deleteGrouping(APP_USER, TEMP_TEST);
//            assertTrue(groupingFactoryService.isPathEmpty(APP_USER, TEMP_TEST));
//
//            // try to mark for purge
//            groupingFactoryService.markGroupForPurge(APP_USER, TEMP_TEST);
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            assertThat(gsre.getGsr().getResultCode(), startsWith(FAILURE));
//        }
//
//
//        //Fails when user trying to delete grouping is not admin
//        try {
//
//            groupingFactoryService.markGroupForPurge(USERNAMES[1], TEMP_TEST + ":kahlin-test");
//
//        } catch (GroupingsServiceResultException gsre) {
//
//            assertThat(gsre.getGsr().getResultCode(), startsWith(FAILURE));
//        }
//
//    }
//
//    @Test
//    public void privilegesTest(){
//
//            groupingFactoryService.privilegegTets(APP_USER, "hawaii.edu:custom:test:kahlin:privilegeTesting");
//
//    }

}
