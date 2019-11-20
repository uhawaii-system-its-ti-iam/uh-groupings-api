package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public interface GrouperFactoryService {
    WsGroupSaveResults addEmptyGroup(String username, String path);

    WsGroupDeleteResults deleteGroup(WsSubjectLookup username, WsGroupLookup path);

    WsSubjectLookup makeWsSubjectLookup(String username);

    WsGroupLookup makeWsGroupLookup(String group);

    WsStemLookup makeWsStemLookup(String stemName);

    WsStemLookup makeWsStemLookup(String stemName, String stemUuid);

    String getDescription(String groupPath);

    WsGroupSaveResults updateGroupDescription(String groupPath, String description);

    WsStemSaveResults makeWsStemSaveResults(String username, String stemPath);

    WsStemDeleteResults deleteStem(WsSubjectLookup username, WsStemLookup stem);

    WsAttributeAssignValue makeWsAttributeAssignValue(String time);

    WsAddMemberResults makeWsAddMemberResultsGroup(String groupPath, WsSubjectLookup lookup, String groupUid);

    WsAddMemberResults makeWsAddMemberResults(String group, WsSubjectLookup lookup, String newMember);

    WsFindGroupsResults makeWsFindGroupsResults(String groupPath);

    WsAddMemberResults makeWsAddMemberResults(String group, WsSubjectLookup lookup, Person personToAdd);

    WsAddMemberResults makeWsAddMemberResults(String group, WsSubjectLookup lookup, List<String> newMembers);

    WsAddMemberResults makeWsAddMemberResults(String group, String newMember);

    WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete);

    WsDeleteMemberResults makeWsDeleteMemberResults(String group, WsSubjectLookup lookup, String memberToDelete);

    WsDeleteMemberResults makeWsDeleteMemberResults(String group, WsSubjectLookup lookup, Person personToDelete);

    WsDeleteMemberResults makeWsDeleteMemberResults(String group, WsSubjectLookup lookup,
                                                           List<String> membersToDelete);

    WsDeleteMemberResults makeWsDeleteMemberResultsGroup(String groupPath, WsSubjectLookup lookup, String groupUid);

    WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsTrio(String assignType,
                                                                                     String attributeDefNameName);

    WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsTrio(String assignType,
                                                                                     String attributeDefNameName0,
                                                                                     String attributeDefNameName1);

    List<WsGetAttributeAssignmentsResults> makeWsGetAttributeAssignmentsResultsTrio(String assignType,
                                                                                           String attributeDefNameName,
                                                                                           List<String> ownerGroupNames);

    List<WsGetAttributeAssignmentsResults> makeWsGetAttributeAssignmentsResultsTrio(String assignType,
                                                                                           String attributeDefNameName0,
                                                                                           String attributeDefNameName1,
                                                                                           List<String> ownerGroupNames);

    WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForMembership(String assignType,
                                                                                              String attributeDefNameName,
                                                                                              String membershipId);

    WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForGroup(String assignType,
                                                                                         String group);

    WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForGroup(String assignType,
                                                                                         String attributeDefNameName,
                                                                                         String group);

    WsHasMemberResults makeWsHasMemberResults(String group, String username);

    WsHasMemberResults makeWsHasMemberResults(String group, Person person);

    WsAssignAttributesResults makeWsAssignAttributesResults(String attributeAssignType,
                                                                   String attributeAssignOperation,
                                                                   String ownerGroupName,
                                                                   String attributeDefNameName,
                                                                   String attributeAssignValueOperation,
                                                                   WsAttributeAssignValue value);

    WsAssignAttributesResults makeWsAssignAttributesResultsForMembership(String attributeAssignType,
                                                                                String attributeAssignOperation,
                                                                                String attributeDefNameName,
                                                                                String ownerMembershipId);

    WsAssignAttributesResults makeWsAssignAttributesResultsForGroup(String attributeAssignType,
                                                                           String attributeAssignOperation,
                                                                           String attributeDefNameName,
                                                                           String ownerGroupName);

    WsAssignAttributesResults makeWsAssignAttributesResultsForGroup(WsSubjectLookup lookup,
                                                                           String attributeAssignType,
                                                                           String attributeAssignOperation,
                                                                           String attributeDefNameName,
                                                                           String ownerGroupName);


    WsAssignGrouperPrivilegesLiteResult makeWsAssignGrouperPrivilegesLiteResult(String groupName,
                                                                                       String privilegeName,
                                                                                       WsSubjectLookup lookup,
                                                                                       WsSubjectLookup admin,
                                                                                       boolean isAllowed);

    WsAssignGrouperPrivilegesLiteResult makeWsAssignGrouperPrivilegesLiteResult(String groupName,
                                                                                       String privilegeName,
                                                                                       WsSubjectLookup lookup,
                                                                                       boolean isAllowed);

    WsGetGrouperPrivilegesLiteResult makeWsGetGrouperPrivilegesLiteResult(String groupName,
                                                                                 String privilegeName,
                                                                                 WsSubjectLookup lookup);

    WsGetMembershipsResults makeWsGetMembershipsResults(String groupName, WsSubjectLookup lookup);

    WsGetMembersResults makeWsGetMembersResults(String subjectAttributeName,
                                                       WsSubjectLookup lookup,
                                                       List<String> groupPaths,
                                                       Integer pageNumber,
                                                       Integer pageSize,
                                                       String sortString,
                                                       Boolean isAscending);

    WsGetGroupsResults makeWsGetGroupsResults(String username, WsStemLookup stemLookup, StemScope stemScope);

    WsAttributeAssign[] makeEmptyWsAttributeAssignArray();

    WsGroupSaveResults addCompositeGroup(String username, String parentGroupPath, String compositeType,
                                                String leftGroupPath, String rightGroupPath);

    WsGetSubjectsResults makeWsGetSubjectsResults(WsSubjectLookup lookup);

    List<String> getSyncDestinations();
}
