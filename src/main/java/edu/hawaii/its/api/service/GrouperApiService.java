package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public interface GrouperApiService {
    List<SyncDestination> syncDestinations();

    String descriptionOf(String groupPath);

    WsGroupSaveResults updateGroupDescription(String groupPath, String description);

    WsAddMemberResults addMember(String group, WsSubjectLookup lookup, String newMember);

    WsAddMemberResults addMember(String group, String newMember);

    WsDeleteMemberResults removeMember(String group, String memberToDelete);

    WsDeleteMemberResults removeMember(String group, WsSubjectLookup lookup, String memberToDelete);

    WsGetAttributeAssignmentsResults groupsOf(String assignType,
            String attributeDefNameName);

    WsGetAttributeAssignmentsResults attributeAssigns(String assignType,
            String attributeDefNameName0,
            String attributeDefNameName1);

    WsGetAttributeAssignmentsResults groupAttributeDefNames(String assignType,
            String group);

    WsGetAttributeAssignmentsResults groupAttributeAssigns(String assignType,
            String attributeDefNameName,
            String group);

    WsHasMemberResults hasMemberResults(String group, String username);

    WsHasMemberResults hasMemberResults(String group, Person person);

    WsAssignAttributesResults assignAttributesResults(String attributeAssignType,
            String attributeAssignOperation,
            String ownerGroupName,
            String attributeDefNameName,
            String attributeAssignValueOperation,
            WsAttributeAssignValue value);

    WsAssignAttributesResults assignAttributesResultsForGroup(String attributeAssignType,
            String attributeAssignOperation,
            String attributeDefNameName,
            String ownerGroupName);

    WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLiteResult(String groupName,
            String privilegeName,
            WsSubjectLookup lookup,
            boolean isAllowed);

    WsGetMembershipsResults membershipsResults(String groupName, WsSubjectLookup lookup);

    WsGetMembersResults membersResults(String subjectAttributeName,
            WsSubjectLookup lookup,
            List<String> groupPaths,
            Integer pageNumber,
            Integer pageSize,
            String sortString,
            Boolean isAscending);

    //Overloaded membersResults, only takes three parameters, no pageNumber, pageSize, sortString and isAscending
    WsGetMembersResults membersResults(String subjectAttributeName,
            WsSubjectLookup lookup,
            List<String> groupPaths);

    WsGetGroupsResults groupsResults(String username, WsStemLookup stemLookup, StemScope stemScope);

    WsGetSubjectsResults subjectsResults(WsSubjectLookup lookup);

    WsFindGroupsResults findGroupsResults(String groupPath);

    WsSubjectLookup subjectLookup(String username);

    WsStemLookup stemLookup(String stemName);

    WsStemLookup stemLookup(String stemName, String stemUuid);

    WsAttributeAssignValue assignAttributeValue(String time);

}
