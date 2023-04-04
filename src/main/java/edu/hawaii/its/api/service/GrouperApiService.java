package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersCommand;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FindGroupsCommand;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GroupAttributeCommand;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveCommand;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersCommand;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersCommand;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsCommand;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.api.GcFindAttributeDefNames;
import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("grouperApiService")
public class GrouperApiService {

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

    @Value("${groupings.api.stem}")
    private String STEM;

    @Autowired
    MemberAttributeService membershipAttributeService;

    @Autowired ExecutorService exec;

    public HasMemberResult memberResult(String groupingPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = exec.execute(new HasMembersCommand()
                .assignGroupPath(groupingPath)
                .addUhIdentifier(uhIdentifier));
        return hasMembersResults.getResult();
    }

    public GroupSaveResults groupSaveResults(String groupingPath, String description) {
        GroupSaveResults groupSaveResults = exec.execute(new GroupSaveCommand()
                .setGroupingPath(groupingPath)
                .setDescription(description));
        return groupSaveResults;
    }

    public FindGroupsResults findGroupsResults(String groupPath) {
        FindGroupsResults findGroupsResults = exec.execute(new FindGroupsCommand()
                .addPath(groupPath));
        return findGroupsResults;
    }

    public FindGroupsResults findGroupsResults(List<String> groupPaths) {
        FindGroupsResults findGroupsResults = exec.execute(new FindGroupsCommand()
                .addPaths(groupPaths));
        return findGroupsResults;
    }

    public Subject getSubject(String uhIdentifier) {
        SubjectsResults subjectsResults = exec.execute(new SubjectsCommand()
                .addSubject(uhIdentifier));
        List<Subject> subjects = subjectsResults.getSubjects();
        if (subjects.size() == 1) {
            return subjects.get(0);
        }
        return new Subject();
    }

    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        SubjectsResults subjectsResults = exec.execute(new SubjectsCommand()
                .addSubjects(uhIdentifiers));
        return subjectsResults;
    }

    public GroupAttributeResults groupAttributeResults(String attribute) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute));
    }

    public GroupAttributeResults groupAttributeResults(List<String> attributes) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes));
    }

    public GroupAttributeResults groupAttributeResults(String attribute, String groupPath) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroup(groupPath));
    }

    public GroupAttributeResults groupAttributeResults(String attribute, List<String> groupPaths) {
        return exec.execute(new GroupAttributeCommand()
                .addAttribute(attribute)
                .addGroups(groupPaths));
    }

    public GroupAttributeResults groupAttributeResults(List<String> attributes, String groupPath) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroup(groupPath));
    }

    public GroupAttributeResults groupAttributeResults(List<String> attributes, List<String> groupPaths) {
        return exec.execute(new GroupAttributeCommand()
                .addAttributes(attributes)
                .addGroups(groupPaths));
    }

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

    public AddMemberResult addMember(String groupPath, String uhIdentifier) {
        return exec.execute(new AddMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    public AddMembersResults addMembers(String groupPath, List<String> uhIdentifiers) {
        return exec.execute(new AddMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
    }

    public RemoveMemberResult removeMember(String groupPath, String uhIdentifier) {
        return exec.execute(new RemoveMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier)).getResults().get(0);
    }

    public AddMembersResults resetGroupMembers(String groupPath) {
        return exec.execute(new AddMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifiers(new ArrayList<>())
                .replaceGroupMembers(true));
    }

    public RemoveMembersResults removeMembers(String groupPath, List<String> uhIdentifiers) {
        return exec.execute(new RemoveMembersCommand()
                .assignGroupPath(groupPath)
                .addUhIdentifiers(uhIdentifiers));
    }

    public WsGetAttributeAssignmentsResults groupsOf(String assignType,
            String attributeDefNameName) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    public WsGetAttributeAssignmentsResults attributeAssigns(String assignType,
            String attributeDefNameName0,
            String attributeDefNameName1) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName0)
                .addAttributeDefNameName(attributeDefNameName1)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    public WsGetAttributeAssignmentsResults groupAttributeDefNames(String assignType,
            String group) {
        return new GcGetAttributeAssignments()
                .addOwnerGroupName(group)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    public WsGetAttributeAssignmentsResults groupAttributeAssigns(String assignType,
            String attributeDefNameName,
            String group) {
        return new GcGetAttributeAssignments()
                .addAttributeDefNameName(attributeDefNameName)
                .addOwnerGroupName(group)
                .assignAttributeAssignType(assignType)
                .execute();
    }

    public WsHasMemberResults hasMemberResults(String group, String uhIdentifier) {
        if (membershipAttributeService.isUhUuid(uhIdentifier)) {
            return new GcHasMember()
                    .assignGroupName(group)
                    .addSubjectId(uhIdentifier)
                    .execute();
        }
        return new GcHasMember()
                .assignGroupName(group)
                .addSubjectIdentifier(uhIdentifier)
                .execute();
    }

    public WsHasMemberResults hasMemberResults(String group, Person person) {
        if (person.getUsername() != null) {
            return hasMemberResults(group, person.getUsername());
        }

        if (person.getUhUuid() == null) {
            throw new IllegalArgumentException("The person is required to have either a username or a uuid");
        }

        return new GcHasMember()
                .assignGroupName(group)
                .addSubjectId(person.getUhUuid())
                .execute();
    }

    public WsAssignAttributesResults assignAttributesResults(
            String attributeAssignType,
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

    public WsGetMembershipsResults membershipsResults(String groupName,
            WsSubjectLookup lookup) {

        return new GcGetMemberships()
                .addGroupName(groupName)
                .addWsSubjectLookup(lookup)
                .execute();
    }

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

    public WsGetGroupsResults groupsResults(String uhIdentifier) {
        WsStemLookup stemLookup = stemLookup(STEM);
        StemScope stemScope = StemScope.ALL_IN_SUBTREE;

        if (membershipAttributeService.isUhUuid(uhIdentifier)) {
            return new GcGetGroups()
                    .addSubjectId(uhIdentifier)
                    .assignWsStemLookup(stemLookup)
                    .assignStemScope(stemScope)
                    .execute();
        }

        return new GcGetGroups()
                .addSubjectIdentifier(uhIdentifier)
                .assignWsStemLookup(stemLookup)
                .assignStemScope(stemScope)
                .execute();
    }

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

    public WsSubjectLookup subjectLookup(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();

        if (membershipAttributeService.isUhUuid(uhIdentifier)) {
            wsSubjectLookup.setSubjectId(uhIdentifier);
        } else {
            wsSubjectLookup.setSubjectIdentifier(uhIdentifier);
        }
        return wsSubjectLookup;
    }

    public WsStemLookup stemLookup(String stemName) {
        return stemLookup(stemName, null);
    }

    public WsStemLookup stemLookup(String stemName, String stemUuid) {
        return new WsStemLookup(stemName, stemUuid);
    }

    public WsAttributeAssignValue assignAttributeValue(String time) {

        WsAttributeAssignValue dateTimeValue = new WsAttributeAssignValue();
        dateTimeValue.setValueSystem(time);

        return dateTimeValue;
    }
}
