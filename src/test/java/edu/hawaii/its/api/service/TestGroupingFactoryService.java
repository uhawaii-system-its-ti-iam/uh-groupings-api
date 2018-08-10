package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import static org.junit.Assert.assertTrue;


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
//    public void setUp() {
//        groupAttributeService.changeListservStatus(GROUPING, username[0], true);
//        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
//        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);
//
//        //put in include
//        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[0]);
//        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[1]);
//        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[2]);
//
//        //remove from exclude
//        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[4]);
//        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[5]);
//
//        //add to exclude
//        membershipService.deleteGroupingMemberByUsername(username[0], GROUPING, username[3]);
//    }

    @Test
    public void addGroupingTest() {

        List<String>  basis = new ArrayList<>();
        List<String>  include = new ArrayList<>();
        List<String>  exclude = new ArrayList<>();
        List<String>  owners = new ArrayList<>();

        include.add("zknoebel");
        owners.add("kahlin");
        //basis.add("jiaqil");
        exclude.add("clintmor");


        //this needs to be an admin account to work
        groupingFactoryService.addGroupingVoid(ADMIN, KAHLIN_TEST + ":kahlin-test", basis, include, exclude, owners);

//        membershipService.addGroupMember(ADMIN, KAHLIN_TEST + ":kahlin-test:owners", "watarub");

    }

    @Test
    public void deleteGroupingTest() {

        groupingFactoryService.deleteGroupingVoid(ADMIN, "hawaii.edu:custom:test:kahlin:kahlin-test:owners");

    }
}
