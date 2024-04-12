package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
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

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@Service
public class OotbGroupingPropertiesService {

    public static final Log logger = LogFactory.getLog(OotbGroupingPropertiesService.class);

    @Value("${groupings.api.ootb.groupings_users}")
    private String OOTB_USERS;

    @Autowired
    @Qualifier("HasMembersResultsOOTBBean")
    private HasMembersResults hasMembersResults;

    @Autowired
    @Qualifier("FindGroupsResultsOOTBBean")
    private FindGroupsResults findGroupsResults;

    @Autowired
    @Qualifier("GetSubjectsResultsOOTBBean")
    private SubjectsResults subjectsResults;

    @Autowired
    @Qualifier("GroupSaveResultsOOTBBean")
    private GroupSaveResults groupSaveResults;

    @Autowired
    @Qualifier("AssignAttributesOOTBBean")
    private AssignAttributesResults assignAttributesResults;

    @Autowired
    @Qualifier("GetMembersResultsOOTBBean")
    private GetMembersResults getMembersResults;

    @Autowired
    @Qualifier("AddMemberResultsOOTBBean")
    private AddMembersResults addMembersResults;

    @Autowired
    @Qualifier("RemoveMembersResultsOOTBBean")
    private RemoveMembersResults removeMembersResults;

    @Autowired
    @Qualifier("AttributeAssignmentResultsOOTBBean")
    private GroupAttributeResults groupAttributeResults;

    @Autowired
    @Qualifier("GetGroupsResultsOOTBBean")
    private GetGroupsResults getGroupsResults;

    public HasMembersResults getHasMembersResultsBean() {
        return this.hasMembersResults;
    }

    public FindGroupsResults getFindGroupsResults() {
        return this.findGroupsResults;
    }

    public SubjectsResults getSubjectsResults() {
        return this.subjectsResults;
    }

    public GroupSaveResults getGroupSaveResults() {
        return this.groupSaveResults;
    }

    public AssignAttributesResults getAssignAttributesResults() {
        return this.assignAttributesResults;
    }

    public GetMembersResults getMembersResults() {
        return this.getMembersResults;
    }

    public AddMembersResults getAddMembersResults() {
        return this.addMembersResults;
    }

    public RemoveMembersResults getRemoveMembersResults() {
        return removeMembersResults;
    }

    public GroupAttributeResults getGroupAttributeResults() {
        return groupAttributeResults;
    }

    public GetGroupsResults getGroupsResults() {
        return getGroupsResults;
    }

    /* Updating Member(s) */

    public void ootbRemoveMember(String currentUser, String groupPath, String uhIdentifier) {

        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifier.equals(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembersResults);

        /* Update getGroupResults for updating managePerson  */

        updateGetGroupsResults(uhIdentifier, groupPath, "remove");
    }

    public void ootbRemoveMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifiers.contains(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembersResults);

        updateGetGroupsResults(uhIdentifiers, groupPath, "remove");
    }

    public void ootbAddMember(String currentUser, String groupPath, String uhIdentifier) {

        /* Self opt-in, opt-out */
        if (currentUser.equals(uhIdentifier)) {
            if (groupPath.endsWith(GroupType.INCLUDE.value())) {
                optIn(groupPath);
            } else {
                optOut(groupPath);
            }
            return;
        }

        /* Map< uhIdentifier, Pair< Name, Attributes >> for list of users can be added in ootb groupings page. */
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap = Arrays.stream(wsGetMembersResultsList())
                .filter(wsGetMembersResult -> OOTB_USERS.equals(
                        wsGetMembersResult.getWsGroup().getName()))
                .flatMap(wsGetMembersResult -> Arrays.stream(wsGetMembersResult.getWsSubjects()))
                .filter(wsSubject -> uhIdentifier.equals(wsSubject.getId()))
                .collect(Collectors.toMap(
                        WsSubject::getId,
                        wsSubject -> Pair.of(wsSubject.getName(), wsSubject.getAttributeValues())
                ));

        /* Update member information */
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));
                        WsSubject newMember = new WsSubject();
                        Pair<String, String[]> nameAndAttributes = idToNameAndAttributesMap.get(uhIdentifier);
                        newMember.setId(uhIdentifier);
                        if (nameAndAttributes != null) {
                            newMember.setName(nameAndAttributes.getLeft());
                            newMember.setAttributeValues(nameAndAttributes.getRight());
                        }
                        newMember.setSuccess("T");
                        newMember.setResultCode("SUCCESS");
                        newMember.setSourceId("UH core LDAP");
                        updatedSubjectsList.add(newMember);

                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);

        /* Update getGroupResults for updating managePerson  */

        updateGetGroupsResults(uhIdentifier, groupPath, "add");

    }

    public void ootbAddMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {

        /* Map< uhIdentifier, Pair< Name, Attributes >> for list of users can be added in ootb groupings page. */
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap = getValidOotbUsers(uhIdentifiers);

        /* Update member information */
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));

                        uhIdentifiers.stream()
                                .filter(idToNameAndAttributesMap::containsKey)
                                .forEach(id -> {
                                    WsSubject newMember = new WsSubject();
                                    Pair<String, String[]> nameAndAttributes = idToNameAndAttributesMap.get(id);
                                    newMember.setId(id);
                                    if (nameAndAttributes != null) {
                                        newMember.setName(nameAndAttributes.getLeft());
                                        newMember.setAttributeValues(nameAndAttributes.getRight());
                                    }
                                    newMember.setSuccess("T");
                                    newMember.setResultCode("SUCCESS");
                                    newMember.setSourceId("UH core LDAP");
                                    updatedSubjectsList.add(newMember);
                                });

                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);

        updateGetGroupsResults(uhIdentifiers, groupPath, "add");
    }

    public void addSelf(String currentUser, String groupPath, String uhIdentifier) {
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));
                        WsSubject newMember = new WsSubject();
                        List<String> attributes = List.of(uhIdentifier, uhIdentifier);
                        newMember.setId(uhIdentifier);
                        newMember.setName(uhIdentifier);
                        newMember.setAttributeValues(attributes.toArray(new String[0]));
                        newMember.setSuccess("T");
                        newMember.setResultCode("SUCCESS");
                        newMember.setSourceId("UH core LDAP");
                        updatedSubjectsList.add(newMember);
                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    private void optIn(String groupPath) {

        /* Change attribute can opt-in to can opt-out */
        changeAttribute(groupPath, OptType.IN, OptType.OUT);

        /* Add the group with specific groupPath in GetGroupsResults */
        WsGetGroupsResults wsGetGroupsResults = getGroupsResults.getWsGetGroupsResults();
        String groupName = PathFilter.parentGroupingPath(groupPath);

        WsGroup newGroup = new WsGroup();
        newGroup.setName(groupName + ":include");
        newGroup.setDisplayName(groupName + ":include");
        newGroup.setExtension("include");
        newGroup.setDisplayExtension("include");
        newGroup.setTypeOfGroup("group");

        Arrays.stream(wsGetGroupsResults.getResults()).forEach(result -> {
            List<WsGroup> groupList = new ArrayList<>(Arrays.asList(result.getWsGroups()));
            groupList.add(newGroup); // Add the new group with name matching groupPath
            result.setWsGroups(
                    groupList.toArray(new WsGroup[0]));
        });

    }

    private void optOut(String groupPath) {

        /* Change attribute can opt out to can opt in */
        changeAttribute(groupPath, OptType.OUT, OptType.IN);

        /* Remove a Group with specific groupPaths from GetGroupsResults */
        WsGetGroupsResults wsGetGroupsResults = getGroupsResults.getWsGetGroupsResults();
        String groupName = PathFilter.parentGroupingPath(groupPath);

        Arrays.stream(wsGetGroupsResults.getResults()).forEach(result -> {
            WsGroup[] filteredGroups = Arrays.stream(result.getWsGroups())
                    .filter(group -> !group.getName().equals(groupName + ":include"))
                    .toArray(WsGroup[]::new);
            result.setWsGroups(filteredGroups);
        });
    }

    /* Updating Subject(s) */
    public void updateSubjectsByUhIdentifier(String uhIdentifier) {

        /* Valid Ootb Users List */
        WsSubject[] wsSubjects = getWsSubjectListOfOotbUsers();

        /* Skipping the validating without grouperClient */
        if (!isValidOotbUhIdentifier(uhIdentifier)) {
            WsSubject newSubject = new WsSubject();
            newSubject.setIdentifierLookup(uhIdentifier);
            newSubject.setId(uhIdentifier);
            newSubject.setResultCode("SUCCESS");
            newSubject.setName("OOTB CURRENT USER");
            WsSubject[] updatedSubjects = new WsSubject[] { newSubject };
            subjectsResults.getWsGetSubjectsResults().setWsSubjects(updatedSubjects);
            return;
        }

        /* Updating subjects for displaying the information of the user to be deleted, added
           when user clicked the add, remove button */
        WsSubject[] filteredWsSubjects = Arrays.stream(wsSubjects)
                .filter(wsSubject -> uhIdentifier.equals(wsSubject.getId()) || uhIdentifier.equals(
                        wsSubject.getIdentifierLookup()))
                .toArray(WsSubject[]::new);
        subjectsResults.getWsGetSubjectsResults().setWsSubjects(filteredWsSubjects);
    }

    public void updateSubjectsByUhIdentifiers(List<String> uhIdentifiers) {

        /* Valid Ootb Users List */
        WsSubject[] wsSubjects = getWsSubjectListOfOotbUsers();

        /* Skipping the validating without grouperClient */
        if (!isValidOotbUhIdentifier(uhIdentifiers)) {
            WsSubject[] updatedWsSubjects = uhIdentifiers.stream()
                    .map(uhIdentifier -> {
                        WsSubject wsSubject = new WsSubject();
                        wsSubject.setIdentifierLookup(uhIdentifier);
                        wsSubject.setId(uhIdentifier);
                        wsSubject.setResultCode("SUCCESS");
                        wsSubject.setName("OOTB CURRENT USER");
                        return wsSubject;
                    })
                    .toArray(WsSubject[]::new);
            subjectsResults.getWsGetSubjectsResults().setWsSubjects(updatedWsSubjects);
            return;
        }

        /* Updating subjects for displaying the information of the user to be deleted added,
           when user clicked the add, remove button */
        WsSubject[] filteredWsSubjects = Arrays.stream(wsSubjects)
                .filter(wsSubject -> uhIdentifiers.contains(wsSubject.getId()) || uhIdentifiers.contains(
                        wsSubject.getIdentifierLookup()))
                .toArray(WsSubject[]::new);
        subjectsResults.getWsGetSubjectsResults().setWsSubjects(filteredWsSubjects);
    }

    public FindGroupsResults getFindGroups(String path) {
        WsGroup[] currentGroups = getFindGroupsResults().getWsFindGroupsResults().getGroupResults();

        String groupPath = PathFilter.parentGroupingPath(path);

        List<WsGroup> filteredGroups = Arrays.stream(currentGroups)
                .filter(group -> groupPath.equals(group.getName()))
                .toList();
        if (filteredGroups.isEmpty()) {
            return new FindGroupsResults();
        }
        WsFindGroupsResults newWsFindGroupsResults = new WsFindGroupsResults();
        newWsFindGroupsResults.setGroupResults(filteredGroups.toArray(new WsGroup[0]));

        return new FindGroupsResults(newWsFindGroupsResults);
    }

    public FindGroupsResults getFindGroups(List<String> groupPaths) {
        WsGroup[] currentGroups = getFindGroupsResults().getWsFindGroupsResults().getGroupResults();

        List<WsGroup> filteredGroups = Arrays.stream(currentGroups)
                .filter(group -> groupPaths.contains(group.getName()))
                .toList();

        if (filteredGroups.isEmpty()) {
            return new FindGroupsResults();
        }

        WsFindGroupsResults newWsFindGroupsResults = new WsFindGroupsResults();
        newWsFindGroupsResults.setGroupResults(filteredGroups.toArray(new WsGroup[0]));
        return new FindGroupsResults(newWsFindGroupsResults);
    }


    /* Query Function */

    public AddMemberResult addMember(String currentUser, String groupPath, String uhIdentifier) {
        ootbAddMember(currentUser, groupPath, uhIdentifier);
        return getAddMembersResults().getResults().get(0);
    }

    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        ootbAddMembers(currentUser, groupPath, uhIdentifiers);
        return getAddMembersResults();
    }

    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        ootbRemoveMember(currentUser, groupPath, uhIdentifier);
        return getRemoveMembersResults().getResults().get(0);
    }

    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        ootbRemoveMembers(currentUser, groupPath, uhIdentifiers);
        return getRemoveMembersResults();
    }

    public SubjectsResults getSubject(String uhIdentifier) {
        updateSubjectsByUhIdentifier(uhIdentifier);
        return getSubjectsResults();
    }

    public SubjectsResults getSubjects(List<String> uhIdentifiers) {
        updateSubjectsByUhIdentifiers(uhIdentifiers);
        return getSubjectsResults();
    }

    public GroupAttributeResults getGroupAttributeResultsByAttribute(String attribute) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                getGroupAttributeResults().getWsGetAttributeAssignmentsResults();

        WsAttributeAssign[] currentAssigns = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();

        List<WsAttributeAssign> filteredAssigns = Arrays.stream(currentAssigns)
                .filter(assign -> attribute.equals(assign.getAttributeDefNameName()))
                .toList();

        WsGetAttributeAssignmentsResults modifiedResults = new WsGetAttributeAssignmentsResults();

        modifiedResults.setWsAttributeDefs(wsGetAttributeAssignmentsResults.getWsAttributeDefs());
        modifiedResults.setWsAttributeDefNames(wsGetAttributeAssignmentsResults.getWsAttributeDefNames());
        modifiedResults.setWsGroups(wsGetAttributeAssignmentsResults.getWsGroups());
        modifiedResults.setWsAttributeAssigns(filteredAssigns.toArray(new WsAttributeAssign[0]));

        return new GroupAttributeResults(modifiedResults);
    }

    public GroupAttributeResults getGroupAttributeResultsByAttributeAndGroupPathList(String attribute,
            List<String> groupPaths) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                getGroupAttributeResults().getWsGetAttributeAssignmentsResults();

        WsAttributeAssign[] currentAssigns = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();

        List<WsAttributeAssign> filteredAssigns = Arrays.stream(currentAssigns)
                .filter(assign -> attribute.equals(assign.getAttributeDefNameName()) && groupPaths.contains(
                        assign.getOwnerGroupName()))
                .toList();

        WsGetAttributeAssignmentsResults modifiedResults = new WsGetAttributeAssignmentsResults();

        modifiedResults.setWsAttributeDefs(wsGetAttributeAssignmentsResults.getWsAttributeDefs());
        modifiedResults.setWsAttributeDefNames(wsGetAttributeAssignmentsResults.getWsAttributeDefNames());
        modifiedResults.setWsGroups(wsGetAttributeAssignmentsResults.getWsGroups());
        modifiedResults.setWsAttributeAssigns(filteredAssigns.toArray(new WsAttributeAssign[0]));

        return new GroupAttributeResults(modifiedResults);
    }

    public GetGroupsResults getGroups(String uhIdentifier) {
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap =
                getValidOotbUsers(Collections.singletonList(uhIdentifier));

        if (idToNameAndAttributesMap.isEmpty()) {
            return getGroupsResults();
        }

        GetGroupsResults getGroupsResults1 = getGroupsResults;
        WsGetGroupsResults wsGetGroupsResults = getGroupsResults1.getWsGetGroupsResults();

        WsGetGroupsResult[] filteredResults = Arrays.stream(wsGetGroupsResults.getResults())
                .filter(result -> {
                    WsSubject subject = result.getWsSubject();
                    return uhIdentifier.equals(subject.getIdentifierLookup()) || uhIdentifier.equals(subject.getId());
                })
                .toArray(WsGetGroupsResult[]::new);

        WsGetGroupsResult[] finalResults = filteredResults.length > 0 ?
                new WsGetGroupsResult[] { filteredResults[0] } :
                new WsGetGroupsResult[] {};

        /* To avoid the update of getGroupResult */
        WsGetGroupsResults wsGetGroupsResults1 = new WsGetGroupsResults();
        wsGetGroupsResults1.setResults(finalResults);
        return new GetGroupsResults(wsGetGroupsResults1);
    }






    /* Util Function */

    public WsGetMembersResult[] wsGetMembersResultsList() {
        WsGetMembersResults wsGetMembersResults = getMembersResults().getWsGetMembersResults();
        WsGetMembersResult[] wsGetMembers = wsGetMembersResults.getResults();
        return wsGetMembers;
    }

    public GetMembersResult getMembersByGroupPath(String groupPath) {
        Optional<GetMembersResult> matchingResult = getMembersResults.getMembersResults().stream()
                .filter(result -> groupPath.equals(result.getGroup().getGroupPath()))
                .findFirst();
        return matchingResult.orElse(null);
    }

    public WsSubject[] getWsSubjectListOfOotbUsers() {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        WsSubject[] wsSubjects = getMembersResult.getWsGetMembersResult().getWsSubjects();
        return wsSubjects;
    }

    public Boolean isValidOotbUhIdentifier(String uhIdentifier) {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifier.equals(subject.getId()) || uhIdentifier.equals(
                        subject.getIdentifierLookup()));
    }

    public Boolean isValidOotbUhIdentifier(List<String> uhIdentifiers) {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifiers.contains(subject.getId()) || uhIdentifiers.contains(
                        subject.getIdentifierLookup()));
    }

    private Map<String, Pair<String, String[]>> getValidOotbUsers(List<String> uhIdentifiers) {
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap = Arrays.stream(wsGetMembersResultsList())
                .filter(wsGetMembersResult -> OOTB_USERS.equals(
                        wsGetMembersResult.getWsGroup().getName()))
                .flatMap(wsGetMembersResult -> Arrays.stream(wsGetMembersResult.getWsSubjects()))
                .filter(wsSubject -> uhIdentifiers.contains(wsSubject.getId()))
                .collect(Collectors.toMap(
                        WsSubject::getId,
                        wsSubject -> Pair.of(wsSubject.getName(), wsSubject.getAttributeValues())
                ));
        return idToNameAndAttributesMap;
    }

    public WsGroup getWsGroupFromFindGroupsResults(FindGroupsResults findGroupsResults, String filteredGroupPath) {

        WsGroup wsGroup1 = Arrays.stream(findGroupsResults.getWsFindGroupsResults().getGroupResults())
                .filter(wsGroup -> wsGroup.getName().equals(filteredGroupPath))
                .findFirst()
                .orElse(null);
        return wsGroup1;
    }

    private void changeAttribute(String groupPath, OptType from, OptType to) {

        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                getGroupAttributeResults().getWsGetAttributeAssignmentsResults();
        String groupName = PathFilter.parentGroupingPath(groupPath);

        Arrays.stream(wsGetAttributeAssignmentsResults.getWsAttributeAssigns())
                .filter(wsAttributeAssign -> groupName.equals(wsAttributeAssign.getOwnerGroupName()) &&
                        from.value().equals(wsAttributeAssign.getAttributeDefNameName()))
                .forEach(wsAttributeAssign -> wsAttributeAssign.setAttributeDefNameName(to.value()));
    }

    public void updateGetGroupsResults(String uhIdentifier, String groupPath, String operation) {
        WsGetGroupsResults wsGetGroupsResults1 = getGroupsResults().getWsGetGroupsResults();

        if (operation.equals("add")) {
            FindGroupsResults findGroupsResults1 = getFindGroups(groupPath);

            Arrays.stream(wsGetGroupsResults1.getResults())
                    .filter(result -> uhIdentifier.equals(result.getWsSubject().getIdentifierLookup())
                            || uhIdentifier.equals(result.getWsSubject().getId()))
                    .forEach(result -> {
                        WsGroup newGroup = new WsGroup();
                        newGroup.setName(groupPath);
                        newGroup.setDisplayName(groupPath);
                        newGroup.setDescription(findGroupsResults1.getGroup().getDescription());
                        newGroup.setExtension(findGroupsResults1.getGroup().getExtension());
                        newGroup.setDisplayExtension(findGroupsResults1.getGroup().getExtension());
                        newGroup.setTypeOfGroup("Group");

                        WsGroup[] existingGroups = result.getWsGroups();
                        WsGroup[] updatedGroups = new WsGroup[existingGroups.length + 1];
                        updatedGroups[existingGroups.length] = newGroup;

                        result.setWsGroups(updatedGroups);
                    });
        }

        if (operation.equals("remove")) {
            Arrays.stream(wsGetGroupsResults1.getResults())
                    .filter(result -> uhIdentifier.equals(result.getWsSubject().getIdentifierLookup())
                            || uhIdentifier.equals(result.getWsSubject().getId()))
                    .forEach(result -> {
                        WsGroup[] filteredGroups = Arrays.stream(result.getWsGroups())
                                .filter(group -> !groupPath.equals(group.getName()))
                                .toArray(WsGroup[]::new);
                        result.setWsGroups(filteredGroups);
                    });
        }

    }

    public void updateGetGroupsResults(List<String> uhIdentifier, String groupPath, String operation) {
        WsGetGroupsResults wsGetGroupsResults1 = getGroupsResults().getWsGetGroupsResults();

        if ("add".equals(operation)) {
            FindGroupsResults findGroupsResults1 = getFindGroups(groupPath);

            Arrays.stream(wsGetGroupsResults1.getResults())
                    .filter(result -> uhIdentifier.contains(result.getWsSubject().getIdentifierLookup())
                            || uhIdentifier.contains(result.getWsSubject().getId()))
                    .forEach(result -> {
                        WsGroup newGroup = new WsGroup();
                        newGroup.setName(groupPath);
                        newGroup.setDisplayName(groupPath);
                        newGroup.setDescription(findGroupsResults1.getGroup().getDescription());
                        newGroup.setExtension(findGroupsResults1.getGroup().getExtension());
                        newGroup.setDisplayExtension(findGroupsResults1.getGroup().getExtension());
                        newGroup.setTypeOfGroup("Group");

                        WsGroup[] existingGroups = result.getWsGroups();
                        WsGroup[] updatedGroups = new WsGroup[existingGroups.length + 1];
                        updatedGroups[existingGroups.length] = newGroup;

                        result.setWsGroups(updatedGroups);
                    });
        } else if ("remove".equals(operation)) {
            Arrays.stream(wsGetGroupsResults1.getResults())
                    .filter(result -> uhIdentifier.contains(result.getWsSubject().getIdentifierLookup())
                            || uhIdentifier.contains(result.getWsSubject().getId()))
                    .forEach(result -> {
                        WsGroup[] filteredGroups = Arrays.stream(result.getWsGroups())
                                .filter(group -> !groupPath.equals(group.getName()))
                                .toArray(WsGroup[]::new);
                        result.setWsGroups(filteredGroups);
                    });
        }
    }
}

