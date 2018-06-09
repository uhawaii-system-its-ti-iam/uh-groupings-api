package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMembershipService {

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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

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
        groupAttributeService.changeListservStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);

        //put in include
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[0]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[1]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[2]);

        //remove from exclude
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[4]);
        membershipService.addGroupingMemberByUsername(username[0], GROUPING, username[5]);

        //add to exclude
        membershipService.deleteGroupingMemberByUsername(username[0], GROUPING, username[3]);

        //        // Remove from include
        //        for(int i = 0; i < 6; i++) {
        //            membershipService.deleteGroupMemberByUsername(username[0], GROUPING_INCLUDE, username[i]);
        //        }

        //        try {
        //            membershipService.deleteAdmin(ADMIN, username[3]);
        //        } catch (GroupingsServiceResultException gsre) {
        //            gsre.printStackTrace();
        //        }
    }

    @Test
    public void groupOptInPermissionTest() {
        assertTrue(membershipService.groupOptInPermission(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.groupOptInPermission(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void groupOptOutPermissionTest() {
        assertTrue(membershipService.groupOptOutPermission(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.groupOptOutPermission(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void updateLastModifiedTest() {
        // Test is accurate to the minute, and if checks to see if the current
        // time gets added to the lastModified attribute of a group if the
        // minute happens to change in between getting the time and setting
        // the time, the test will fail.

        final String group = GROUPING_INCLUDE;

        GroupingsServiceResult gsr = membershipService.updateLastModified(group);
        String dateStr = gsr.getAction().split(" to time ")[1];

        WsGetAttributeAssignmentsResults assignments =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, group, YYYYMMDDTHHMM);
        String assignedValue = assignments.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        assertEquals(dateStr, assignedValue);
    }

    @Test
    public void optTest() {

        //tst[3] is not in the composite or include, but is in the basis and exclude
        //tst[3] is not self opted into the exclude
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));

        //tst[3] opts in to the Grouping
        membershipService.optIn(username[3], GROUPING);
        //tst[3] should still be in the basis and now also in the Grouping
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        //tst[3] is no longer in the exclude, and because tst[3] is in the basis,
        //tst[3] does not get added to the include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //tst[3] opts out of the Grouping
        membershipService.optOut(username[3], GROUPING);
        //tst[3] is still in basis, now in exclude and not in Grouping or include
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        //tst[3] is now self opted into exclude
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));

        //reset group
        membershipService.removeSelfOpted(GROUPING_EXCLUDE, username[3]);
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));
    }

    //Issue with not finding group on the server when calling is owner while getGroupPaths is able to find them
    @Test
    public void listOwnedTest() {

        // Tests that when there is no groups owned, the list is empty
        assertTrue(membershipService.listOwned(ADMIN, username[1]).isEmpty());

        // Adds user to owners of GROUPING 1
        membershipService.addGroupMember(username[0], GROUPING_OWNERS, username[1]);

        // Tests that the list now contains the path to GROUPING 1 since user is now an owner
        assertTrue(membershipService.listOwned(ADMIN, username[1]).get(0).equals(GROUPING));

        // Tests if a non admin can access users groups owned
        assertTrue(membershipService.listOwned(username[0], username[1]).isEmpty());

        //Reset ownership
        membershipService.deleteGroupMemberByUsername(username[0], GROUPING_OWNERS, username[1]);
    }

    @Test
    public void addRemoveSelfOptedTest() {

        //username[2] is not in the include, but not self opted
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //add the self opted attribute for username[2]'s membership for the include group
        membershipService.addSelfOpted(GROUPING_INCLUDE, username[2]);

        //username[2] should now be self opted
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //remove the self opted attribute for username[2]'s membership from the include group
        membershipService.removeSelfOpted(GROUPING_INCLUDE, username[2]);

        //username[2] should no longer be self opted into the include
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //try to add self opted attribute when not in the group
        GroupingsServiceResult groupingsServiceResult;

        try {
            groupingsServiceResult = membershipService.addSelfOpted(GROUPING_EXCLUDE, username[2]);
        } catch (GroupingsServiceResultException gsre) {
            groupingsServiceResult = gsre.getGsr();
        }
        assertTrue(groupingsServiceResult.getResultCode().startsWith(FAILURE));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[2]));
    }

    @Test
    public void groupOptPermissionTest() {
        assertTrue(membershipService.groupOptOutPermission(username[0], GROUPING_INCLUDE));
        assertTrue(membershipService.groupOptOutPermission(username[0], GROUPING_EXCLUDE));

        assertTrue(membershipService.groupOptInPermission(username[0], GROUPING_INCLUDE));
        assertTrue(membershipService.groupOptInPermission(username[0], GROUPING_EXCLUDE));
    }

    @Test
    public void addMemberAsTest() {

        //username[3] is in the basis and exclude, not the composite or include
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //an owner adds username[3] to the include group
        List<GroupingsServiceResult> addMember =
                membershipService.addGroupMemberByUsername(username[0], GROUPING_INCLUDE, username[3]);

        //the addition was successful
        assertTrue(addMember.get(0).getResultCode().startsWith(SUCCESS));
        //username[3] is in the basis, include and composite, not the exclude
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //put username[3] back in the exclude group
        addMember = membershipService.addGroupMemberByUsername(username[0], GROUPING_EXCLUDE, username[3]);

        //the addition was successful
        assertEquals(addMember.get(0).getResultCode(), SUCCESS);
        //username[3] is in the basis and exclude, not the composite or include
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //test adding when already in group
        addMember = membershipService.addGroupMemberByUsername(username[0], GROUPING_EXCLUDE, username[3]);
        //the addition was successful
        assertTrue(addMember.get(0).getResultCode().startsWith(SUCCESS));
        //username[3] is in the basis and exclude, not the composite or include
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
    }

    @Test
    public void listGroupsTest() {
        //todo
    }

    @Test
    public void getMembersTest() {
        Group group = groupingAssignmentService.getMembers(username[0], GROUPING);
        List<String> usernames = group.getUsernames();

        assertTrue(usernames.contains(username[0]));
        assertTrue(usernames.contains(username[1]));
        assertTrue(usernames.contains(username[2]));
        assertFalse(usernames.contains(username[3]));
        assertTrue(usernames.contains(username[4]));
        assertTrue(usernames.contains(username[5]));
    }

    @Test
    public void deleteMemberAsTest() {
        //username[2] is in composite and include, not basis or exclude
        assertTrue(memberAttributeService.isMember(GROUPING, username[2]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //username[3] is in basis and exclude, not composite or include
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //delete username[3] from exclude
        GroupingsServiceResult deleteMember1 =
                membershipService.deleteGroupMemberByUsername(username[0], GROUPING_EXCLUDE, username[3]);
        //deletion was successful
        assertEquals(deleteMember1.getResultCode(), SUCCESS);
        //username[3] is no longer in the exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //delete username[2] from include
        GroupingsServiceResult deleteMember2 =
                membershipService.deleteGroupMemberByUsername(username[0], GROUPING_INCLUDE, username[2]);
        //deletion was successful
        assertEquals(deleteMember2.getResultCode(), SUCCESS);
        //username[2] is no longer in composite or include
        assertFalse(memberAttributeService.isMember(GROUPING, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //test when not in group
        deleteMember1 = membershipService.deleteGroupMemberByUsername(username[0], GROUPING_EXCLUDE, username[3]);
        deleteMember2 = membershipService.deleteGroupMemberByUsername(username[0], GROUPING_INCLUDE, username[2]);

        //results are successful because the end result is the same
        assertTrue(deleteMember1.getResultCode().startsWith(SUCCESS));
        assertTrue(deleteMember2.getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void addGroupingMemberByUuidTest() {

        List<GroupingsServiceResult> results;
        GroupingsServiceResult sResults;
        String ownerUsername = username[0];
        String uid = "";

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //add member already in the group
        results = membershipService.addGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //username[3] is not in the composite(in the exclude)
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //add member not in the composite but in the basis
        results = membershipService.addGroupingMemberByUuid(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is now in the Composite via basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //removes username[1] from the group
        membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);

        //Checks to see if username[1] is not in the basis or the composite
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds to group
        results = membershipService.addGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to make sure user is in composite and include and nothing else
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds username[3] to the include and removes from the exclude
        membershipService.addGroupMemberByUsername(ownerUsername, GROUPING_INCLUDE, username[3]);

        //username[3] is in the composite, basis and include
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //delete member from grouping
        results = membershipService.addGroupingMemberByUuid(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is in the composite, and basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //checks to see where username 4 is in the group
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //has a non owner try to remove the owner
        try {
            membershipService.deleteGroupingMemberByUsername(username[4], GROUPING, ownerUsername);
        } catch (GroupingsServiceResultException e) {
            sResults = e.getGsr();
            assertTrue(sResults.getResultCode().startsWith(FAILURE));
        }

        //Makes sure that owner is still in the group
        assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, ownerUsername));

    }

    @Test
    public void deleteGroupingMemberByUuidTest() {
        List<GroupingsServiceResult> lResults;
        GroupingsServiceResult results;
        String ownerUsername = username[0];

        //username[1] is in the composite, not basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //delete member from grouping
        lResults = membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to see if username[1] is in the grouping
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //tries to remove someone who is already removed from the group
        lResults = membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to see if username[1] is in the grouping
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //resets
        setUp();

        //adds username[3] to the include and removes from the exclude
        membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_INCLUDE, username[3]);

        //username[3] is in the composite, basis and include
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //delete member from grouping
        lResults = membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[3]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //resets
        setUp();

        //username[4] is in the composite, basis but not the exclude and include
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //delete member from grouping
        lResults = membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[4]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        assertFalse(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //resets
        setUp();

        //checks to see where username 4 is in the group
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //has a non owner try to remove the owner
        try {
            membershipService.deleteGroupingMemberByUsername(username[4], GROUPING, ownerUsername);
        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));
        }

        //Makes sure that owner is still in the group
        assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, ownerUsername));

    }

    @Test
    public void addGroupMembersByUsernameTest() {

        String ownerUsername = username[0];

        List<GroupingsServiceResult> results;
        List<String> usernames = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            usernames.add(username[i]);
        }

        results = membershipService.addGroupMembersByUsername(ownerUsername, GROUPING_INCLUDE, usernames);

        for (GroupingsServiceResult result : results) {
            assertTrue(result.getResultCode().startsWith(SUCCESS));
        }
    }

    @Test
    public void addGroupingMemberTest() {
        List<GroupingsServiceResult> results;
        GroupingsServiceResult sResults;
        String ownerUsername = username[0];
        String uid = "";

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //add member already in the group
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //username[3] is not in the composite(in the exclude)
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //add member not in the composite but in the basis
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is now in the Composite via basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //removes username[1] from the group
        membershipService.deleteGroupingMemberByUuid(ownerUsername, GROUPING, username[1]);

        //Checks to see if username[1] is not in the basis or the composite
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds to group
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to make sure user is in composite and include and nothing else
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds username[3] to the include and removes from the exclude
        membershipService.addGroupMemberByUsername(ownerUsername, GROUPING_INCLUDE, username[3]);

        //username[3] is in the composite, basis and include
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //delete member from grouping
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is in the composite, and basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //checks to see where username 4 is in the group
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //has a non owner try to remove the owner
        try {
            membershipService.deleteGroupingMemberByUsername(username[4], GROUPING, ownerUsername);
        } catch (GroupingsServiceResultException e) {
            sResults = e.getGsr();
            assertTrue(sResults.getResultCode().startsWith(FAILURE));
        }

        //Makes sure that owner is still in the group
        assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, ownerUsername));
    }

    @Test
    public void addGroupMemberTest() {
        List<GroupingsServiceResult> lResults;
        GroupingsServiceResult result;
        String ownerUsername = username[0];

        //Checks to see that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //tries to add a member to a group not allowed, i.e basis
        try {
            lResults = membershipService.addGroupMember(ownerUsername, GROUPING_BASIS, username[2]);
            assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //checks if user is an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMember(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks if user is a superuser
        assertFalse(memberAttributeService.isSuperuser(username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMember(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is in exclude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //adds user who was in exlcude to include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[3]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is NOT in exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //checks to make sure user is in the group but not in exclude or include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //tries adding user to the include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[5]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks the user is in include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add a user already in a group
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //adds user to exlcude when user is in include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure it is actually in the exclude and out of include
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add user who is already in the exclude
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in exlcude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //checks to make user is not an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //adds user to owner list
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        //tries to add user who is already owner
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        membershipService.deleteGroupMemberByUuid(ownerUsername, GROUPING_OWNERS, username[2]);
    }

    @Test
    public void addGroupMemberByUuidTest() {
        List<GroupingsServiceResult> lResults;
        GroupingsServiceResult result;
        String ownerUsername = username[0];

        //Checks to see that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //tries to add a member to a group not allowed, i.e basis
        try {
            lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_BASIS, username[2]);
            assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //checks if user is an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMemberByUuid(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks if user is a superuser
        assertFalse(memberAttributeService.isSuperuser(username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMemberByUuid(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is in exclude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //adds user who was in exlcude to include
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_INCLUDE, username[3]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is NOT in exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //checks to make sure user is in the group but not in exclude or include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //tries adding user to the include
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_INCLUDE, username[5]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks the user is in include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add a user already in a group
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_INCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //adds user to exlcude when user is in include
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure it is actually in the exclude and out of include
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add user who is already in the exclude
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in exlcude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //checks to make user is not an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //adds user to owner list
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        //tries to add user who is already owner
        lResults = membershipService.addGroupMemberByUuid(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        membershipService.deleteGroupMemberByUuid(ownerUsername, GROUPING_OWNERS, username[2]);

    }

    @Test
    public void addGroupMembersByUuidTest() {
        String ownerUsername = username[0];

        List<GroupingsServiceResult> results;
        List<String> usernames = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            usernames.add(username[i]);
        }

        results = membershipService.addGroupMembersByUuid(ownerUsername, GROUPING_INCLUDE, usernames);

        for (GroupingsServiceResult result : results) {
            assertTrue(result.getResultCode().startsWith(SUCCESS));
        }

    }

    @Test
    public void addGroupMembersTest() {
        String ownerUsername = username[0];

        List<GroupingsServiceResult> results;
        List<String> usernames = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            usernames.add(username[i]);
        }

        results = membershipService.addGroupMembers(ownerUsername, GROUPING_INCLUDE, usernames);

        for (GroupingsServiceResult result : results) {
            assertTrue(result.getResultCode().startsWith(SUCCESS));
        }

//        for (int i = 0; i < 6; i++) {
//            membershipService.deleteGroupMemberByUsername(ownerUsername, GROUPING_INCLUDE, username[i]);
//        }
    }

    @Test
    public void deleteGroupMemberByUuidTest() {
        GroupingsServiceResult results;
        String ownerUsername = username[0];

        //Makes sure user isn't owner or superuser
        assertFalse(memberAttributeService.isOwner(GROUPING, username[4]));
        assertFalse(memberAttributeService.isSuperuser(username[4]));

        //has non owner/superuser try to delete
        try {
            results = membershipService.deleteGroupMemberByUuid(username[4], GROUPING_EXCLUDE, username[3]);

        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure username[3] is still a part of the exclude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //checks that owner is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, ownerUsername));

        //tries to delete user from basis group
        try {
            results = membershipService.deleteGroupMemberByUuid(ownerUsername, GROUPING_BASIS, username[3]);
        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure username[3] is still a part of the basis
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //checks to see that username[2] is NOT a part of the exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //tries to delete member from exclude group that isn't in the exclude
        try {
            results = membershipService.deleteGroupMemberByUuid(ownerUsername, GROUPING_EXCLUDE, username[2]);
        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure username[3] is still not a part of the exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //checks to make sure that username[3] is part of the exclude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //deletes user from exclude group
        results = membershipService.deleteGroupMemberByUuid(ownerUsername, GROUPING_EXCLUDE, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see if username[3] is apart of the group and not in the exclude
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //tests if a superuser can remove and that a person from owners can removed
        results = membershipService.deleteGroupMemberByUuid(ADMIN, GROUPING_OWNERS, ownerUsername);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see if ownerUsername is still and owner
        assertFalse(memberAttributeService.isOwner(GROUPING, ownerUsername));

        //adds owner back into owner group
        membershipService.addGroupMemberByUsername(ADMIN, GROUPING_OWNERS, ownerUsername);

        //tests removing from include
        results = membershipService.deleteGroupMemberByUuid(ADMIN, GROUPING_INCLUDE, username[2]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks if username[2] is still in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

    }

    //Add admin and delete admin in one test
    @Test
    public void adminTest() {

        GroupingsServiceResult results;

        //checks to see that username[3] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //makes username[3] an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to make sure that username[3] is an admin
        assertTrue(memberAttributeService.isSuperuser(username[3]));

        //tries to make an already admin an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to make sure that username[3] is an admin
        assertTrue(memberAttributeService.isSuperuser(username[3]));

        //removes username[3] as an admin
        results = membershipService.deleteAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see that username[3] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //tries to remove an person that is not an admin
        results = membershipService.deleteAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see that username[3] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //checks to see that username[4] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[4]));

        //tries to make username[4] an admin but fails due to username[3] not being an admin
        try {

            results = membershipService.addAdmin(username[3], username[4]);

        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));

        }

        //checks to see that username[4] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[4]));

        //tries to delete username[4] as an admin but fails due to username[3] not being an admin
        try {
            results = membershipService.deleteAdmin(username[3], username[4]);

        } catch (GroupingsServiceResultException e) {
            results = e.getGsr();
            assertTrue(results.getResultCode().startsWith(FAILURE));

        }
    }
}
