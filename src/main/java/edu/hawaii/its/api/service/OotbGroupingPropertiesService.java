package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.type.OotbActiveProfileResult;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@Service
public class OotbGroupingPropertiesService {

    private static final Log logger = LogFactory.getLog(OotbGroupingPropertiesService.class);

    @Value("${groupings.api.ootb.groupings_users}")
    private String GROUPING_OOTBS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Qualifier("HasMembersResultsOOTBBean")
    private final HasMembersResults hasMembersResults;

    @Qualifier("FindGroupsResultsOOTBBean")
    private final FindGroupsResults findGroupsResults;

    @Qualifier("GetSubjectsResultsOOTBBean")
    private final SubjectsResults subjectsResults;

    @Qualifier("GroupSaveResultsOOTBBean")
    private final GroupSaveResults groupSaveResults;

    @Qualifier("AssignAttributesOOTBBean")
    private final AssignAttributesResults assignAttributesResults;

    @Qualifier("GetMembersResultsOOTBBean")
    private final GetMembersResults getMembersResults;

    @Qualifier("AddMemberResultsOOTBBean")
    private final AddMembersResults addMembersResults;

    @Qualifier("RemoveMembersResultsOOTBBean")
    private final RemoveMembersResults removeMembersResults;

    @Qualifier("AttributeAssignmentResultsOOTBBean")
    private final GroupAttributeResults groupAttributeResults;

    @Qualifier("GetGroupsResultsOOTBBean")
    private final GetGroupsResults getGroupsResults;

    public OotbGroupingPropertiesService(HasMembersResults hasMembersResults,
            FindGroupsResults findGroupsResults,
            SubjectsResults subjectsResults,
            GroupSaveResults groupSaveResults,
            AssignAttributesResults assignAttributesResults,
            GetMembersResults getMembersResults,
            AddMembersResults addMembersResults,
            RemoveMembersResults removeMembersResults,
            GroupAttributeResults groupAttributeResults,
            GetGroupsResults getGroupsResults) {
        this.hasMembersResults = hasMembersResults;
        this.findGroupsResults = findGroupsResults;
        this.subjectsResults = subjectsResults;
        this.groupSaveResults = groupSaveResults;
        this.assignAttributesResults = assignAttributesResults;
        this.getMembersResults = getMembersResults;
        this.addMembersResults = addMembersResults;
        this.removeMembersResults = removeMembersResults;
        this.groupAttributeResults = groupAttributeResults;
        this.getGroupsResults = getGroupsResults;
    }

    public HasMembersResults getHasMembersResults() {
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

    // Update active user profile with the values from ui request
    public OotbActiveProfileResult updateActiveUserProfile(List<String> authorities, String uid, String uhUuid,
            String name, String givenName) {

        WsSubject activeProfileSubject = new WsSubject();
        activeProfileSubject.setId(uhUuid);
        activeProfileSubject.setName(givenName);
        activeProfileSubject.setIdentifierLookup(uid);
        activeProfileSubject.setAttributeValues(
                new String[] { uid, "", "LAST NAME", "FIRST NAME" });
        activeProfileSubject.setSuccess("T");
        activeProfileSubject.setResultCode("SUCCESS");
        activeProfileSubject.setSourceId("UH core LDAP");

        // 1. Update Active User Profile In GROUPING_OOTBS group path
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup().getName().equals(GROUPING_OOTBS)) {
                        List<WsSubject> subjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));

                        // Check for duplicate
                        boolean isDuplicate = subjectsList.stream()
                                .anyMatch(subject -> subject.getIdentifierLookup()
                                        .equals(uid));

                        // If no duplicate, add new subject
                        if (!isDuplicate) {
                            subjectsList.add(activeProfileSubject);
                        }

                        wsGetMembersResult.setWsSubjects(subjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);

        // 2. Update Active User Profile In GetGroupsResults Bean
        WsGetGroupsResult[] results = getGroupsResults.getWsGetGroupsResults().getResults();

        Arrays.stream(results)
                .filter(result -> result.getWsSubject().getAttributeValues()[0].equals(name))
                .findFirst()
                .ifPresent(result -> {
                    result.setWsSubject(activeProfileSubject);
                });

        // 3. Update Active User Profile In HasMembersResults Bean
        HasMembersResults hasMembersResults = getHasMembersResults();
        WsHasMemberResults wsHasMemberResults = hasMembersResults.getWsHasMemberResults();

        WsGroup group = wsHasMemberResults.getWsGroup();
        group.setTypeOfGroup("GROUP");
        group.setDisplayName(GROUPING_ADMINS);
        group.setName(GROUPING_ADMINS);

        WsHasMemberResult[] currentResults = wsHasMemberResults.getResults();
        WsHasMemberResult newWsHasMemberResult = new WsHasMemberResult();

        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode("IS_MEMBER");
        newWsHasMemberResult.setWsSubject(activeProfileSubject);
        newWsHasMemberResult.setResultMetadata(wsResultMeta);

        List<WsHasMemberResult> resultList = Stream.concat(
                Stream.of(newWsHasMemberResult),
                Arrays.stream(currentResults)
        ).toList();

        wsHasMemberResults.setWsGroup(group);
        wsHasMemberResults.setResults(resultList.toArray(new WsHasMemberResult[0]));

        // 4. Add Active User Profile To Subject List Without Duplicate
        WsGetSubjectsResults wsGetSubjectsResults = getSubjectsResults().getWsGetSubjectsResults();
        WsSubject[] currentSubjects = wsGetSubjectsResults.getWsSubjects();

        // Check if there is already a subject with the same UID
        boolean isDuplicate = Arrays.stream(currentSubjects)
                .anyMatch(subject -> subject.getIdentifierLookup().equals(uid));

        // If no duplicate, add new subject
        if (!isDuplicate) {
            WsSubject[] updatedSubjects = Arrays.copyOf(currentSubjects, currentSubjects.length + 1);
            updatedSubjects[currentSubjects.length] = activeProfileSubject;
            wsGetSubjectsResults.setWsSubjects(updatedSubjects);
        }

        // 5. Add Active User Profile To Members List Without Duplicate
        // Add default user to default users' owned group
        GetGroupsResults getGroupsResults = getGroups(uid);
        getGroupsResults.getGroups().stream()
                .map(Group::getGroupPath)
                .forEach(groupPath -> addMember("Default User", groupPath, uid));

        // Add default user to admin member list if default user has admin role
        if (authorities.contains("ROLE_ADMIN")) {
            addMember("Default User", GROUPING_ADMINS, uid);
        }


        OotbActiveProfile profile = new OotbActiveProfile.Builder()
            .uid(uid)
            .uhUuid(uhUuid)
            .name(name)
            .givenName(givenName)
            .authorities(authorities)
            .build();

        return new OotbActiveProfileResult(profile);
    }

    /* Updating Member(s) */

    public void ootbRemoveMember(String currentUser, String groupPath, String uhIdentifier) {

        /* Update member information */
        updateMemberRemoved(groupPath, Collections.singletonList(uhIdentifier));

        /* Update getGroupResults for updating managePerson  */
        updateGetGroupsResults(uhIdentifier, groupPath, "remove");
    }

    public void ootbRemoveMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {

        /* Update member information */
        updateMemberRemoved(groupPath, uhIdentifiers);

        /* Update getGroupResults for updating managePerson  */
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

        /* Map< uhIdentifier, Subject> for list of users can be added in ootb groupings page. */
        Map<String, Subject> getValidOotbUsers = getValidOotbUsers(Collections.singletonList(uhIdentifier));

        /* Update member information */
        updateMemberAdded(groupPath, Collections.singletonList(uhIdentifier), getValidOotbUsers);

        /* Update getGroupResults for updating managePerson  */
        if (!currentUser.equals("Default User")) {
            updateGetGroupsResults(uhIdentifier, groupPath, "add");
        }

    }

    public void ootbAddMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {

        /* Map< uhIdentifier, Suibject> for list of users can be added in ootb groupings page. */
        Map<String, Subject> getValidOotbUsers = getValidOotbUsers(uhIdentifiers);

        /* Update member information */
        updateMemberAdded(groupPath, uhIdentifiers, getValidOotbUsers);

        /* Update getGroupResults for updating managePerson  */
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

    public void ootbResetGroup(String groupPath) {
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] resetSubjects = new WsSubject[] {};
                        filteredResult.setWsSubjects(resetSubjects);

                        /* Updating managePersonResults with uhIdentifiers to be reset*/
                        String[] uhIdentifiers = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .map(WsSubject::getId)
                                .toArray(String[]::new);
                        updateGetGroupsResults(List.of(uhIdentifiers), groupPath, "remove");

                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembersResults);
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

    public AddMembersResults resetGroup(String groupPath) {
        ootbResetGroup(groupPath);
        return getAddMembersResults();
    }

    public GetMembersResults getOwnedGroupings(List<String> groupPaths) {
        WsGetMembersResult[] originalResults = getMembersResults().getWsGetMembersResults().getResults();
        WsGetMembersResult[] filteredResults = Arrays.stream(originalResults)
                .filter(result -> result.getWsGroup() != null && groupPaths.contains(result.getWsGroup().getName()))
                .toArray(WsGetMembersResult[]::new);
        WsGetMembersResults newWsGetMembersResults = new WsGetMembersResults();
        newWsGetMembersResults.setResults(filteredResults);
        return new GetMembersResults(newWsGetMembersResults);
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
        Map<String, Subject> getValidOotbUsers = getValidOotbUsers(Collections.singletonList(uhIdentifier));

        if (getValidOotbUsers.isEmpty()) {
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
        GetMembersResult getMembersResult = getMembersByGroupPath(GROUPING_OOTBS);
        WsSubject[] wsSubjects = getMembersResult.getWsGetMembersResult().getWsSubjects();
        return wsSubjects;
    }

    public Boolean isValidOotbUhIdentifier(String uhIdentifier) {
        GetMembersResult getMembersResult = getMembersByGroupPath(GROUPING_OOTBS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifier.equals(subject.getId()) || uhIdentifier.equals(
                        subject.getIdentifierLookup()));
    }

    public Boolean isValidOotbUhIdentifier(List<String> uhIdentifiers) {
        GetMembersResult getMembersResult = getMembersByGroupPath(GROUPING_OOTBS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifiers.contains(subject.getId()) || uhIdentifiers.contains(
                        subject.getIdentifierLookup()));
    }

    private Map<String, Subject> getValidOotbUsers(List<String> uhIdentifiers) {
        Map<String, Subject> validOotbUsersMap = new HashMap<>();

        Arrays.stream(wsGetMembersResultsList())
                .filter(wsGetMembersResult -> GROUPING_OOTBS.equals(wsGetMembersResult.getWsGroup().getName()))
                .flatMap(wsGetMembersResult -> Arrays.stream(wsGetMembersResult.getWsSubjects()))
                .filter(wsSubject -> uhIdentifiers.contains(wsSubject.getIdentifierLookup()) || uhIdentifiers.contains(
                        wsSubject.getId()))
                .forEach(wsSubject -> {
                    Subject subject = new Subject(wsSubject);
                    validOotbUsersMap.put(wsSubject.getIdentifierLookup(), subject);
                    validOotbUsersMap.put(wsSubject.getId(), subject);
                });

        return validOotbUsersMap;
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
                                .filter(group -> group != null && !groupPath.equals(group.getName()))
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
                                .filter(group -> group != null && !groupPath.equals(group.getName()))
                                .toArray(WsGroup[]::new);
                        result.setWsGroups(filteredGroups);
                    });
        }
    }

    private void updateMemberAdded(String groupPath, List<String> uhIdentifiers,
            Map<String, Subject> getValidOotbUsers) {
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));
                        Set<String> existingIdentifiers = updatedSubjectsList.stream()
                                .map(WsSubject::getIdentifierLookup)
                                .collect(Collectors.toSet());

                        uhIdentifiers.stream()
                                .filter(getValidOotbUsers::containsKey)
                                .map(getValidOotbUsers::get)
                                .map(ootbUser -> {
                                    WsSubject newMember = new WsSubject();
                                    newMember.setId(ootbUser.getUhUuid());
                                    newMember.setName(ootbUser.getName());
                                    newMember.setIdentifierLookup(ootbUser.getUid());
                                    newMember.setAttributeValues(
                                            new String[] { ootbUser.getUid(), "", ootbUser.getLastName(),
                                                    ootbUser.getFirstName() });
                                    newMember.setSuccess("T");
                                    newMember.setResultCode("SUCCESS");
                                    newMember.setSourceId("UH core LDAP");
                                    return newMember;
                                })
                                .filter(newMember -> !existingIdentifiers.contains(newMember.getIdentifierLookup()))
                                .forEach(updatedSubjectsList::add);

                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    private void updateMemberRemoved(String groupPath, List<String> uhIdentifiers) {
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
    }

    public GroupSaveResults updateDescription(String groupPath, String description) {
        WsFindGroupsResults wsFindGroupsResults = getFindGroupsResults().getWsFindGroupsResults();
        WsGroup[] groups = wsFindGroupsResults.getGroupResults();

        if (groups.length == 0) {
            return new GroupSaveResults();
        }

        Arrays.stream(groups)
                .filter(group -> group.getName() != null && group.getName().equals(groupPath))
                .findFirst()
                .ifPresent(group -> group.setDescription(description));

        return getGroupSaveResults();
    }
}

