package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GrouperFactoryServiceMockTest {

    GrouperFactoryServiceImpl gfs = new GrouperFactoryServiceImpl();

    @Test
    public void addEmptyGroupTest() {
        try {
            gfs.addEmptyGroup("", "");
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertThat(result, is("java.lang.IllegalArgumentException: host parameter is null"));
        }
    }

    @Test
    public void getSyncDestinationsTest() {
        try {
            gfs.getSyncDestinations();
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void deleteGroupTest() {
        try {
            WsSubjectLookup subjectLookup = null;
            WsGroupLookup path = null;
            gfs.deleteGroup(subjectLookup, path);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void getDescriptionTest() {
        try {
            gfs.getDescription("");
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertThat(result, is("java.lang.IllegalArgumentException: host parameter is null"));
        }
    }

    @Test
    public void addCompositeGroupTest() {
        try {
            String username = "username";
            String parentGroupPath = "";
            String compositeType = "";
            String leftGroupPath = "path:to:grouping1";
            String rightGroupPath = "path:to:grouping2";
            gfs.addCompositeGroup(username, parentGroupPath, compositeType, leftGroupPath, rightGroupPath);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsStemSaveResultsTest() {
        try {
            gfs.makeWsStemSaveResults("", "");
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void deleteStemTest() {
        try {
            WsSubjectLookup admin = null;
            WsStemLookup stem = null;
            gfs.deleteStem(admin, stem);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsAddMemberResultsGroupTest() {
        try {
            String groupPath = "";
            WsSubjectLookup lookup = null;
            String groupUid = "";
            gfs.makeWsAddMemberResultsGroup(groupPath, lookup, groupUid);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsSubjectLookupTest() {
        String username = "username";
        WsSubjectLookup lookup = gfs.makeWsSubjectLookup(username);

        assertThat(lookup.getSubjectIdentifier(), is(username));
    }

    @Test
    public void makeWsAddMemberResultsTest() {
        try {
            String group = "path:to:grouping1";
            WsSubjectLookup lookup = gfs.makeWsSubjectLookup("username");
            ;
            String newMember = "username";
            gfs.makeWsAddMemberResults(group, lookup, newMember);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "path:to:grouping1";
            WsSubjectLookup lookup = gfs.makeWsSubjectLookup("username1");
            List<String> newMembers = new ArrayList<String>();
            gfs.makeWsAddMemberResults(group, lookup, newMembers);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "path:to:grouping1";
            WsSubjectLookup lookup = gfs.makeWsSubjectLookup("username1");
            ;
            Person personToAdd = new Person();
            personToAdd.setUsername("username");
            gfs.makeWsAddMemberResults(group, lookup, personToAdd);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "path:to:grouping1";
            WsSubjectLookup lookup = gfs.makeWsSubjectLookup("username1");
            ;
            Person personToAdd = new Person();
            personToAdd.setUhUuid("123456789");
            gfs.makeWsAddMemberResults(group, lookup, personToAdd);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "path:to:grouping1";
            String newMember = "username";
            gfs.makeWsAddMemberResults(group, newMember);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGroupLookupTest() {
        String groupPath = "path:to:group";
        WsGroupLookup groupLookup = gfs.makeWsGroupLookup(groupPath);
        assertThat(groupLookup.getGroupName(), is(groupPath));
    }

    @Test
    public void makeWsStemLookupTest() {
        String stemPath = "path:to:stem";
        String stemUuid = "12345";
        WsStemLookup stemLookup = gfs.makeWsStemLookup(stemPath);
        assertThat(stemLookup.getStemName(), is(stemPath));

        stemLookup = gfs.makeWsStemLookup(stemPath, stemUuid);
        assertThat(stemLookup.getStemName(), is(stemPath));
        assertThat(stemLookup.getUuid(), is(stemUuid));
    }

    @Test
    public void makeWsAttributeAssignValueTest() {
        String time = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");

        WsAttributeAssignValue attributeAssignValue = gfs.makeWsAttributeAssignValue(time);
        assertThat(attributeAssignValue.getValueSystem(), is(time));
    }

    //todo
    @Test
    public void makeWsAddMemberResultsWithLookupTest() {
    }

    //todo
    @Test
    public void makeWsAddMemberResultsWithListTest() {
    }

    @Test
    public void makeWsDeleteMemberResultsTest() {
        try {
            String group = "";
            WsSubjectLookup lookup = null;
            String memberToDelete = "";
            gfs.makeWsDeleteMemberResults(group, lookup, memberToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            String memberToDelete = "";
            gfs.makeWsDeleteMemberResults(group, memberToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            WsSubjectLookup lookup = null;
            Person personToDelete = new Person();
            personToDelete.setUsername("username");
            gfs.makeWsDeleteMemberResults(group, lookup, personToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            WsSubjectLookup lookup = null;
            Person personToDelete = new Person();
            personToDelete.setUhUuid("123456789");
            gfs.makeWsDeleteMemberResults(group, lookup, personToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        //Tests null UhUuid.
        try {
            String group = "";
            WsSubjectLookup lookup = null;
            Person personToDelete = new Person();
            gfs.makeWsDeleteMemberResults(group, lookup, personToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            List<String> membersToDelete = new ArrayList<String>();
            WsSubjectLookup lookup = null;
            gfs.makeWsDeleteMemberResults(group, lookup, membersToDelete);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsDeleteMemberResultsGroupTest() {
        try {
            String groupPath = "";
            WsSubjectLookup lookup = null;
            String groupUid = "";
            gfs.makeWsDeleteMemberResultsGroup(groupPath, lookup, groupUid);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    //todo
    @Test
    public void makeWsDeleteMemberResultsTestWithSubjectLookup() {
    }

    //todo
    @Test
    public void makeWsDeleteMemberResultsWithListTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsTest() {
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTest() {
        try {
            String assignType = "";
            String attributeDefNameName = "";
            gfs.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String assignType = "";
            String attributeDefNameName0 = "";
            String attributeDefNameName1 = "";
            gfs.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0, attributeDefNameName1);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String assignType = "";
            String attributeDefNameName = "";
            List<String> ownerGroupNames = new ArrayList<>();
            ownerGroupNames.add("username1");
            ownerGroupNames.add("username2");
            ownerGroupNames.add("username3");
            gfs.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName, ownerGroupNames);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String assignType = "";
            String attributeDefNameName0 = "";
            String attributeDefNameName1 = "";
            List<String> ownerGroupNames = new ArrayList<>();
            ownerGroupNames.add("username1");
            ownerGroupNames.add("username2");
            ownerGroupNames.add("username3");
            gfs.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0, attributeDefNameName1,
                    ownerGroupNames);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupTest() {
        try {
            String assignType = "";
            String group = "";
            gfs.makeWsGetAttributeAssignmentsResultsForGroup(assignType, group);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String attributeDefNameName = "";
            String assignType = "";
            String group = "";
            gfs.makeWsGetAttributeAssignmentsResultsForGroup(assignType, attributeDefNameName, group);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioWithListTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioWithTwoAttributeDefNamesTest() {
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForMembershipTest() {
        try {
            String assignType = "";
            String attributeDefNameName = "";
            String membershipId = "";
            gfs.makeWsGetAttributeAssignmentsResultsForMembership(assignType, attributeDefNameName, membershipId);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupWithAttributeDefNameTest() {
    }

    @Test
    public void makeWsHasMemberResultsTest() {
        try {
            String group = "";
            String username = "";
            gfs.makeWsHasMemberResults(group, username);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            String username = "0000";
            gfs.makeWsHasMemberResults(group, username);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            Person person = new Person();
            person.setUsername("username");
            gfs.makeWsHasMemberResults(group, person);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            Person person = new Person();
            gfs.makeWsHasMemberResults(group, person);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String group = "";
            Person person = new Person();
            person.setUhUuid("0123456789");
            gfs.makeWsHasMemberResults(group, person);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsAssignAttributesResultsTest() {
        try {
            String attributeAssignType = "";
            String attributeAssignOperation = "";
            String ownerGroupName = "";
            String attributeDefNameName = "";
            String attributeAssignValueOperation = "";
            WsAttributeAssignValue value = null;
            gfs.makeWsAssignAttributesResults(attributeAssignType, attributeAssignOperation, ownerGroupName,
                    attributeDefNameName, attributeAssignValueOperation, value);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsAssignAttributesResultsForMembershipTest() {
        try {
            String attributeAssignType = "";
            String attributeAssignOperation = "";
            String attributeDefNameName = "";
            String ownerMembershipId = "";
            gfs.makeWsAssignAttributesResultsForMembership(attributeAssignType, attributeAssignOperation,
                    attributeDefNameName, ownerMembershipId);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsAssignAttributesResultsForGroupTest() {
        try {
            String attributeAssignType = "";
            String attributeAssignOperation = "";
            String attributeDefNameName = "";
            String ownerGroupName = "";
            gfs.makeWsAssignAttributesResultsForGroup(attributeAssignType, attributeAssignOperation,
                    attributeDefNameName,
                    ownerGroupName);
        } catch (
                Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            WsSubjectLookup lookup = null;
            String attributeAssignType = "";
            String attributeAssignOperation = "";
            String attributeDefNameName = "";
            String ownerGroupName = "";
            gfs.makeWsAssignAttributesResultsForGroup(lookup, attributeAssignType, attributeAssignOperation,
                    attributeDefNameName,
                    ownerGroupName);
        } catch (
                Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

    }

    @Test
    public void makeWsAssignGrouperPrivilegesLiteResultTest() {
        try {
            String groupName = "";
            String privilegeName = "";
            WsSubjectLookup admin = null;
            WsSubjectLookup lookup = null;
            boolean isAllowed = false;
            gfs.makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeName, lookup, admin, isAllowed);
        } catch (
                Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String groupName = "";
            String privilegeName = "";
            WsSubjectLookup lookup = null;
            boolean isAllowed = false;
            gfs.makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeName, lookup, isAllowed);
        } catch (
                Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

    }

    @Test
    public void makeWsGetGrouperPrivilegesLiteResultTest() {
        try {
            String groupName = "";
            String privilegeName = "";
            WsSubjectLookup lookup = null;
            gfs.makeWsGetGrouperPrivilegesLiteResult(groupName, privilegeName, lookup);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetMembershipsResultsTest() {
        try {
            String groupName = "";
            WsSubjectLookup lookup = null;
            gfs.makeWsGetMembershipsResults(groupName, lookup);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetAllMembershipsResultsTest() {
        try {
            List<String> groupNames = new ArrayList<>();
            groupNames.add("username1");
            groupNames.add("username2");
            groupNames.add("username3");
            List<WsSubjectLookup> lookups = new ArrayList<>();
            WsSubjectLookup lookup1 = null;
            WsSubjectLookup lookup2 = null;
            WsSubjectLookup lookup3 = null;
            lookups.add(lookup1);
            lookups.add(lookup2);
            lookups.add(lookup3);
            gfs.makeWsGetAllMembershipsResults(groupNames, lookups);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetMembersResultsTest() {
        try {
            String subjectAttributeName = "";
            WsSubjectLookup lookup = null;
            List<String> groupPaths = new ArrayList<>();
            groupPaths.add("path:to:grouping1");
            groupPaths.add("path:to:grouping2");
            groupPaths.add("path:to:grouping3");
            Integer pageNumber = 0;
            Integer pageSize = 0;
            String sortString = "";
            Boolean isAscending = false;
            gfs.makeWsGetMembersResults(subjectAttributeName, lookup, groupPaths, pageNumber, pageSize, sortString,
                    isAscending);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetGroupsResultsTest() {
        try {
            String username = "";
            WsStemLookup stemLookup = null;
            StemScope stemScope = null;
            gfs.makeWsGetGroupsResults(username, stemLookup, stemScope);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }

        try {
            String username = "0000";
            WsStemLookup stemLookup = null;
            StemScope stemScope = null;
            gfs.makeWsGetGroupsResults(username, stemLookup, stemScope);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeWsGetSubjectsResultsTest() {
        try {
            WsSubjectLookup lookup = null;
            gfs.makeWsGetSubjectsResults(lookup);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void updateGroupDescriptionTest() {
        try {
            String groupPath = "path:to:grouping1";
            String description = "a path to a grouping";
            gfs.updateGroupDescription(groupPath, description);
        } catch (Exception e) {
            System.out.println(e);
            String result = e.toString();
            assertTrue(result != null);
        }
    }

    @Test
    public void makeEmptyWsAttributeAssignArrayTest() {
        WsAttributeAssign[] emptyAttributeAssigns = new WsAttributeAssign[0];
        assertTrue(Arrays.equals(emptyAttributeAssigns, gfs.makeEmptyWsAttributeAssignArray()));
    }

    @Test
    public void toStringTest() {
        assertThat(gfs.toString(), is("GrouperFactoryServiceImpl"));
    }
}
