package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.Dates;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
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
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
@WebAppConfiguration
public class GrouperFactoryServiceMockTest {

    GrouperFactoryServiceImpl gfs = new GrouperFactoryServiceImpl();

    //todo
    @Test
    public void addEmptyGroupTest() {

    }

    @Test
    public void makeWsSubjectLookupTest() {
        String username = "username";
        WsSubjectLookup lookup = gfs.makeWsSubjectLookup(username);

        assertThat(lookup.getSubjectIdentifier(), is(username));
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

    //todo
    @Test
    public void makeWsStemSaveResultsTest() {
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

    //todo
    @Test
    public void makeWsAddMemberResultsTest() {
    }

    //todo
    @Test
    public void makeWsDeleteMemberResultsTest() {
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

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioWithListTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioWithTwoAttributeDefNamesTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsForMembershipTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupTest() {
    }

    //todo
    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupWithAttributeDefNameTest() {
    }

    //todo
    @Test
    public void makeWsHasMemberResultsTest() {
    }

    //todo
    @Test
    public void makeWsAssignAttributesReultsTest() {
    }

    //todo
    @Test
    public void makeWsAssignAttributesResultsForMembershipTest() {
    }

    //todo
    @Test
    public void makeWsAssignAttributesResultsForGroupTest() {
    }

    //todo
    @Test
    public void makeWsAssignAttributesResultsForGroup() {
    }

    //todo
    @Test
    public void makeWsAssignGrouperPrivilegesLiteResult() {
    }

    //todo
    @Test
    public void makeWsGetGrouperPrivilegesLiteResult() {
    }

    //todo
    @Test
    public void makeWsGetMembershipsResults() {
    }

    //todo
    @Test
    public void makeWsGetMembersResults() {
    }


    //todo
    @Test
    public void makeWsGetGroupsResults() {
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
