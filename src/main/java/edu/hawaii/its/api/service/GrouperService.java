package edu.hawaii.its.api.service;

import java.util.List;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

public interface GrouperService {
    HasMembersResults hasMemberResults(String groupPath, String uhIdentifier);

    HasMembersResults hasMembersResults(String groupPath, List<String> uhIdentifiers);

    GroupSaveResults groupSaveResults(String groupingPath, String description);

    FindGroupsResults findGroupsResults(String groupPath);

    FindGroupsResults findGroupsResults(String currentUser, String groupPath);

    FindGroupsResults findGroupsResults(List<String> groupPaths);

    SubjectsResults getSubjects(String uhIdentifier);

    SubjectsResults getSubjects(List<String> uhIdentifiers);

    GroupAttributeResults groupAttributeResults(String attribute);

    GroupAttributeResults groupAttributeResults(List<String> attributes);

    GroupAttributeResults groupAttributeResults(String attribute, String groupPath);

    GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths);

    GroupAttributeResults groupAttributeResults(List<String> attributes, String groupPath);

    GroupAttributeResults groupAttributeResults(String currentUser, List<String> attributes, String groupPath);

    GroupAttributeResults groupAttributeResults(List<String> attributes, List<String> groupPaths);

    GroupAttributeResults groupAttributeResult(String groupPath);

    GroupAttributeResults groupAttributeResult(String currentUser, String groupPath);

    GetGroupsResults getGroupsResults(String uhIdentifier);

    GetGroupsResults getGroupsResults(String uhIdentifier, String query);

    GetMembersResult getMembersResult(String currentUser, String groupPath);

    GetMembersResults getMembersResults(List<String> groupPaths);

    FindAttributesResults findAttributesResults(String attributeTypeName, String searchScope);

    FindAttributesResults findAttributesResults(String currentUser, String attributeTypeName, String searchScope);

    AddMemberResult addMember(String currentUser, String groupPath, String uhIdentifier);

    AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers);

    AddMembersResults addGroupPathOwners(String currentUser, String groupPath, List<String> groupPathOwners);

    RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier);

    RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers);

    RemoveMembersResults removeGroupPathOwners(String currentUser, String groupPath, List<String> groupPathOwners);

    AddMembersResults resetGroupMembers(String groupPath);

    AssignAttributesResults assignAttributesResults(String currentUser, String assignType, String assignOperation, String groupPath, String attributeName);

    AssignGrouperPrivilegesResult assignGrouperPrivilegesResult(String currentUser, String groupPath, String privilegeName, String uhIdentifier, boolean isAllowed);

    GetMembersResults getMembersResults(String currentUser, List<String> groupPaths, Integer pageNumber, Integer pageSize, String sortString, Boolean isAscending);
}
