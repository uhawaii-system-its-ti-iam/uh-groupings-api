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

import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.type.OotbActiveProfileResult;
import edu.hawaii.its.api.type.OotbGrouping;
import edu.hawaii.its.api.type.OotbMember;
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
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
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
    @Value("${groupings.api.ootb.groupings_users}")
    private String GROUPING_OOTBS;
    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;
    private String currentUser;

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

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    // Update active user profile with the values from ui request

    public OotbActiveProfileResult updateActiveUserProfile(OotbActiveProfile ootbActiveProfile) {
        List<OotbGrouping> groupings = ootbActiveProfile.getGroupings();
        WsSubject activeProfileSubject = getWsSubject(ootbActiveProfile);
        WsGroup[] wsGroups = getWsGroups(groupings);
        List<WsGroup> newGroupsList = getGroups(groupings);

        setCurrentUser(activeProfileSubject.getIdentifierLookup());

        // 1. Update Active User Profile In FindGroupsResults Bean
        WsFindGroupsResults wsFindGroupsResults = getFindGroupsResults().getWsFindGroupsResults();

        // Group path without extension for FindGroupsResults here
        List<WsGroup> existingGroups = Arrays.asList(wsFindGroupsResults.getGroupResults());
        List<String> existingGroupNames = existingGroups.stream().map(WsGroup::getName).toList();

        List<WsGroup> combinedGroups = new ArrayList<>(existingGroups);
        newGroupsList.stream()
                .filter(group -> !existingGroupNames.contains(group.getName()))
                .forEach(combinedGroups::add);

        // Update the existing WsFindGroupsResults with the combined group list
        wsFindGroupsResults.setGroupResults(combinedGroups.toArray(new WsGroup[0]));
        Arrays.stream(wsGroups).toList().forEach(c -> updateDescription(c.getName(), c.getDescription()));

        // 2. Update Active User Profile In HasMembersResults Bean
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

        // 3. Add Active User Profiles To Subject List Without Duplicate
        WsGetSubjectsResults wsGetSubjectsResults = getSubjectsResults().getWsGetSubjectsResults();
        WsSubject[] currentSubjects = wsGetSubjectsResults.getWsSubjects();

        // Check if there is already a subject with the same UID
        boolean isDuplicate = Arrays.stream(currentSubjects)
                .anyMatch(subject -> subject.getIdentifierLookup().equals(activeProfileSubject.getIdentifierLookup()));

        // If no duplicate, add new subject
        if (!isDuplicate) {
            WsSubject[] updatedSubjects = Arrays.copyOf(currentSubjects, currentSubjects.length + 1);
            updatedSubjects[currentSubjects.length] = activeProfileSubject;
            wsGetSubjectsResults.setWsSubjects(updatedSubjects);
        }

        // 4. Add Active User Profile To Members List Without Duplicate
        // Add group for 4 different extension (basis, include, exclude, owners)
        newGroupsList.forEach(newGroup -> {
            addNewGroupMemberResult(newGroup, "basis");
            addNewGroupMemberResult(newGroup, "include");
            addNewGroupMemberResult(newGroup, "exclude");
            addNewGroupMemberResult(newGroup, "owners");
        });

        // 5. Add all members in multiple groupPaths
        groupings.forEach(
                grouping -> {
                    addMember(grouping.getName(), Collections.singletonList(activeProfileSubject));
                    addMember(grouping.getName(), convertOotbMemberListToWsSubject(grouping.getMembers()));

                    addMember(GROUPING_OOTBS, Collections.singletonList(activeProfileSubject));
                    addMember(GROUPING_OOTBS, convertOotbMemberListToWsSubject(grouping.getMembers()));
                }
        );
        // Add default user to admin member list if default user has admin role
        if (ootbActiveProfile.getAuthorities().contains("ROLE_ADMIN")) {
            addMember(GROUPING_ADMINS, Collections.singletonList(activeProfileSubject));
        }

        return new OotbActiveProfileResult(ootbActiveProfile);
    }

    /* Updating Member(s) */

    public void ootbRemoveMembers(String groupPath, List<WsSubject> wsSubjectList) {

        /* Update member information */
        updateMemberRemoved(groupPath, wsSubjectList);

        /* Update getGroupResults for updating managePerson  */
        wsSubjectList.forEach(wsSubject -> {
            updateGetGroupsResults(wsSubject, groupPath, "remove");
        });
    }

    private void ootbAddMembers(String groupPath, List<WsSubject> wsSubjectList) {
        String groupName = PathFilter.parentGroupingPath(groupPath);
        String extension = PathFilter.extractExtension(groupPath);

        // update getMembersResults
        updateMemberAdded(groupPath, wsSubjectList);
        // update getGroupsResults
        wsSubjectList.forEach(wsSubject -> {
            if (extension.equals("include")) {
                updateGetGroupsResults(wsSubject, groupPath, "add");
                updateGetGroupsResults(wsSubject, groupName + ":exclude",
                        "remove");
                updateMemberRemoved(groupName + ":exclude", Collections.singletonList(wsSubject));
                return;
            }

            if (extension.equals("exclude")) {
                updateGetGroupsResults(wsSubject, groupPath, "add");
                updateGetGroupsResults(wsSubject, groupName + ":include",
                        "remove");
                updateMemberRemoved(groupName + ":include", Collections.singletonList(wsSubject));
                return;
            }

            updateGetGroupsResults(wsSubject, groupPath, "add");
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
        WsSubject wsSubject = getWsOotbSubject(uhIdentifier);
        ootbAddMembers(groupPath, Collections.singletonList(wsSubject));
        return getAddMembersResults().getResults().get(0);
    }

    public AddMembersResults addMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        List<WsSubject> wsSubjectList = getWsOotbSubjects(uhIdentifiers);
        ootbAddMembers(groupPath, wsSubjectList);
        return getAddMembersResults();
    }

    public AddMemberResult addMember(String groupPath, List<WsSubject> subjects) {
        ootbAddMembers(groupPath, subjects);
        return getAddMembersResults().getResults().get(0);
    }

    public RemoveMemberResult removeMember(String currentUser, String groupPath, String uhIdentifier) {
        WsSubject wsSubject = getWsOotbSubject(uhIdentifier);
        ootbRemoveMembers(groupPath, Collections.singletonList(wsSubject));
        return getRemoveMembersResults().getResults().get(0);
    }

    public RemoveMembersResults removeMembers(String currentUser, String groupPath, List<String> uhIdentifiers) {
        List<WsSubject> wsSubject = getWsOotbSubjects(uhIdentifiers);
        ootbRemoveMembers(groupPath, wsSubject);
        return getRemoveMembersResults();
    }

    public RemoveMemberResult removeMember(String groupPath, List<WsSubject> subjects) {
        ootbRemoveMembers(groupPath, subjects);
        return getRemoveMembersResults().getResults().get(0);
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
                .filter(assign -> attribute.equals(assign.getAttributeDefNameName()) && (
                        assign.getOwnerMemberSubjectId() == null || assign.getOwnerMemberSubjectId()
                                .equals(getCurrentUser())))
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
                .filter(assign -> attribute.equals(assign.getAttributeDefNameName()) &&
                        groupPaths.contains(assign.getOwnerGroupName()) &&
                        (currentUser == null || currentUser.equals(assign.getOwnerMemberSubjectId())))
                .toList();

        WsGetAttributeAssignmentsResults modifiedResults = new WsGetAttributeAssignmentsResults();
        modifiedResults.setWsAttributeDefs(wsGetAttributeAssignmentsResults.getWsAttributeDefs());
        modifiedResults.setWsAttributeDefNames(wsGetAttributeAssignmentsResults.getWsAttributeDefNames());
        modifiedResults.setWsGroups(wsGetAttributeAssignmentsResults.getWsGroups());
        modifiedResults.setWsAttributeAssigns(filteredAssigns.toArray(new WsAttributeAssign[0]));

        return new GroupAttributeResults(modifiedResults);
    }

    public GroupAttributeResults getGroupAttributeResults(String currentUser,
            String groupPath) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                getGroupAttributeResults().getWsGetAttributeAssignmentsResults();

        WsAttributeAssign[] currentAssigns = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();
        WsAttributeDefName[] wsAttributeDefNames = wsGetAttributeAssignmentsResults.getWsAttributeDefNames();

        List<WsAttributeAssign> filteredAssigns = Arrays.stream(currentAssigns)
                .filter(assign ->
                        groupPath.equals(assign.getOwnerGroupName()) &&
                                (currentUser == null || currentUser.equals(assign.getOwnerMemberSubjectId())))
                .toList();

        List<WsAttributeDefName> filteredDefName = Arrays.stream(wsAttributeDefNames)
                .filter(defName -> defName.getIdIndex().equals(currentUser) && defName.getUuid().equals(groupPath))
                .toList();

        WsGetAttributeAssignmentsResults modifiedResults = new WsGetAttributeAssignmentsResults();
        modifiedResults.setWsAttributeDefs(wsGetAttributeAssignmentsResults.getWsAttributeDefs());
        modifiedResults.setWsAttributeDefNames(wsGetAttributeAssignmentsResults.getWsAttributeDefNames());
        modifiedResults.setWsGroups(wsGetAttributeAssignmentsResults.getWsGroups());
        modifiedResults.setWsAttributeAssigns(filteredAssigns.toArray(new WsAttributeAssign[0]));
        modifiedResults.setWsAttributeDefNames(filteredDefName.toArray(new WsAttributeDefName[0]));

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

    private void updateMemberAdded(String groupPath, List<WsSubject> wsSubjects) {
        if (wsSubjects.isEmpty()) {
            return;
        }

        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> existingSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));
                        List<WsSubject> newSubjects = wsSubjects.stream()
                                .filter(newSubject -> existingSubjectsList.stream()
                                        .noneMatch(existingSubject -> existingSubject.getIdentifierLookup()
                                                .equals(newSubject.getIdentifierLookup())))
                                .toList();

                        existingSubjectsList.addAll(newSubjects);
                        wsGetMembersResult.setWsSubjects(existingSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults().getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    public AssignAttributesResults manageAttributeAssignment(String groupPath, String attributeName,
            String assignOperation) {
        String groupName = PathFilter.parentGroupingPath(groupPath);

        Set<String> membersUid = Stream.concat(
                getMembersOfMembership(groupName + ":include").stream(),
                getMembersOfMembership(groupName + ":basis").stream()
        ).collect(Collectors.toSet());

        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                getGroupAttributeResults().getWsGetAttributeAssignmentsResults();
        List<WsAttributeAssign> attributeAssigns =
                new ArrayList<>(Arrays.asList(wsGetAttributeAssignmentsResults.getWsAttributeAssigns()));
        List<WsAttributeDefName> attributeDefNames =
                new ArrayList<>(Arrays.asList(wsGetAttributeAssignmentsResults.getWsAttributeDefNames()));

        if (assignOperation.equals("assign_attr")) {
            membersUid.forEach(uid -> {
                WsAttributeAssign newAssign = new WsAttributeAssign();
                newAssign.setOwnerGroupName(groupName);
                newAssign.setAttributeDefNameName(attributeName);
                newAssign.setOwnerMemberSubjectId(uid);

                boolean isDuplicateAssign = attributeAssigns.stream().anyMatch(wsAttributeAssign ->
                        wsAttributeAssign.getOwnerGroupName().equals(newAssign.getOwnerGroupName()) &&
                                wsAttributeAssign.getAttributeDefNameName().equals(newAssign.getAttributeDefNameName())
                                &&
                                wsAttributeAssign.getOwnerMemberSubjectId()
                                        .equals(newAssign.getOwnerMemberSubjectId()));

                if (!isDuplicateAssign) {
                    attributeAssigns.add(newAssign);
                }
            });

            membersUid.forEach(uid -> {
                boolean isDuplicateDefName = attributeDefNames.stream().anyMatch(defName ->
                        defName.getName().equals(attributeName) &&
                                defName.getUuid().equals(groupName) &&
                                defName.getIdIndex().equals(uid));

                if (!isDuplicateDefName) {
                    WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
                    wsAttributeDefName.setIdIndex(uid);
                    wsAttributeDefName.setUuid(groupName);
                    wsAttributeDefName.setName(attributeName);
                    wsAttributeDefName.setDisplayName(attributeName);
                    wsAttributeDefName.setAttributeDefName(attributeName);
                    attributeDefNames.add(wsAttributeDefName);
                }
            });
        }

        if (assignOperation.equals("remove_attr")) {
            membersUid.forEach(uid -> {
                attributeAssigns.removeIf(wsAttributeAssign ->
                        groupName.equals(wsAttributeAssign.getOwnerGroupName()) &&
                                attributeName.equals(wsAttributeAssign.getAttributeDefNameName()) &&
                                (wsAttributeAssign.getOwnerMemberSubjectId() == null ||
                                        wsAttributeAssign.getOwnerMemberSubjectId().equals(uid)));

                attributeDefNames.removeIf(defName ->
                        defName.getUuid().equals(groupName) &&
                                defName.getIdIndex().equals(uid) &&
                                defName.getName().equals(attributeName));
            });
        }

        wsGetAttributeAssignmentsResults.setWsAttributeDefNames(attributeDefNames.toArray(new WsAttributeDefName[0]));
        wsGetAttributeAssignmentsResults.setWsAttributeAssigns(attributeAssigns.toArray(new WsAttributeAssign[0]));
        return getAssignAttributesResults();
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

    public WsSubject getWsOotbSubject(String uhIdentifier) {
        GetMembersResult getMembersResult = getMembersByGroupPath(GROUPING_OOTBS);
        WsSubject[] wsSubjects = getMembersResult.getWsGetMembersResult().getWsSubjects();
        Optional<WsSubject> subject = Arrays.stream(wsSubjects)
                .filter(wsSubject -> wsSubject.getIdentifierLookup().equals(uhIdentifier))
                .findFirst();
        return subject.orElse(null);
    }

    public List<WsSubject> getWsOotbSubjects(List<String> uhIdentifiers) {
        GetMembersResult getMembersResult = getMembersByGroupPath(GROUPING_OOTBS);
        WsSubject[] wsSubjects = getMembersResult.getWsGetMembersResult().getWsSubjects();
        Optional<List<WsSubject>> subject = Optional.of(
                Arrays.stream(wsSubjects).filter(wsSubject -> uhIdentifiers.contains(wsSubject.getId()))
                        .toList());
        return subject.get();
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

        if (findGroupsResults == null || findGroupsResults.getWsFindGroupsResults() == null ||
                findGroupsResults.getWsFindGroupsResults().getGroupResults() == null) {
            return null;
        }

        WsGroup[] groupResults = findGroupsResults.getWsFindGroupsResults().getGroupResults();
        return Arrays.stream(groupResults)
                .filter(wsGroup -> wsGroup.getName().equals(filteredGroupPath))
                .findFirst()
                .orElse(null);
    }

    public void updateGetGroupsResults(WsSubject wsSubject, String groupPath, String operation) {
        WsGetGroupsResults wsGetGroupsResults1 = getGroupsResults().getWsGetGroupsResults();
        WsGetGroupsResult[] wsGetGroupsResults = wsGetGroupsResults1.getResults();
        List<WsGetGroupsResult> wsGetGroupsResultList = new ArrayList<>(Arrays.asList(wsGetGroupsResults));

        String parentGroupingPath = PathFilter.parentGroupingPath(groupPath);
        String extension = PathFilter.extractExtension(groupPath);
        // Check if there is already a result with the same subject
        Optional<WsGetGroupsResult> existingResult = wsGetGroupsResultList.stream()
                .filter(result -> wsSubject.getId().equals(result.getWsSubject().getIdentifierLookup())
                        || wsSubject.getId().equals(result.getWsSubject().getId()))
                .findFirst();

        if (existingResult.isPresent()) {
            WsGetGroupsResult existedGroup = existingResult.get();
            if (operation.equals("add")) {
                WsGroup newGroup = createWsGroup(parentGroupingPath, extension);
                WsGroup[] updatedGroups = mergeGroups(existedGroup, newGroup);
                existedGroup.setWsGroups(updatedGroups);
                return;
            }
            if (operation.equals("remove")) {
                WsGroup[] filteredGroups = Arrays.stream(existedGroup.getWsGroups())
                        .filter(group -> group != null && !groupPath.equals(group.getName()))
                        .toArray(WsGroup[]::new);
                existedGroup.setWsGroups(filteredGroups);
                return;
            }
        }

        if (operation.equals("add")) {
            WsGetGroupsResult newResult = new WsGetGroupsResult();
            newResult.setWsSubject(wsSubject);
            WsGroup newGroup = createWsGroup(parentGroupingPath, extension);
            newResult.setWsGroups(new WsGroup[] { newGroup });
            wsGetGroupsResultList.add(newResult);
        }

        if (operation.equals("remove")) {
            return;
        }

        // Update getGroupsResults
        WsGetGroupsResult[] updatedResults = wsGetGroupsResultList.toArray(new WsGetGroupsResult[0]);
        getGroupsResults().getWsGetGroupsResults().setResults(updatedResults);
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

                        WsGroup[] updatedGroups = mergeGroups(result, newGroup);

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

    private WsSubject getWsSubject(OotbActiveProfile ootbActiveProfile) {
        WsSubject activeProfileSubject = new WsSubject();
        activeProfileSubject.setId(ootbActiveProfile.getUhUuid());
        activeProfileSubject.setName(ootbActiveProfile.getAttributes().get("givenName"));
        activeProfileSubject.setIdentifierLookup(ootbActiveProfile.getUid());
        activeProfileSubject.setAttributeValues(
                new String[] { ootbActiveProfile.getUid(), "", "LAST NAME", "FIRST NAME" });
        activeProfileSubject.setSuccess("T");
        activeProfileSubject.setResultCode("SUCCESS");
        activeProfileSubject.setSourceId("UH core LDAP");
        return activeProfileSubject;
    }

    private void updateMemberRemoved(String groupPath, List<WsSubject> wsSubjectsToRemove) {
        if (wsSubjectsToRemove.isEmpty())
            return;
        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> currentSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));

                        currentSubjectsList.removeIf(subjectInList ->
                                wsSubjectsToRemove.stream()
                                        .anyMatch(subjectToRemove -> subjectToRemove.getId()
                                                .equals(subjectInList.getId())));

                        wsGetMembersResult.setWsSubjects(currentSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    private WsGroup[] getWsGroups(List<OotbGrouping> groupings) {
        return groupings.stream()
                .map(g -> {
                    WsGroup group = new WsGroup();
                    group.setTypeOfGroup("group");
                    group.setName(g.getName());
                    group.setDisplayName(g.getDisplayName());
                    group.setExtension(g.getExtension());
                    group.setDisplayExtension(g.getDisplayExtension());
                    group.setDescription(g.getDescription());
                    return group;
                })
                .toArray(WsGroup[]::new);
    }

    private List<WsGroup> getGroups(List<OotbGrouping> groupings) {
        Map<String, WsGroup> uniqueGroups = groupings.stream()
                .map(g -> {
                    WsGroup group = new WsGroup();
                    String filteredName = PathFilter.parentGroupingPath(g.getName());
                    group.setName(filteredName);
                    group.setDisplayName(filteredName);
                    group.setExtension("extension");
                    group.setDisplayExtension("extension");
                    group.setDescription(g.getDescription());
                    return group;
                })
                .collect(Collectors.toMap(
                        WsGroup::getName,
                        group -> group,
                        (existing, replacement) -> existing // Remove duplicated parentGroupingPath
                ));

        return new ArrayList<>(uniqueGroups.values());
    }

    private void addNewGroupMemberResult(WsGroup wsGroup, String extension) {
        WsGetMembersResult[] currentResults = wsGetMembersResultsList();

        Optional<WsGetMembersResult> existingResult = Arrays.stream(currentResults)
                .filter(result -> result.getWsGroup() != null && (wsGroup.getName() + ":" + extension).equals(
                        result.getWsGroup().getName()))
                .findFirst();

        if (existingResult.isEmpty()) {
            WsGetMembersResult newResult = new WsGetMembersResult();
            WsGroup WsGroupWithExtension = new WsGroup();
            WsGroupWithExtension.setName(wsGroup.getName() + ":" + extension);
            WsGroupWithExtension.setDisplayName(wsGroup.getName() + ":" + extension);
            WsGroupWithExtension.setExtension(extension);
            WsGroupWithExtension.setDisplayExtension(extension);
            newResult.setWsGroup(WsGroupWithExtension);
            newResult.setWsSubjects(new WsSubject[0]);

            List<WsGetMembersResult> resultList = new ArrayList<>(Arrays.asList(currentResults));
            resultList.add(newResult);

            getMembersResults.getWsGetMembersResults().setResults(resultList.toArray(new WsGetMembersResult[0]));
        }
    }

    private List<WsSubject> convertOotbMemberListToWsSubject(List<OotbMember> ootbMemberList) {
        return ootbMemberList.stream().map(ootbMember -> {
            WsSubject ootbMemberSubject = new WsSubject();
            ootbMemberSubject.setId(ootbMember.getUhUuid());
            ootbMemberSubject.setName(ootbMember.getName());
            ootbMemberSubject.setIdentifierLookup(ootbMember.getUid());
            ootbMemberSubject.setAttributeValues(
                    new String[] { ootbMember.getUid(), "", "LAST NAME", "FIRST NAME" });
            ootbMemberSubject.setSuccess("T");
            ootbMemberSubject.setResultCode("SUCCESS");
            ootbMemberSubject.setSourceId("UH core LDAP");
            return ootbMemberSubject;
        }).toList();
    }

    private WsGroup[] mergeGroups(WsGetGroupsResult result, WsGroup newGroup) {
        WsGroup[] existingGroups = result.getWsGroups();
        if (existingGroups == null) {
            return new WsGroup[] { newGroup };
        }

        boolean groupExists = Arrays.stream(existingGroups)
                .anyMatch(group -> group.getName().equals(newGroup.getName()) && group.getExtension()
                        .equals(newGroup.getExtension()));

        if (groupExists) {
            return existingGroups;
        }

        WsGroup[] updatedGroups = Arrays.copyOf(existingGroups, existingGroups.length + 1);
        updatedGroups[existingGroups.length] = newGroup;
        return updatedGroups;
    }

    private WsGroup createWsGroup(String groupName, String extension) {
        WsGroup newGroup = new WsGroup();
        newGroup.setName(groupName + ":" + extension);
        newGroup.setDisplayName(groupName + ":" + extension);
        newGroup.setExtension(extension);
        newGroup.setDisplayExtension(extension);
        newGroup.setTypeOfGroup("group");
        return newGroup;
    }

    private List<String> getMembersOfMembership(String membership) {
        GetMembersResults getMembersResults1 = getOwnedGroupings(Collections.singletonList(membership));

        return getMembersResults1.getMembersResults().stream()
                .flatMap(getMembersResult -> getMembersResult.getSubjects().stream())
                .map(Subject::getUid)
                .collect(Collectors.toList());
    }
}

