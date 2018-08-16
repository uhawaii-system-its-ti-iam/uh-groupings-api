package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.hamcrest.Matchers;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.AssertTrue;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
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

    @Value("${groupings.api.test.grouping_kahlin}")
    private String KAHLIN_TEST;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

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

    @Test
    public void addGroupingTest() {

        List<GroupingsServiceResult> results = new ArrayList<>();
        GroupingsServiceResult sResults;



        //Works correctly
        assertThat(memberAttributeService.isAdmin(ADMIN), equalTo(true));

        results = groupingFactoryService.addGrouping(ADMIN, KAHLIN_TEST);

        assertThat(groupingFactoryService.pathIsEmpty(ADMIN, KAHLIN_TEST),
                equalTo(false));


        //Fails when the grouping already exists
        try {
            results = groupingFactoryService.addGrouping(ADMIN, KAHLIN_TEST);

        } catch (GroupingsServiceResultException gsre) {

            sResults = gsre.getGsr();
            assertThat(sResults.getResultCode(), startsWith(FAILURE));
        }

        //delete the grouping
        groupingFactoryService.deleteGrouping(ADMIN, KAHLIN_TEST);



        //Fails when user trying to add grouping is not admin
        try {

            results = groupingFactoryService.addGrouping("sbraun", KAHLIN_TEST + ":kahlin-test");

        } catch (GroupingsServiceResultException gsre) {

            sResults = gsre.getGsr();
            assertThat(sResults.getResultCode(), startsWith(FAILURE));
        }





    }
    @Test
    public void deleteGroupingTest() {

        List<GroupingsServiceResult> results = new ArrayList<>();
        GroupingsServiceResult sResults;

        //add the grouping
        groupingFactoryService.addGrouping(ADMIN, KAHLIN_TEST);

        //Works correctly
        assertThat(memberAttributeService.isAdmin(ADMIN), equalTo(true));

        results = groupingFactoryService.deleteGrouping(ADMIN, KAHLIN_TEST);

        assertThat(groupingFactoryService.pathIsEmpty(ADMIN, KAHLIN_TEST),
                equalTo(true));


        //Fails when the grouping doesn't exists
        try {
            results = groupingFactoryService.deleteGrouping(ADMIN, KAHLIN_TEST);

        } catch (GroupingsServiceResultException gsre) {

            sResults = gsre.getGsr();
            assertThat(sResults.getResultCode(), startsWith(FAILURE));
        }



        //Fails when user trying to delete grouping is not admin
        try {

            results = groupingFactoryService.deleteGrouping("sbraun", KAHLIN_TEST + ":kahlin-test");

        } catch (GroupingsServiceResultException gsre) {

            sResults = gsre.getGsr();
            assertThat(sResults.getResultCode(), startsWith(FAILURE));
        }
    }
}
