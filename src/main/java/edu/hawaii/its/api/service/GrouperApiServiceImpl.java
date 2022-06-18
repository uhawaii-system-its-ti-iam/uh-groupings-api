package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.util.JsonUtil;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcFindAttributeDefNames;
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

@Service("grouperApiService")
//@Profile(value = { "localhost", "test", "integrationTest", "qa", "prod" })
public class GrouperApiServiceImpl implements GrouperApiService {

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.username}")
    private String USERNAME;

    @Autowired
    HelperService helperService;

    @Override
    public List<SyncDestination> syncDestinations() {
        WsFindAttributeDefNamesResults findAttributeDefNamesResults = new GcFindAttributeDefNames()
                .assignScope(SYNC_DESTINATIONS_LOCATION)
                .assignNameOfAttributeDef(SYNC_DESTINATIONS_CHECKBOXES)
                .execute();

        List<SyncDestination> syncDest = new ArrayList<>();

        for (WsAttributeDefName wsAttributeDefName : findAttributeDefNamesResults.getAttributeDefNameResults()) {
            SyncDestination newSyncDest =
                    new SyncDestination(wsAttributeDefName.getName(), wsAttributeDefName.getDescription());
            newSyncDest = JsonUtil.asObject(newSyncDest.getDescription(), SyncDestination.class);
            newSyncDest.setName(wsAttributeDefName.getName());
            syncDest.add(newSyncDest);
        }
        return syncDest;
    }

    @Override
    public String descriptionOf(String groupPath) {
        WsFindGroupsResults wsFindGroupsResults = findGroupsResults(groupPath);

        return wsFindGroupsResults.getGroupResults()[0].getDescription();

    }

    public WsGroupSaveResults updateGroupDescription(String groupPath, String description) {
        WsGroup updatedGroup = new WsGroup();
        updatedGroup.setDescription(description);

        WsGroupLookup groupLookup = new WsGroupLookup(groupPath,
                findGroupsResults(groupPath).getGroupResults()[0].getUuid());

        WsGroupToSave groupToSave = new WsGroupToSave();
        groupToSave.setWsGroup(updatedGroup);
        groupToSave.setWsGroupLookup(groupLookup);

        return new GcGroupSave().addGroupToSave(groupToSave).execute();
    }

    @Override
    public WsAddMemberResults addMember(String group, WsSubjectLookup lookup, String newMember) {
        if (helperService.isUhUuid(newMember)) {
            return new GcAddMember()
                    .assignActAsSubject(lookup)
                    .addSubjectId(newMember)
                    .assignGroupName(group)
                    .execute();
        }
        return new GcAddMember()
                .assignActAsSubject(lookup)
                .addSubjectIdentifier(newMember)
                .assignGroupName(group)
                .execute();
    }

    @Override
    public WsAddMemberResults addMember(String group, String newMember) {
        if (helperService.isUhUuid(newMember)) {
            return new GcAddMember()
                    .addSubjectId(newMember)
                    .assignGroupName(group)
                    .execute();
        }
        return new GcAddMember()
                .addSubjectIdentifier(newMember)
                .assignGroupName(group)
                .execute();
    }

    @Override
    public WsDeleteMemberResults removeMember(String group, String memberToDelete) {
        if (helperService.isUhUuid(memberToDelete)) {
            return new GcDeleteMember()
                    .addSubjectId(memberToDelete)
                    .assignGroupName(group)
                    .execute();
        }
        return new GcDeleteMember()
                .addSubjectIdentifier(memberToDelete)
                .assignGroupName(group)
                .execute();
    }

    @Override
    public WsDeleteMemberResults removeMember(String group, WsSubjectLookup lookup, String memberToDelete) {
        if (helperService.isUhUuid(memberToDelete)) {
            return new GcDeleteMember()
                    .assignActAsSubject(lookup)
                    .addSubjectId(memberToDelete)
                    .assignGroupName(group)
                    .execute();
        }
        return new GcDeleteMember()
                .assignActAsSubject(lookup)
                .addSubjectIdentifier(memberToDelete)
                .assignGroupName(group)
                .execute();
    }

    @Override
    public WsGetAttributeAssignmentsResults groupsOf(String assignType,
            String attributeDefNameName) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    @Override
    public WsGetAttributeAssignmentsResults attributeAssigns(String assignType,
            String attributeDefNameName0,
            String attributeDefNameName1) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName0)
                .addAttributeDefNameName(attributeDefNameName1)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    @Override
    public WsGetAttributeAssignmentsResults groupAttributeDefNames(String assignType,
            String group) {
        return new GcGetAttributeAssignments()
                .addOwnerGroupName(group)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    @Override
    public WsGetAttributeAssignmentsResults groupAttributeAssigns(String assignType,
            String attributeDefNameName,
            String group) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName)
                .addOwnerGroupName(group)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    @Override
    public WsHasMemberResults hasMemberResults(String group, String username) {
        if (helperService.isUhUuid(username)) {
            return new GcHasMember()
                    .assignGroupName(group)
                    .addSubjectId(username)
                    .execute();
        }
        return new GcHasMember()
                .assignGroupName(group)
                .addSubjectIdentifier(username)
                .execute();
    }

    @Override
    public WsHasMemberResults hasMemberResults(String group, Person person) {
        if (person.getUsername() != null) {
            return hasMemberResults(group, person.getUsername());
        }

        if (person.getUhUuid() == null) {
            throw new NullPointerException("The person is required to have either a username or a uuid");
        }

        return new GcHasMember()
                .assignGroupName(group)
                .addSubjectId(person.getUhUuid())
                .execute();
    }

    @Override
    public WsAssignAttributesResults assignAttributesResults(String attributeAssignType,
            String attributeAssignOperation,
            String ownerGroupName,
            String attributeDefNameName,
            String attributeAssignValueOperation,
            WsAttributeAssignValue value) {

        return new GcAssignAttributes()
                .assignAttributeAssignType(attributeAssignType)
                .assignAttributeAssignOperation(attributeAssignOperation)
                .addOwnerGroupName(ownerGroupName)
                .addAttributeDefNameName(attributeDefNameName)
                .assignAttributeAssignValueOperation(attributeAssignValueOperation)
                .addValue(value)
                .execute();
    }

    @Override
    public WsAssignAttributesResults assignAttributesResultsForGroup(String attributeAssignType,
            String attributeAssignOperation,
            String attributeDefNameName,
            String ownerGroupName) {

        return new GcAssignAttributes()
                .assignAttributeAssignType(attributeAssignType)
                .assignAttributeAssignOperation(attributeAssignOperation)
                .addAttributeDefNameName(attributeDefNameName)
                .addOwnerGroupName(ownerGroupName)
                .execute();
    }

    @Override
    public WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLiteResult(String groupName,
            String privilegeName,
            WsSubjectLookup lookup,
            boolean isAllowed) {

        return new GcAssignGrouperPrivilegesLite()
                .assignGroupName(groupName)
                .assignPrivilegeName(privilegeName)
                .assignSubjectLookup(lookup)
                .assignAllowed(isAllowed)
                .execute();
    }

    @Override
    public WsGetMembershipsResults membershipsResults(String groupName,
            WsSubjectLookup lookup) {

        return new GcGetMemberships()
                .addGroupName(groupName)
                .addWsSubjectLookup(lookup)
                .execute();
    }

    @Override
    public WsGetMembersResults membersResults(String subjectAttributeName,
            WsSubjectLookup lookup,
            List<String> groupPaths,
            Integer pageNumber,
            Integer pageSize,
            String sortString,
            Boolean isAscending) {
        GcGetMembers members = new GcGetMembers();

        if (groupPaths != null && groupPaths.size() > 0) {
            for (String path : groupPaths) {
                members.addGroupName(path);
            }
        }

        members.assignPageNumber(pageNumber);
        members.assignPageSize(pageSize);
        members.assignAscending(isAscending);
        members.assignSortString(sortString);

        return members
                .addSubjectAttributeName(subjectAttributeName)
                .assignActAsSubject(lookup)
                .assignIncludeSubjectDetail(true)
                .execute();
    }

    @Override
    public WsGetMembersResults membersResults(String subjectAttributeName,
            WsSubjectLookup lookup,
            List<String> groupPaths) {
        GcGetMembers members = new GcGetMembers();

        if (groupPaths != null && groupPaths.size() > 0) {
            for (String path : groupPaths) {
                members.addGroupName(path);
            }
        }

        return members
                .addSubjectAttributeName(subjectAttributeName)
                .assignActAsSubject(lookup)
                .assignIncludeSubjectDetail(true)
                .execute();
    }

    @Override
    public WsGetGroupsResults groupsResults(String username, WsStemLookup stemLookup, StemScope stemScope) {

        if (helperService.isUhUuid(username)) {
            return new GcGetGroups()
                    .addSubjectId(username)
                    .assignWsStemLookup(stemLookup)
                    .assignStemScope(stemScope)
                    .execute();
        }

        return new GcGetGroups()
                .addSubjectIdentifier(username)
                .assignWsStemLookup(stemLookup)
                .assignStemScope(stemScope)
                .execute();
    }

    @Override
    public WsGetSubjectsResults subjectsResults(WsSubjectLookup lookup) {
        return new GcGetSubjects()
                .addSubjectAttributeName(USERNAME)
                .addSubjectAttributeName(COMPOSITE_NAME)
                .addSubjectAttributeName(LAST_NAME)
                .addSubjectAttributeName(FIRST_NAME)
                .addSubjectAttributeName(UHUUID)
                .addWsSubjectLookup(lookup)
                .execute();
    }

    @Override
    public WsFindGroupsResults findGroupsResults(String groupPath) {
        return new GcFindGroups()
                .addGroupName(groupPath)
                .execute();
    }

    @Override
    public WsSubjectLookup subjectLookup(String username) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();

        if (helperService.isUhUuid(username)) {
            wsSubjectLookup.setSubjectId(username);
        } else {
            wsSubjectLookup.setSubjectIdentifier(username);
        }
        return wsSubjectLookup;
    }

    @Override
    public WsStemLookup stemLookup(String stemName) {
        return stemLookup(stemName, null);
    }

    @Override
    public WsStemLookup stemLookup(String stemName, String stemUuid) {
        return new WsStemLookup(stemName, stemUuid);
    }

    @Override
    public WsAttributeAssignValue assignAttributeValue(String time) {

        WsAttributeAssignValue dateTimeValue = new WsAttributeAssignValue();
        dateTimeValue.setValueSystem(time);

        return dateTimeValue;
    }
}
